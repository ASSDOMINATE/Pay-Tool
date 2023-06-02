package org.dominate.achp.controller;

import lombok.AllArgsConstructor;
import org.dominate.achp.common.cache.ChatCache;
import org.dominate.achp.common.cache.PayOrderCache;
import org.dominate.achp.common.enums.CardRecordState;
import org.dominate.achp.common.enums.PayType;
import org.dominate.achp.common.enums.PaymentTargetType;
import org.dominate.achp.common.helper.ApplePayHelper;
import org.dominate.achp.common.helper.AuthHelper;
import org.dominate.achp.entity.BaseCard;
import org.dominate.achp.entity.BaseCardRecord;
import org.dominate.achp.entity.BasePaymentRecord;
import org.dominate.achp.entity.dto.AppleNoticeDTO;
import org.dominate.achp.entity.dto.ContentDTO;
import org.dominate.achp.entity.dto.GroupDTO;
import org.dominate.achp.entity.req.PageReq;
import org.dominate.achp.service.*;
import org.dominate.achp.sys.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 对话记录相关接口
 *
 * @author dominate
 * @since 2023-04-03
 */
@RestController
@RequestMapping("/external")
@AllArgsConstructor
public class ApiExternalController {

    private final IBasePaymentRecordService basePaymentRecordService;
    private final IBaseCardRecordService baseCardRecordService;
    private final IBaseCardService baseCardService;

    /**
     * 苹果通知
     *
     * @param data 通知数据
     */
    @GetMapping(path = "apple/notice")
    @ResponseBody
    public Response<Boolean> appleNotice(
            @RequestBody String data
    ) {
        AppleNoticeDTO notice = ApplePayHelper.notice(data);
        BasePaymentRecord findPayment;
        switch (notice.getType()) {
            case RENEW:
                // 续费
                // 1.查询第一次购买的订单
                String orgPartyOrder = notice.getOrgTransactionId();
                findPayment = basePaymentRecordService.find(PayType.APPLE, orgPartyOrder);
                if (null == findPayment || PaymentTargetType.CARD.getCode() != findPayment.getTargetType()) {
                    break;
                }
                // 2.查询到初次生成的卡密套餐
                BaseCard card = baseCardService.findCardForRenew(notice.getCardProductCode());
                String partyOrder = notice.getTransactionId();
                // 3.确认不会重复购买
                if (!basePaymentRecordService.isUniqueOrder(partyOrder, PayType.APPLE.getDbCode())) {
                    break;
                }
                // 4.创建新的购买信息
                int cardRecordId = baseCardRecordService.bindRecord(findPayment.getAccountId(), card);
                basePaymentRecordService.save(findPayment.getAccountId(), partyOrder, PayType.APPLE, card, cardRecordId);
                break;
            case CANCEL_BUY:
                // 取消购买
                // 1.查询生效的订单
                String cancelPartyOrder = notice.getTransactionId();
                findPayment = basePaymentRecordService.find(PayType.APPLE, cancelPartyOrder);
                if (null == findPayment || PaymentTargetType.CARD.getCode() != findPayment.getTargetType()) {
                    break;
                }
                // 2.设置订单的卡密禁用
                BaseCardRecord disabledRecord = new BaseCardRecord();
                disabledRecord.setId(findPayment.getTargetId());
                disabledRecord.setState(CardRecordState.DISABLED.getCode());
                baseCardRecordService.updateById(disabledRecord);
                ChatCache.removeUsingCard(findPayment.getAccountId());
                // 3.清空购买记录金额
                BasePaymentRecord cancelPayment = new BasePaymentRecord();
                cancelPayment.setId(findPayment.getId());
                cancelPayment.setPrice(BigDecimal.ZERO);
                basePaymentRecordService.updateById(cancelPayment);
                break;
            case INVALID:
            default:
                break;
        }
        return Response.success();
    }


}
