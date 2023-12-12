package cn.hoxinte.pay.entity.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * 支付订单请求参数
 * </p>
 *
 * @author dominate
 * @since 2023-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayOrderReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单编码
     */
    @NotNull
    private String orderCode;

    /**
     * 支付类型
     */
    @NotNull
    private Integer payType;

    private Boolean onSandbox;

    // TODO 敏感参数建议加密发送

    private Integer account;

}
