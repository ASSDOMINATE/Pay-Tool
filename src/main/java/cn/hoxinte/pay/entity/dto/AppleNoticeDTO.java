package cn.hoxinte.pay.entity.dto;

import cn.hoxinte.pay.common.enums.AppleNoticeType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * APPLE 通知对象
 * </p>
 *
 * @author dominate
 * @since 2023-04-04
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class AppleNoticeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡密产品编码
     */
    private String cardProductCode;

    /**
     * 苹果订单ID
     */
    private String transactionId;

    /**
     * 苹果原订单ID
     */
    private String orgTransactionId;

    private AppleNoticeType type;

    private Long expiresTime;
}
