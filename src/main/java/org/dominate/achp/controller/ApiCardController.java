package org.dominate.achp.controller;

import lombok.AllArgsConstructor;
import org.dominate.achp.common.enums.ExceptionType;
import org.dominate.achp.common.enums.PayType;
import org.dominate.achp.common.helper.ALiPayHelper;
import org.dominate.achp.common.helper.AuthHelper;
import org.dominate.achp.common.helper.PayOrderHelper;
import org.dominate.achp.common.helper.WeChatPayHelper;
import org.dominate.achp.common.utils.ApplePayUtil;
import org.dominate.achp.common.utils.UniqueCodeUtil;
import org.dominate.achp.entity.BaseCard;
import org.dominate.achp.entity.BaseCardRecord;
import org.dominate.achp.entity.dto.CardDTO;
import org.dominate.achp.entity.dto.CardRecordDTO;
import org.dominate.achp.entity.dto.PayOrderDTO;
import org.dominate.achp.entity.req.ExchangeReq;
import org.dominate.achp.entity.req.PayOrderReq;
import org.dominate.achp.entity.req.PayReq;
import org.dominate.achp.service.IBaseCardRecordService;
import org.dominate.achp.service.IBaseCardService;
import org.dominate.achp.service.IBasePaymentRecordService;
import org.dominate.achp.sys.Response;
import org.dominate.achp.sys.exception.BusinessException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author dominate
 * @since 2023-04-03
 */
@RestController
@RequestMapping("/card")
@AllArgsConstructor
public class ApiCardController {

    private final IBaseCardService baseCardService;
    private final IBaseCardRecordService baseCardRecordService;
    private final IBasePaymentRecordService basePaymentRecordService;

    @GetMapping(path = "getEnableCard")
    @ResponseBody
    public Response<List<CardDTO>> getEnableCard() {
        List<CardDTO> cardList = baseCardService.enableList();
        return Response.data(cardList);
    }

    @GetMapping(path = "getUserCard")
    @ResponseBody
    public Response<List<CardRecordDTO>> getUserCard(
            @RequestHeader String token
    ) {
        int accountId = AuthHelper.parseWithValidForId(token);
        List<CardRecordDTO> recordList = baseCardRecordService.userRecordList(accountId);
        return Response.data(recordList);
    }

    @GetMapping(path = "checkUserCard")
    @ResponseBody
    public Response<CardRecordDTO> checkUserCard(
            @RequestHeader String token
    ) {
        int accountId = AuthHelper.parseWithValidForId(token);
        CardRecordDTO record = baseCardRecordService.checkUserRecord(accountId);
        return Response.data(record);
    }

    @PostMapping(path = "createPayOrder")
    @ResponseBody
    public Response<String> createPayOrder(
            @RequestHeader String token,
            @Validated @RequestBody PayReq payReq
    ) {
        int accountId = AuthHelper.parseWithValidForId(token);
        BaseCard card = baseCardService.getById(payReq.getCardId());
        String sysOrderCode = UniqueCodeUtil.createPayOrder(payReq.getPayType());
        String partyOrderCode;
        switch (PayType.getValueByDbCode(payReq.getPayType())) {
            case WECHAT:
                partyOrderCode = WeChatPayHelper.createAppPayOrder(sysOrderCode, card.getBalance(), card.getName());
                break;
            case WECHAT_NATIVE:
                partyOrderCode = WeChatPayHelper.createNativePayOrder(sysOrderCode, card.getBalance(), card.getName());
                break;
            case ALIPAY:
                throw BusinessException.create(ExceptionType.PAY_ORDER_TYPE_ERROR);
                //TODO 未接入
//                partyOrderCode = ALiPayHelper.createPayOrder(sysOrderCode, card.getBalance(), card.getName());
//                break;
            case APPLE:
                throw BusinessException.create(ExceptionType.PAY_ORDER_TYPE_ERROR);
            default:
                throw BusinessException.create(ExceptionType.PARAM_ERROR);
        }
        PayOrderDTO payOrder = new PayOrderDTO(payReq);
        payOrder.setAccountId(accountId);
        payOrder.setSysOrderCode(partyOrderCode);
        PayOrderHelper.save(payOrder);
        return Response.data(partyOrderCode);
    }

    @PostMapping(path = "savePayOrder")
    @ResponseBody
    public Response<Boolean> savePayOrder(
            @RequestHeader String token,
            @Validated @RequestBody PayReq payReq
    ) {
        int accountId = AuthHelper.parseWithValidForId(token);
        String sysOrderCode = UniqueCodeUtil.createPayOrder(payReq.getPayType());
        PayOrderDTO order = new PayOrderDTO(payReq);
        order.setAccountId(accountId);
        order.setSysOrderCode(sysOrderCode);
        PayOrderHelper.save(order);
        return Response.success();
    }

    @PostMapping(path = "checkPayOrder")
    @ResponseBody
    public Response<Boolean> checkPayOrder(
            @RequestHeader String token,
            @Validated @RequestBody PayOrderReq payOrderReq
    ) {
        int accountId = AuthHelper.parseWithValidForId(token);
        PayOrderDTO cacheOrder = PayOrderHelper.find(payOrderReq.getPayType(), payOrderReq.getOrderCode());
        if (null == cacheOrder) {
            throw BusinessException.create(ExceptionType.PAY_ORDER_NOT_FOUND);
        }
        if (accountId != cacheOrder.getAccountId()) {
            throw BusinessException.create(ExceptionType.PAY_ORDER_MUST_SAME_USER);
        }
        BaseCard card = baseCardService.getById(cacheOrder.getCardId());
        switch (PayType.getValueByDbCode(payOrderReq.getPayType())) {
            case APPLE:
                ApplePayUtil.verifyPay(cacheOrder.getAuth());
                break;
            case ALIPAY:
                // 校验金额
                ALiPayHelper.verifyPayOrder(cacheOrder.getPartyOrderCode(), card.getBalance());
                break;
            case WECHAT:
            case WECHAT_NATIVE:
                // 校验金额
                WeChatPayHelper.verifyPayOrder(cacheOrder.getPartyOrderCode(), card.getBalance());
                break;
            default:
                throw BusinessException.create(ExceptionType.PARAM_ERROR);
        }
        int paymentId = basePaymentRecordService.save(cacheOrder, card);
        if (paymentId == 0) {
            return Response.failed();
        }
        if (baseCardRecordService.bindRecord(accountId, card)) {
            PayOrderHelper.remove(cacheOrder.getSysOrderCode());
            return Response.success();
        }
        basePaymentRecordService.removeById(paymentId);
        return Response.failed();

    }

    @PostMapping(path = "exchangeCard")
    @ResponseBody
    public Response<Boolean> exchangeCard(
            @RequestHeader String token,
            @Validated @RequestBody ExchangeReq exchangeReq) {
        int accountId = AuthHelper.parseWithValidForId(token);
        BaseCardRecord record = baseCardRecordService.findActiveRecord(exchangeReq.getExchangeKey());
        if (Optional.ofNullable(record).isEmpty()) {
            throw BusinessException.create(ExceptionType.NOT_FOUND_CARD);
        }
        try {
            baseCardRecordService.checkUserRecord(accountId);
            throw BusinessException.create(ExceptionType.HAS_CARD_BINDING);
        } catch (BusinessException e) {
            // 检查记录状态出现异常代表没有可以用的卡
        }
        BaseCard card = baseCardService.getById(record.getCardId());
        return Response.data(baseCardRecordService.bindRecord(accountId, record.getId(), card));
    }
}
