package org.dominate.achp.common.cache;

import com.hwja.tool.clients.redis.RedisClient;
import com.hwja.tool.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.dominate.achp.entity.dto.PayOrderDTO;

import java.util.Collection;
import java.util.Map;

@Slf4j
public final class PayOrderCache {

    private static final String CACHE_PAY_ORDER_INFO_HASH_KEY = "cache:pay:order:info:hash";
    private static final String CACHE_PAY_ORDER_CODE_HEADER_KEY = "cache:pay:order:code:";

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