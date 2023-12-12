package cn.hoxinte.pay.common.helper;

import cn.hoxinte.pay.common.cache.PayOrderCache;
import cn.hoxinte.pay.common.enums.AppleNoticeType;
import cn.hoxinte.pay.common.enums.ExceptionType;
import cn.hoxinte.pay.entity.dto.AppleNoticeDTO;
import cn.hoxinte.pay.entity.dto.AppleProductDTO;
import cn.hoxinte.pay.sys.exception.BusinessException;
import cn.hoxinte.tool.utils.HttpUtil;
import cn.hoxinte.tool.utils.JsonUtil;
import cn.hoxinte.tool.utils.LoadUtil;
import cn.hoxinte.tool.utils.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.*;

/**
 * 苹果支付工具
 *
 * @author dominate
 * @since 2023-04-27
 */
@Slf4j
public class ApplePayHelper {

    private static final String VERIFY_URL = "https://buy.itunes.apple.com/verifyReceipt";
    private static final String VERIFY_URL_SANDBOX = "https://sandbox.itunes.apple.com/verifyReceipt";

    private static final String REQUEST_RECEIPT_DATA = "receipt-data";
    private static final String REQUEST_PASSWORD = "password";

    private static final String CERT_FACTORY_X5C = "X.509";

    // TODO change DTO object

    private static final String RESPONSE_NOTICE_CERT_X5C = "x5c";
    private static final String RESPONSE_NOTICE_SIGNED_PAYLOAD = "signedPayload";
    private static final String RESPONSE_NOTICE_DATA = "data";
    private static final String RESPONSE_NOTICE_SIGNED_TRANSACTION_INFO = "signedTransactionInfo";
    private static final String RESPONSE_NOTICE_ORG_TRANSACTION_ID = "originalTransactionId";
    private static final String RESPONSE_NOTICE_TRANSACTION_ID = "transactionId";
    private static final String RESPONSE_NOTICE_PRODUCT_ID = "productId";
    private static final String RESPONSE_NOTICE_EXPIRES_DATE = "expiresTime";
    private static final String RESPONSE_NOTICE_NOTIFICATION_TYPE = "notificationType";
    private static final String RESPONSE_NOTICE_SUBTYPE = "subtype";
    private static final String RESPONSE_NOTICE_BUNDLE_ID = "bundleId";

    // TODO change DTO object

    private static final String[] RESPONSE_STATUS = {"status"};
    private static final String[] RESPONSE_APP_LIST = {"receipt", "in_app"};
    private static final String[] RESPONSE_BUNDLE = {"receipt", "bundle_id"};
    private static final String RESPONSE_ORG_TRANSACTION_ID = "original_transaction_id";
    private static final String RESPONSE_EXPIRES_DATE_MS = "expires_date_ms";

    private static final String RESPONSE_TRANSACTION_ID = "transaction_id";
    private static final String RESPONSE_PRODUCT_ID = "product_id";

    private static final String SUCCESS_STATUS_CODE = "0";
    private static final String CHECK_ON_SANDBOX_CODE = "21007";

    private static final String APPLE_PASSWORD = LoadUtil.getProperty("apple.pay.password");
    private static final String[] BUNDLE_IDS = LoadUtil.getArrayProperty("apple.pay.bundle-ids");
    private static final boolean USE_SANDBOX = LoadUtil.getBooleanProperty("apple.pay.sandbox");

