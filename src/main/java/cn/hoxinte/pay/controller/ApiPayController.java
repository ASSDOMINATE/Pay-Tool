package cn.hoxinte.pay.controller;

import cn.hoxinte.pay.common.cache.PayOrderCache;
import cn.hoxinte.pay.common.enums.ExceptionType;
import cn.hoxinte.pay.common.enums.PayType;
import cn.hoxinte.pay.common.enums.ResponseType;
import cn.hoxinte.pay.common.helper.AliPayHelper;
import cn.hoxinte.pay.common.helper.ApplePayHelper;
import cn.hoxinte.pay.common.helper.WeChatPayHelper;
import cn.hoxinte.pay.common.utils.UniqueCodeUtil;
import cn.hoxinte.pay.entity.dto.AppleProductDTO;
import cn.hoxinte.pay.entity.dto.PayOrderDTO;
import cn.hoxinte.pay.entity.dto.PayResultDTO;
import cn.hoxinte.pay.entity.req.AppleResumeReq;
import cn.hoxinte.pay.entity.req.PayOrderReq;
import cn.hoxinte.pay.entity.req.PayReq;
import cn.hoxinte.pay.sys.Response;
import cn.hoxinte.pay.sys.exception.BusinessException;
import cn.hoxinte.tool.utils.StringUtil;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author dominate
 * @since 2023-04-03
 */
@RestController
@RequestMapping("/pay")
@AllArgsConstructor
public class ApiPayController {


    @PostMapping(path = "createPayUrl")
    @ResponseBody
    public Response<PayResultDTO> createPayUrl(
            @Validated @RequestBody PayReq payReq
    ) {
        int accountId = payReq.getAccountId();
        String sysOrderCode = UniqueCodeUtil.createPayOrder(payReq.getPayType());
        PayResultDTO payResult;
        switch (PayType.getValueByDbCode(payReq.getPayType())) {
            case WECHAT_NATIVE:
                payResult = WeChatPayHelper.createNativePayOrder(sysOrderCode, payReq.getBalance(), payReq.getName());
                break;
            case WECHAT:
            case ALIPAY:
            case APPLE:
                throw BusinessException.create(ExceptionType.PAY_ORDER_TYPE_ERROR);
            default:
                throw BusinessException.create(ExceptionType.PARAM_ERROR);
        }
        payReq.setOrderCode(sysOrderCode);
        payResult.setSysOrderCode(sysOrderCode);
        PayOrderDTO payOrder = new PayOrderDTO(payReq);
        payOrder.setAccountId(accountId);
        payOrder.setSysOrderCode(sysOrderCode);
        payOrder.setPartyOrderCode(payResult.getPartyOrderCode());
        PayOrderCache.save(payOrder);
        return Response.data(payResult);
    }

    @PostMapping(path = "createPayOrder")
    @ResponseBody
    public Response<String> createPayOrder(
            @Validated @RequestBody PayReq payReq
    ) {
        String sysOrderCode = UniqueCodeUtil.createPayOrder(payReq.getPayType());
        String partyOrderCode;
        switch (PayType.getValueByDbCode(payReq.getPayType())) {
            case WECHAT:
                partyOrderCode = WeChatPayHelper.createAppPayOrder(sysOrderCode, payReq.getBalance(), payReq.getName());
                break;
            case WECHAT_NATIVE:
            case ALIPAY:
            case APPLE:
                throw BusinessException.create(ExceptionType.PAY_ORDER_TYPE_ERROR);
            default:
                throw BusinessException.create(ExceptionType.PARAM_ERROR);
        }
        payReq.setOrderCode(sysOrderCode);
        PayOrderDTO payOrder = new PayOrderDTO(payReq);
        payOrder.setAccountId(payReq.getAccountId());
        payOrder.setSysOrderCode(sysOrderCode);
        payOrder.setPartyOrderCode(partyOrderCode);
        PayOrderCache.save(payOrder);
        return Response.data(partyOrderCode);
    }

    @PostMapping(path = "savePayOrder")
    @ResponseBody
    public Response<Boolean> savePayOrder(
            @Validated @RequestBody PayReq payReq
    ) {
        if (StringUtil.isEmpty(payReq.getOrderCode())) {
            return Response.failed();
        }
        String sysOrderCode = UniqueCodeUtil.createPayOrder(payReq.getPayType());
        if (null == payReq.getBuyId()) {
            // TODO Apple 支付需要提供 ProductCode 查询购买物品ID
            // do something
            payReq.setBuyId(0);
        }
        PayOrderDTO order = new PayOrderDTO(payReq);
        order.setAccountId(payReq.getAccountId());
        order.setSysOrderCode(sysOrderCode);
        order.setPartyOrderCode(payReq.getOrderCode());
        PayOrderCache.save(order);
        return Response.success();
    }

    @PostMapping(path = "resumeApple")
    @ResponseBody
    public Response<Boolean> resumeApple(
            @Validated @RequestBody AppleResumeReq resumeReq
    ) {
        // TODO 用户ID
        int accountId = 0;
        List<AppleProductDTO> productList = ApplePayHelper.parseProductList(resumeReq.getReceiptDate());
        // 一般不凭证中不会有多个产品，故只会循环1次暂时不用优化数据库操作
        for (AppleProductDTO product : productList) {
            // 多个购买产品 确认状态后恢复购买
        }
        return Response.code(ResponseType.RESUME_SUCCESS);
    }

    @PostMapping(path = "checkPayOrder")
    @ResponseBody
    public Response<Boolean> checkPayOrder(
            @Validated @RequestBody PayOrderReq payOrderReq
    ) {
        PayOrderDTO cacheOrder = PayOrderCache.find(payOrderReq.getPayType(), payOrderReq.getOrderCode());
        if (null == cacheOrder) {
            throw BusinessException.create(ExceptionType.PAY_ORDER_NOT_FOUND);
        }
        if (!Objects.equals(payOrderReq.getAccount(), cacheOrder.getAccountId())) {
            throw BusinessException.create(ExceptionType.PAY_ORDER_MUST_SAME_USER);
        }
        // TODO 获取订单购买信息 productCode是苹果特有的，其余支付方式提供balance
        // do something
        String productCode = "";
        BigDecimal balance = BigDecimal.ZERO;
        switch (PayType.getValueByDbCode(payOrderReq.getPayType())) {
            case APPLE:
                ApplePayHelper.verifyPay(cacheOrder.getAuth(), cacheOrder.getPartyOrderCode(), productCode);
                break;
            case ALIPAY:
                // 校验金额
                AliPayHelper.verifyPayOrder(cacheOrder.getPartyOrderCode(), balance);
                break;
            case WECHAT:
            case WECHAT_NATIVE:
                // 校验金额
                WeChatPayHelper.verifyPayOrder(cacheOrder.getPartyOrderCode(), balance);
                break;
            default:
                throw BusinessException.create(ExceptionType.PARAM_ERROR);
        }
        // TODO 检测订单号是否唯一
        // do something
        boolean isUniqueOrder = true;
        if (!isUniqueOrder) {
            throw BusinessException.create(ExceptionType.PAY_NOT_COMPLETED);
        }

        // TODO 获取订单商品
        // do something
        int recordId = 0;
        // 4.完成订单
        if (recordId != 0) {
            // TODO 完成订单
            // do something
            PayOrderCache.remove(cacheOrder.getSysOrderCode());
            return Response.success();
        }
        return Response.failed();
    }

}
