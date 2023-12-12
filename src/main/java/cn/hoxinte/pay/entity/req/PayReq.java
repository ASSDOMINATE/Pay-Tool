package cn.hoxinte.pay.entity.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 支付请求参数
 * </p>
 *
 * @author dominate
 * @since 2023-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer buyId;

    private String productCode;

    /**
     * 支付类型
     */
    @NotNull
    private Integer payType;

    /**
     * 支付编码
     */
    private String orderCode;

    /**
     * 支付凭证
     */
    private String auth;

    // TODO 敏感数据，建议服务器存储

    private Integer accountId;

    private String name;

    private BigDecimal balance;
}
