package cn.hoxinte.pay.entity.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * APPLE 产品信息
 * </p>
 *
 * @author dominate
 * @since 2023-04-04
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class AppleProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String transactionId;

    private String orgTransactionId;

    private String productCode;

    private Long expiresTime;


}