    /**
     * 苹果支付回调验证
     * 0 正常
     * 21000 App Store不能读取你提供的JSON对象
     * 21002 receipt-data域的数据有问题
     * 21003 receipt无法通过验证
     * 21004 提供的shared secret不匹配你账号中的shared secret
     * 21005 receipt服务器当前不可用
     * 21006 receipt合法，但是订阅已过期。服务器接收到这个状态码时，receipt数据仍然会解码并一起发送
     * 21007 receipt是Sandbox receipt，但却发送至生产系统的验证服务
     * 21008 receipt是生产receipt，但却发送至Sandbox环境的验证服务
     *
     * @param receiptDate 数据凭证
     * @throws BusinessException 未抛出异常代表成功，否则在异常里返回失败原因
     */
    public static void verifyPay(String receiptDate, String orderCode, String productCode) throws BusinessException {
        // 发送平台验证
        String response = verifyReceipt(receiptDate);
        // 验证支付订单号及产品
        JSONArray apps = JsonUtil.parseResponseValueForJsonArray(response, RESPONSE_APP_LIST);
        for (int i = 0; i < apps.size(); ++i) {
            JSONObject app = apps.getJSONObject(i);
            String resultOrderCode = app.getString(RESPONSE_ORG_TRANSACTION_ID);
            if (!orderCode.equals(resultOrderCode)) {
                resultOrderCode = app.getString(RESPONSE_TRANSACTION_ID);
                if (!orderCode.equals(resultOrderCode)) {
                    continue;
                }
            }
            // 检查当前订单号
            String resultProductId = app.getString(RESPONSE_PRODUCT_ID);
            // 产品ID确认
            if (productCode.equals(resultProductId)) {
                return;
            }
            // 产品ID不对
            log.error("苹果验证订单错误，有刷单嫌疑，产品编码不一致，请求订单号 [{}]，请求产品ID [{}]，苹果产品ID [{}]", orderCode, productCode, resultProductId);
            throw BusinessException.create(ExceptionType.PAY_ORDER_NOT_FOUND);
        }
        // 凭证中无该订单号
        log.error(apps.toString());
        log.error("苹果验证订单错误，有刷单嫌疑，凭证中未找到订单号，请求订单号 [{}]", orderCode);
        throw BusinessException.create(ExceptionType.PAY_ORDER_NOT_FOUND);
    }

    public static String verifyReceipt(String receiptDate) {
        //1.发送平台验证
        String response = ApplePayHelper.sendVerify(receiptDate, false);
        // 苹果服务器没有返回验证结果
        if (StringUtil.isEmpty(response)) {
            log.error("苹果验证失败 支付凭证 {}", receiptDate);
            throw BusinessException.create(ExceptionType.PAY_NOT_FOUND_ORDER);
        }
        // 2.苹果验证 返回结果
        String states = JsonUtil.parseResponseValueForString(response, RESPONSE_STATUS);
        // 3.沙盒测试
        if (USE_SANDBOX && CHECK_ON_SANDBOX_CODE.equals(states)) {
            response = ApplePayHelper.sendVerify(receiptDate, true);
            states = JsonUtil.parseResponseValueForString(response, RESPONSE_STATUS);
        }
        // 只有返回状态值为0是成功
        if (!SUCCESS_STATUS_CODE.equals(states)) {
            log.error("苹果验证不通过 结果 {} 支付凭证 {}", states, receiptDate);
            throw BusinessException.create(ExceptionType.PAY_NOT_COMPLETED);
        }
        // 4.验证支付来源
        String bundleId = JsonUtil.parseResponseValueForString(response, RESPONSE_BUNDLE);
        if (isErrorBundleId(bundleId)) {
            log.error("苹果验证订单错误，有刷单嫌疑，系统ID不一致，请求 BUNDLE ID [{}]", bundleId);
        }
        return response;
    }


    public static List<AppleProductDTO> parseProductList(String receiptDate) throws BusinessException {
        String response = verifyReceipt(receiptDate);
        JSONArray apps = JsonUtil.parseResponseValueForJsonArray(response, RESPONSE_APP_LIST);
        List<AppleProductDTO> productList = new ArrayList<>(apps.size());
        for (int i = 0; i < apps.size(); ++i) {
            JSONObject app = apps.getJSONObject(i);
            AppleProductDTO product = new AppleProductDTO();
            product.setProductCode(app.getString(RESPONSE_PRODUCT_ID));
            product.setTransactionId(app.getString(RESPONSE_TRANSACTION_ID));
            product.setOrgTransactionId(app.getString(RESPONSE_ORG_TRANSACTION_ID));
            String expiresTimeStr = app.getString(RESPONSE_EXPIRES_DATE_MS);
            if (StringUtil.isNotEmpty(expiresTimeStr)) {
                product.setExpiresTime(Long.decode(expiresTimeStr));
            } else {
                product.setExpiresTime(0L);
            }
            productList.add(product);
        }
        return productList;
    }

