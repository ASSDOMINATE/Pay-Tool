package cn.hoxinte.pay.common.helper;

import cn.hoxinte.pay.common.enums.ExceptionType;
import cn.hoxinte.pay.sys.exception.BusinessException;
import cn.hoxinte.tool.utils.JsonUtil;
import cn.hoxinte.tool.utils.LoadUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 支付宝工具类
 *
 * @author dominate
 */
@Slf4j
public final class AliPayHelper {

    // 读取配置问句

    private static final String APP_ID = LoadUtil.getProperty("alipay.app.id");
    private static final String APP_PRIVATE_KEY = LoadUtil.getProperty("alipay.app.private.key");
    private static final String ALI_PAY_PUBLIC_KEY = LoadUtil.getProperty("alipay.ali.public.key");

    // 常量

    private static final String HTTPS_REQ_ADDRESS = "https://openapi.alipay.com/gateway.do";
    private static final String CHARSET = "UTF-8";
    private static final String REQUEST_TYPE = "json";
    private static final String ENCRYPT = "RSA2";

    // 解析/请求参数

    private static final String[] CREATE_ORDER_PARAM_NAMES = {"out_trade_no", "total_amount", "subject"};
    private static final String[] QUERY_ORDER_PARAM_NAMES = {"out_trade_no"};
    private static final String QUERY_ORDER_RESULT_PARAM = "alipay_trade_query_response";
    private static final String TRADE_STATUS = "trade_status";
    private static final String[] TRADE_STATUS_VALUES = {"WAIT_BUYER_PAY", "TRADE_CLOSED", "TRADE_SUCCESS", "TRADE_FINISHED"};
    private static final String TOTAL_AMOUNT = "total_amount";

    private static final AlipayClient ALI_PAY_CLIENT;

    static {
        ALI_PAY_CLIENT = new DefaultAlipayClient(HTTPS_REQ_ADDRESS, APP_ID, APP_PRIVATE_KEY, REQUEST_TYPE, CHARSET, ALI_PAY_PUBLIC_KEY, ENCRYPT);
    }

    /**
     * 创建支付订单
     *
     * @param uniqueOrderCode 唯一支付订单号
     * @param payNum          支付金额
     * @param payTitle        支付标题
     * @return String 支付订单号，若为空字符既创建失败
     */
    public static String createPayOrder(String uniqueOrderCode, BigDecimal payNum, String payTitle) {
        try {
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            request.setBizContent(createRequestContent(CREATE_ORDER_PARAM_NAMES, new String[]{uniqueOrderCode, payNum.toString(), payTitle}));
            AlipayTradeAppPayResponse response = ALI_PAY_CLIENT.sdkExecute(request);
            if (!response.isSuccess()) {
                return StringUtils.EMPTY;
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            log.error("ALiPay create order error ", e);
            return StringUtils.EMPTY;
        }
    }


    /**
     * 支付订单是否已完成
     *
     * @param payResultJson 支付结果
     * @return boolean 是否已完成支付
     */
    public static boolean isSuccessPayOrder(String payResultJson) {
        try {
            String payStatus = JsonUtil.parseResponseValueForString(payResultJson, TRADE_STATUS);
            return TRADE_STATUS_VALUES[2].equals(payStatus) || TRADE_STATUS_VALUES[3].equals(payStatus);
        } catch (Exception e) {
            log.error("ALiPay parse pay result error ", e);
            return false;
        }
    }

    /**
     * 解析支付金额
     *
     * @param payResultJson 支付返回结果JSON
     * @return double 支付金额 RMB 0.00
     */
    public static BigDecimal parsePayNum(String payResultJson) {
        try {
            return new BigDecimal(JsonUtil.parseResponseValueForString(payResultJson, TOTAL_AMOUNT));
        } catch (Exception e) {
            log.error("ALiPay parse pay result error ", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 查询支付订单
     *
     * @param code 支付订单号
     * @return String 支付结果JSON字符串，若为空字符串则查询异常
     * <p>
     * key:trade_status,
     * value:WAIT_BUYER_PAY（交易创建等待买家付款）/TRADE_CLOSED（未付款交易超时关闭/支付完成后全额退款）/TRADE_SUCCESS（交易支付成功）/TRADE_FINISHED（交易结束不可退款）
     * <p>
     * key:total_amount,
     * value:10.00 (支付金额)
     */
    public static void verifyPayOrder(String code, BigDecimal balance) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(createRequestContent(QUERY_ORDER_PARAM_NAMES, new String[]{code}));
        try {
            AlipayTradeQueryResponse response = ALI_PAY_CLIENT.execute(request);
            if (!response.isSuccess()) {
                throw BusinessException.create(ExceptionType.PAY_NOT_COMPLETED);
            }
            String responseJson = JsonUtil.parseResponseValueForString(response.getBody(), QUERY_ORDER_RESULT_PARAM);
            if(balance.compareTo(parsePayNum(responseJson)) != 0){
                throw BusinessException.create(ExceptionType.PAY_PRICE_ERROR);
            }
        } catch (AlipayApiException e) {
            log.error("ALiPay query pay order error ", e);
            throw BusinessException.create(ExceptionType.PAY_NOT_COMPLETED);
        }
    }


    /**
     * 生成请求参数
     *
     * @param paramNames 参数名
     * @param params     参数值
     * @return String JSON请求参数
     */
    private static String createRequestContent(String[] paramNames, String[] params) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < paramNames.length; i++) {
            builder.append("\"");
            builder.append(paramNames[i]);
            builder.append("\":\"");
            builder.append(params[i]);
            builder.append("\"");
            if (i < paramNames.length - 1) {
                builder.append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }


}
