package cn.hoxinte.pay.schedule;

import cn.hoxinte.pay.common.cache.PayOrderCache;
import cn.hoxinte.pay.common.enums.PayType;
import cn.hoxinte.pay.common.helper.AliPayHelper;
import cn.hoxinte.pay.common.helper.ApplePayHelper;
import cn.hoxinte.pay.common.helper.WeChatPayHelper;
import cn.hoxinte.pay.entity.dto.PayOrderDTO;
import cn.hoxinte.tool.utils.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * 订单检查
 *
 * @author dominate
 * @since 2023-04-14
 */
@Slf4j
@Component
@AllArgsConstructor
public class OrderCheck {


    /**
     * 每分钟重试一次
     */
    private static final long CHECK_BETWEEN_TIME = 60 * 1000;

    /**
     * 60分钟还未完成算超时
     */
    private static final long ORDER_OUT_TIME = 60 * 60 * 1000;


    @Scheduled(cron = "*/10 * * * * ?")
    public void checkList() {
        long thisTime = System.currentTimeMillis();
        Collection<PayOrderDTO> payOrders = PayOrderCache.getList();
        for (PayOrderDTO payOrder : payOrders) {
            if (StringUtil.isEmpty(payOrder.getSysOrderCode()) || StringUtil.isEmpty(payOrder.getPartyOrderCode())) {
                PayOrderCache.remove(payOrder.getSysOrderCode());
                return;
            }
            // 订单时间检查
            if (null != payOrder.getCheckedTime()) {
                // 1.未到检查间隔时间
                if (payOrder.getCheckedTime() + CHECK_BETWEEN_TIME > thisTime) {
                    continue;
                }
                // 2.超过过期时间
                if (payOrder.getCreateTime() + ORDER_OUT_TIME < thisTime) {
                    // TODO 记录支付失败
                    // do something
                    PayOrderCache.remove(payOrder.getSysOrderCode());
                    continue;

                }
            }
            // TODO 获取订单购买信息 productCode是苹果特有的，其余支付方式提供balance
            // do something
            String productCode = "";
            BigDecimal balance = BigDecimal.ZERO;

            // 3.订单支付检查
            if (!checkPay(payOrder, productCode, balance)) {
                // 支付失败
                payOrder.setCheckedTime(thisTime);
                PayOrderCache.updateCheckTime(payOrder);
                continue;
            }
            log.info("支付订单校验通过 {} - {}", payOrder.getSysOrderCode(), payOrder.getPartyOrderCode());
            // TODO 检测订单号是否唯一
            // do something
            boolean isUniqueOrder = true;
            if (!isUniqueOrder) {
                payOrder.setCheckedTime(thisTime);
                PayOrderCache.updateCheckTime(payOrder);
                continue;
            }
            // TODO 获取订单商品
            // do something
            int recordId = 0;
            // 4.完成订单
            if (recordId != 0) {
                // TODO 完成订单
                // do something
                PayOrderCache.remove(payOrder.getSysOrderCode());
                continue;
            }
            // 5.数据库异常导致保存失败，清理数据，等待下一次重试
            payOrder.setCheckedTime(thisTime);
            PayOrderCache.updateCheckTime(payOrder);
        }
    }

    private static boolean checkPay(PayOrderDTO payOrder, String productCode, BigDecimal balance) {
        PayType payType = PayType.getValueByDbCode(payOrder.getPayType());
        try {
            switch (payType) {
                case APPLE:
                    ApplePayHelper.verifyPay(payOrder.getAuth(), payOrder.getPartyOrderCode(), productCode);
                    return true;
                case ALIPAY:
                    // 校验金额
                    AliPayHelper.verifyPayOrder(payOrder.getSysOrderCode(), balance);
                    return true;
                case WECHAT:
                case WECHAT_NATIVE:
                    // 校验金额
                    WeChatPayHelper.verifyPayOrder(payOrder.getSysOrderCode(), balance);
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            log.info("订单检查未通过 用户ID {} ，系统订单号 {} ，三方订单号 {}，订单生成时间 {}",
                    payOrder.getAccountId(), payOrder.getSysOrderCode(), payOrder.getPartyOrderCode(), payOrder.getCreateTime());
            return false;
        }

    }

}
