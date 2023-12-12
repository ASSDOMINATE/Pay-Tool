package cn.hoxinte.pay.common.helper;

import cn.hoxinte.pay.common.enums.ExceptionType;
import cn.hoxinte.pay.common.utils.EncryptionUtil;
import cn.hoxinte.pay.entity.dto.PayResultDTO;
import cn.hoxinte.pay.sys.exception.BusinessException;
import cn.hoxinte.tool.utils.HttpUtil;
import cn.hoxinte.tool.utils.LoadUtil;
import cn.hoxinte.tool.utils.RandomUtil;
import cn.hoxinte.tool.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContexts;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.*;

/**
 * 微信支付工具
 *
 * @author dominate
 * @since 2023-04-27
 */
@Slf4j
public final class WeChatPayHelper {

    /**
     * 商户平台设置的密钥key
     */
    private static final String PLATFORM_KEY = LoadUtil.getProperty("wechat.pay.platform.key");
    private static final String APP_ID = LoadUtil.getProperty("wechat.pay.app.id");
    private static final String MCH_ID = LoadUtil.getProperty("wechat.pay.mch.id");
    private static final String WX_PAY_CALLBACK = LoadUtil.getProperty("wechat.pay.callback");
    private static final String CERT_FILE_PATH = LoadUtil.getProperty("wechat.pay.platform.ssl-path");


    private static final String CREATE_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private static final String[] CREATE_PAY_ORDER_FOR_APP_KEYS = {"mch_id", "appid", "trade_type", "notify_url", "body", "total_fee", "out_trade_no", "nonce_str", "spbill_create_ip"};

    private static final String QUERY_ORDER_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    private static final String[] QUERY_PAY_ORDER_KEYS = {"mch_id", "appid", "out_trade_no", "nonce_str"};


    private static final String REFUND_ORDER_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    private static final String[] REFUND_PAY_ORDER_KEYS = {"mch_id", "appid", "out_trade_no", "out_refund_no", "total_fee", "refund_fee", "nonce_str", "refund_account"};

    /**
     * 返回结果常量
     */

    private static final String REFUND_TYPE_UNSETTLED = "REFUND_SOURCE_UNSETTLED_FUNDS";
    private static final String REFUND_TYPE_RECHARGE = "REFUND_SOURCE_RECHARGE_FUNDS";
    private static final String RESULT_TYPE = "result_code";
    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String PREPAY_ID = "prepay_id";
    private static final String CODE_URL = "code_url";
    private static final String TOTAL_FEE = "total_fee";
    private static final String TRADE_STATE = "trade_state";
    private static final String TRADE_TYPE_APP = "APP";
    private static final String TRADE_TYPE_NATIVE = "NATIVE";


    private static final String IP_ADDRESS;

    private static final String SSL_KEY_INSTANCE = "PKCS12";
    private static final SSLContext SSL_CONTEXT;

    static {
        SSL_CONTEXT = loadSslCertFile();
        IP_ADDRESS = getIpAddress();
    }

    /**
     * 读取证书文件
     *
     * @return
     */
    private static SSLContext loadSslCertFile() {
        try (FileInputStream inStream = new FileInputStream(CERT_FILE_PATH)) {
            KeyStore keyStore = KeyStore.getInstance(SSL_KEY_INSTANCE);
            keyStore.load(inStream, MCH_ID.toCharArray());
            return SSLContexts.custom()
                    .loadKeyMaterial(keyStore, MCH_ID.toCharArray())
                    .build();
        } catch (Exception e) {
            log.error("load SSL cert error ", e);
        }
        return null;
    }