    public static AppleNoticeDTO notice(String request) {
        String signedPayload = JsonUtil.parseResponseValueForString(request, RESPONSE_NOTICE_SIGNED_PAYLOAD);
        DecodedJWT decodedJwt = JWT.decode(signedPayload);
        String header = new String(Base64.getDecoder().decode(decodedJwt.getHeader()));
        String x5c = JSONObject.parseObject(header).getJSONArray(RESPONSE_NOTICE_CERT_X5C).getString(0);
        // 获取公钥
        try {
            PublicKey publicKey = getPublicKeyByX5c(x5c);
            // 验证 token
            Algorithm algorithm = Algorithm.ECDSA256((ECPublicKey) publicKey, null);
            algorithm.verify(decodedJwt);
            // 解析数据
            AppleNoticeDTO notice = new AppleNoticeDTO();
            JSONObject payload = parseBase64String(decodedJwt.getPayload());
            JSONObject data = payload.getJSONObject(RESPONSE_NOTICE_DATA);

            if (isErrorBundleId(data.getString(RESPONSE_NOTICE_BUNDLE_ID))) {
                notice.setType(AppleNoticeType.NO_FOLLOW_UP);
                return notice;
            }
            String notificationType = payload.getString(RESPONSE_NOTICE_NOTIFICATION_TYPE);
            String subType = payload.getString(RESPONSE_NOTICE_SUBTYPE);
            // parse notificationType and subType set notice.type
            log.info("Apple notice request notificationType {} ", notificationType);
            log.info("Apple notice request subType {} ", subType);
            AppleNoticeType appleNoticeType = AppleNoticeType.getValueBySign(notificationType);
            switch (appleNoticeType) {
                case REFUND:
                case DID_RENEW:
                    notice.setType(appleNoticeType);
                    break;
                case DID_CHANGE_RENEWAL_STATUS:
                    if (appleNoticeType.getSubSign().equals(subType)) {
                        notice.setType(AppleNoticeType.REFUND);
                        break;
                    }
                    notice.setType(AppleNoticeType.NO_FOLLOW_UP);
                    break;
                default:
                    notice.setType(AppleNoticeType.NO_FOLLOW_UP);
                    break;
            }
            JSONObject transactionInfo = parseJwsPayload(data.getString(RESPONSE_NOTICE_SIGNED_TRANSACTION_INFO));
            notice.setOrgTransactionId(transactionInfo.getString(RESPONSE_NOTICE_ORG_TRANSACTION_ID));
            notice.setTransactionId(transactionInfo.getString(RESPONSE_NOTICE_TRANSACTION_ID));
            notice.setCardProductCode(transactionInfo.getString(RESPONSE_NOTICE_PRODUCT_ID));
            notice.setExpiresTime(transactionInfo.getLong(RESPONSE_NOTICE_EXPIRES_DATE));
            return notice;
        } catch (Exception e) {
            log.error("ApplePayHelper parse notice failed ,{} ,{}", request, e.getMessage());
            PayOrderCache.saveFailedNotice(request);
            throw BusinessException.create(ExceptionType.PARAM_ERROR);
        }
    }


    private static JSONObject parseJwsPayload(String jws) {
        return parseBase64String(JWT.decode(jws).getPayload());
    }

    private static JSONObject parseBase64String(String base64String) {
        return JSONObject.parseObject(new String(Base64.getDecoder().decode(base64String)));
    }

    private static PublicKey getPublicKeyByX5c(String x5c) throws CertificateException {
        byte[] x5c0Bytes = Base64.getDecoder().decode(x5c);
        CertificateFactory fact = CertificateFactory.getInstance(CERT_FACTORY_X5C);
        X509Certificate cer = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(x5c0Bytes));
        return cer.getPublicKey();
    }

    private static String sendVerify(String receiptDate, boolean onSandbox) {
        Map<String, Object> params = new HashMap<>(1);
        params.put(REQUEST_RECEIPT_DATA, receiptDate);
        params.put(REQUEST_PASSWORD, APPLE_PASSWORD);
        return HttpUtil.sendPost(onSandbox ? VERIFY_URL_SANDBOX : VERIFY_URL, params, true);
    }

    private static boolean isErrorBundleId(String bundleId) {
        for (String oneBundleId : BUNDLE_IDS) {
            if (oneBundleId.equals(bundleId)) {
                return false;
            }
        }
        return true;
    }

}