package cn.hoxinte.pay.common.cache;

import cn.hoxinte.pay.entity.dto.PayOrderDTO;
import cn.hoxinte.tool.clients.redis.RedisClient;
import cn.hoxinte.tool.utils.LoadUtil;
import cn.hoxinte.tool.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 支付订单缓存
 * 添加服务器标记
 *
 * @author dominate
 * @since 2023-04-24
 */
@Slf4j
public final class PayOrderCache {

    /**
     * 服务器标记
     */
    private static final String SERVER_SIGN = LoadUtil.getProperty("server.sign");

    /**
     * 哈希缓存
     * 苹果通知失败数据
     */
    private static final String CACHE_APPLE_NOTICE_DATA_FAILED_LIST = SERVER_SIGN + ":cache:apple:notice:failed:list";

    /**
     * 哈希缓存
     * 支付订单信息
     */
    private static final String CACHE_PAY_ORDER_INFO_HASH_KEY = SERVER_SIGN + ":cache:pay:order:info:hash";
    /**
     * 键值缓存
     * 支付订单号
     */
    private static final String CACHE_PAY_ORDER_CODE_HEADER_KEY = SERVER_SIGN + ":cache:pay:order:code:";

    public static void saveFailedNotice(String data) {
        log.info("Apple failed notice count {} ", RedisClient.listLength(CACHE_APPLE_NOTICE_DATA_FAILED_LIST));
        RedisClient.leftPush(CACHE_APPLE_NOTICE_DATA_FAILED_LIST, data);
    }

    public static List<String> getAllFailedNotice() {
        long total = RedisClient.listLength(CACHE_APPLE_NOTICE_DATA_FAILED_LIST);
        return RedisClient.listRange(CACHE_APPLE_NOTICE_DATA_FAILED_LIST, 0, total - 1, String.class);
    }

    /**
     * 更新检查时间
     *
     * @param payOrder 支付订单参数
     */
    public static void updateCheckTime(PayOrderDTO payOrder) {
        String field = createField(payOrder.getPayType(), payOrder.getPartyOrderCode());
        if (!RedisClient.hHasKey(CACHE_PAY_ORDER_INFO_HASH_KEY, field)) {
            return;
        }
        PayOrderDTO cachePayOrder = RedisClient.hGet(CACHE_PAY_ORDER_INFO_HASH_KEY, field, PayOrderDTO.class);
        cachePayOrder.setCheckedTime(payOrder.getCheckedTime());
        RedisClient.hSetPersist(CACHE_PAY_ORDER_INFO_HASH_KEY, field, cachePayOrder);
    }

    /**
     * 保存支付订单，存在相同订单号将只更新订单凭证
     *
     * @param payOrder 支付订单参数
     */
    public static void save(PayOrderDTO payOrder) {
        log.info("saving pay order {} ", payOrder);
        String field = createField(payOrder.getPayType(), payOrder.getPartyOrderCode());
        // 无凭证更新 不存在相同订单号 直接保存
        if (StringUtil.isEmpty(payOrder.getAuth()) || !RedisClient.hHasKey(CACHE_PAY_ORDER_INFO_HASH_KEY, field)) {
            RedisClient.hSetPersist(CACHE_PAY_ORDER_INFO_HASH_KEY, field, payOrder);
            RedisClient.setPersist(CACHE_PAY_ORDER_CODE_HEADER_KEY + payOrder.getSysOrderCode(), field);
            return;
        }
        // 存在订单号 更新 AUTH
        PayOrderDTO cachePayOrder = RedisClient.hGet(CACHE_PAY_ORDER_INFO_HASH_KEY, field, PayOrderDTO.class);
        if (!cachePayOrder.getAuth().equals(payOrder.getAuth())) {
            cachePayOrder.setAuth(payOrder.getAuth());
            RedisClient.hSetPersist(CACHE_PAY_ORDER_INFO_HASH_KEY, field, cachePayOrder);
        }
    }


    /**
     * 查找支付订单
     *
     * @param payType        支付类型
     * @param partyOrderCode 三方订单编码
     * @return 找到返回订单，未找到返回 null
     */
    public static PayOrderDTO find(Integer payType, String partyOrderCode) {
        String field = createField(payType, partyOrderCode);
        if (!RedisClient.hHasKey(CACHE_PAY_ORDER_INFO_HASH_KEY, field)) {
            return null;
        }
        return RedisClient.hGet(CACHE_PAY_ORDER_INFO_HASH_KEY, field, PayOrderDTO.class);
    }

    /**
     * 删除订单 用于支付完成或超时
     *
     * @param sysOrderCode 系统订单号
     */
    public static void remove(String sysOrderCode) {
        String key = CACHE_PAY_ORDER_CODE_HEADER_KEY + sysOrderCode;
        if (!RedisClient.hasKey(key)) {
            return;
        }
        String field = RedisClient.get(key, String.class);
        RedisClient.removeKey(key);
        RedisClient.hRemoveField(CACHE_PAY_ORDER_INFO_HASH_KEY, field);
    }

    /**
     * 获取订单列表
     *
     * @return 订单列表
     */
    public static Collection<PayOrderDTO> getList() {
        Map<String, PayOrderDTO> payOrderMap = RedisClient.hGetAll(CACHE_PAY_ORDER_INFO_HASH_KEY, PayOrderDTO.class);
        return payOrderMap.values();
    }

    private static String createField(Integer payType, String partyOrderCode) {
        return payType + ":" + partyOrderCode;
    }
}