    private static String parseParamPrice(BigDecimal price) {
        return String.valueOf(price.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).intValue());
    }

    public static PayResultDTO createNativePayOrder(String uniqueOrderCode, BigDecimal payNum, String payTitle) {
        return createPayOrder(uniqueOrderCode, payNum, payTitle, TRADE_TYPE_NATIVE);
    }

    public static String createAppPayOrder(String uniqueOrderCode, BigDecimal payNum, String payTitle) {
        PayResultDTO result = createPayOrder(uniqueOrderCode, payNum, payTitle, TRADE_TYPE_APP);
        if (null == result) {
            return StringUtil.EMPTY;
        }
        return result.getPartyOrderCode();
    }

    /**
     * 创建支付订单 用于 APP支付
     *
     * @param uniqueOrderCode 唯一订单号
     * @param payNum          支付金额
     * @param payTitle        支付标题
     * @return String 微信支付订单号，若为空字符串即请求失败
     */
    public static PayResultDTO createPayOrder(String uniqueOrderCode, BigDecimal payNum, String payTitle, String tradeType) {
        String nonceStr = createNonceStr();
        String[] values = {MCH_ID, APP_ID, tradeType, WX_PAY_CALLBACK, payTitle, parseParamPrice(payNum), uniqueOrderCode, nonceStr, IP_ADDRESS};
        try {
            String response = sendRequest(CREATE_ORDER_URL, CREATE_PAY_ORDER_FOR_APP_KEYS, values);
            Map<String, Object> resultMap = WeChatPayHelper.parseXml(response);
            String returnCode = (String) resultMap.get(RESULT_TYPE);
            if (!RESULT_SUCCESS.equals(returnCode)) {
                return null;
            }
            if (tradeType.equals(TRADE_TYPE_NATIVE)) {
                return new PayResultDTO().setCodeUrl(resultMap.get(CODE_URL).toString())
                        .setPartyOrderCode(resultMap.get(PREPAY_ID).toString())
                        .setSign(nonceStr);
            }
            return new PayResultDTO().setPartyOrderCode(resultMap.get(PREPAY_ID).toString())
                    .setSign(nonceStr);
        } catch (Exception e) {
            log.error("WeChat create pay order error ", e);
            return null;
        }
    }


    /**
     * 查询订单
     *
     * @param uniqueOrderCode 唯一订单号
     *                        <p>
     *                        未抛出异常则检查通过
     *                        key:trade_state, value:SUCCESS—支付成功/REFUND—转入退款/NOTPAY—未支付/CLOSED—已关闭/REVOKED—已撤销（刷卡支付）/USERPAYING--用户支付中/PAYERROR--支付失败(其他原因，如银行返回失败)
     *                        key:total_fee, value:0.00
     */
    public static void verifyPayOrder(String uniqueOrderCode, BigDecimal balance) {
        String nonceStr = createNonceStr();
        String[] values = {MCH_ID, APP_ID, uniqueOrderCode, nonceStr};
        String response = sendRequest(QUERY_ORDER_URL, QUERY_PAY_ORDER_KEYS, values);
        Map<String, Object> responseMap = parseXml(response);
        if (!isSuccess(responseMap)) {
            throw BusinessException.create(ExceptionType.PAY_NOT_COMPLETED);
        }
        if (!responseMap.get(TRADE_STATE).equals(RESULT_SUCCESS)) {
            throw BusinessException.create(ExceptionType.PAY_NOT_COMPLETED);
        }
        if (balance.compareTo(parsePayNum(responseMap)) != 0) {
            throw BusinessException.create(ExceptionType.PAY_PRICE_ERROR);
        }

    }


    /**
     * 原路退款，先进行冻结金额退款，失败后进行余额退款
     *
     * @param uniqueOrderCode 支付订单号
     * @param refundOrderCode 退款订单号
     * @param totalFee        退款总金额
     * @param refundFee       退款金额
     * @return 微信返回XML结果
     */
    public static String refundPayOrder(String uniqueOrderCode, String refundOrderCode, int totalFee, int refundFee) {
        String firstResponse = refundPayOrder(uniqueOrderCode, refundOrderCode, totalFee, refundFee, REFUND_TYPE_UNSETTLED);

        Map<String, Object> firstResponseMap = parseXml(firstResponse);
        if (isSuccess(firstResponseMap)) {
            return firstResponse;
        }

        return refundPayOrder(uniqueOrderCode, refundOrderCode, totalFee, refundFee, REFUND_TYPE_RECHARGE);
    }

    /**
     * 原路退款
     *
     * @param uniqueOrderCode 支付订单号
     * @param refundOrderCode 退款订单号
     * @param totalFee        退款总金额
     * @param refundFee       退款金额
     * @param refundType      退款类型 冻结金额 / 余额
     * @return 微信返回XML结果
     */
    private static String refundPayOrder(String uniqueOrderCode, String refundOrderCode, int totalFee, int refundFee, String refundType) {
        String nonceStr = createNonceStr();
        String totalFeeStr = String.valueOf(totalFee);
        String refundFeeStr = String.valueOf(refundFee);
        String[] values = {MCH_ID, APP_ID, uniqueOrderCode, refundOrderCode, totalFeeStr, refundFeeStr, nonceStr, refundType};

        return sendRequest(REFUND_ORDER_URL, REFUND_PAY_ORDER_KEYS, values, true);
    }

    /**
     * 判断请求是否成功
     *
     * @param resultMap 微信返回Map结果
     * @return 是否成功
     */
    public static boolean isSuccess(Map<String, Object> resultMap) {
        return RESULT_SUCCESS.equals(resultMap.get(RESULT_TYPE));
    }

    /**
     * 判断请求是否成功
     *
     * @param resultXml 微信返回Xml结果
     * @return 是否成功
     */
    public static boolean isSuccess(String resultXml) {
        return RESULT_SUCCESS.equals(parseXml(resultXml).get(RESULT_TYPE));
    }

    /**
     * 解析支付订单结果中的金额
     *
     * @param payResultMap 微信返回Map结果
     * @return 订单金额
     */
    public static BigDecimal parsePayNum(Map<String, Object> payResultMap) {
        final BigDecimal rate = new BigDecimal("100");
        try {
            // 微信的支付价格单位为分
            String totalFee = payResultMap.get(TOTAL_FEE).toString();
            if (!StringUtil.isNumeric(totalFee)) {
                return BigDecimal.ZERO;
            }
            BigDecimal thisFee = new BigDecimal(totalFee);
            return thisFee.divide(rate, RoundingMode.DOWN);
        } catch (Exception e) {
            log.error("WeChat parse pay result error ", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取服务器IP
     *
     * @return 服务器IP
     */
    private static String getIpAddress() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("load server ip address error ", e);
        }
        return "";
    }


    private static Map<String, Object> parseXml(String xml) {
        Map<String, Object> map = new HashMap<>(8);
        Document doc;
        try {
            doc = DocumentHelper.parseText(xml);
            Element rootElt = doc.getRootElement();
            List<Element> list = rootElt.elements();
            for (Element element : list) {
                map.put(element.getName(), element.getText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


    /**
     * 微信签名
     *
     * @param map 参数Map
     * @param key 签名Key
     * @return 签名字符串
     */
    private static String signature(Map<String, String> map, String key) {
        Set<String> keySet = map.keySet();
        String[] str = new String[map.size()];
        StringBuilder tmp = new StringBuilder();
        // 进行字典排序
        str = keySet.toArray(str);
        Arrays.sort(str);
        for (String s : str) {
            tmp.append(s).append("=").append(map.get(s)).append("&");
        }
        if (null != key) {
            tmp.append("key=").append(key);
        }
        return Objects.requireNonNull(EncryptionUtil.encryptMd5(tmp.toString())).toUpperCase();
    }

    /**
     * 处理参数为微信要求的XML
     *
     * @param keys   键数组
     * @param values 值数组
     * @return XML字符串
     */
    private static String formatRequestXml(String[] keys, String[] values) {
        assert keys.length == values.length;

        Map<String, String> paramMap = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            paramMap.put(keys[i], values[i]);
        }
        String sign = signature(paramMap, PLATFORM_KEY);
        StringBuilder xmlStr = new StringBuilder();
        xmlStr.append("<xml>");
        for (int i = 0; i < keys.length; i++) {
            xmlStr.append("<");
            xmlStr.append(keys[i]);
            xmlStr.append(">");
            xmlStr.append(values[i]);
            xmlStr.append("</");
            xmlStr.append(keys[i]);
            xmlStr.append(">");
        }
        xmlStr.append("<sign>");
        xmlStr.append(sign);
        xmlStr.append("</sign></xml>");
        return xmlStr.toString();
    }


    private static String sendRequest(String url, String[] keys, String[] values) {
        return sendRequest(url, keys, values, false);
    }

    /**
     * 发起请求
     *
     * @param url
     * @param keys
     * @param values
     * @return
     */
    private static String sendRequest(String url, String[] keys, String[] values, boolean useSsl) {
        String xml = formatRequestXml(keys, values);
        if (useSsl) {
            return HttpUtil.sendPostWithSsl(url, xml, SSL_CONTEXT);
        }
        return HttpUtil.sendPost(url, xml);
    }

    private static String createNonceStr() {
        return RandomUtil.getStringRandom(32);
    }

}
