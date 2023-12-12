package cn.hoxinte.pay.entity.dto;

import cn.hoxinte.pay.entity.req.PayReq;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 支付订单
 * </p>
 *
 * @author dominate
 * @since 2023-04-04
 */
@Getter
@Setter
@Accessors(chain = true)
public class PayOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 系统订单号
     */
    private String sysOrderCode;

    /**
     * 三方订单号
     */
    private String partyOrderCode;

    /**
     * 用户ID
     */
    private Integer accountId;

    /**
     * 卡密ID
     */
    private Integer cardId;

    /**
     * 支付类型
     */
    private Integer payType;

    /**
     * 支付凭证
     */
    private String auth;

    private String password;

    private Long createTime;

    private Long checkedTime;

    public PayOrderDTO(){}

    public PayOrderDTO(PayReq payReq) {
        this.cardId = payReq.getBuyId();
        this.payType = payReq.getPayType();
        this.auth = payReq.getAuth();
        this.createTime = System.currentTimeMillis();
        this.checkedTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "PayOrderDTO{" +
                "sysOrderCode='" + sysOrderCode + '\'' +
                ", partyOrderCode='" + partyOrderCode + '\'' +
                ", accountId=" + accountId +
                ", cardId=" + cardId +
                ", payType=" + payType +
                ", auth='" + auth + '\'' +
                ", createTime=" + createTime +
                ", checkedTime=" + checkedTime +
                '}';
    }
}
