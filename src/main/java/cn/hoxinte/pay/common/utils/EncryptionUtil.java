package cn.hoxinte.pay.common.utils;


import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密工具
 *
 * @author dominate
 * @date 2018/5/12
 */
public final class EncryptionUtil {

    private final static String DES = "DES";
    private final static String DES_CIPHER_NO_PADDING = "DES/ECB/NoPadding";
    private final static String DES_CIPHER = "DES/CBC/PKCS5Padding";
    private final static String DES_SPEC = "12345678";

    private final static String MD5 = "MD5";
    private final static char[] MD5_STRING = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    /**
     * 数字签名，密钥算法
     */
    private static final String RSA_KEY_ALGORITHM = "RSA";

    /**
     * 数字签名签名/验证算法
     */
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * RSA密钥长度，RSA算法的默认密钥长度是1024密钥长度必须是64的倍数，在512到65536位之间
     */
    private static final int KEY_SIZE = 1024;


    private static final String PRI_KEY_PKCS1 =
            "MIICXAIBAAKBgQCtX0eWgh9AD2hPuiNGMSH24ocwB3bpIYruuKk8jUeARb/qfjEx\n" +
                    "WYNRtdGFs79Pm9L7Sfl29JjuBCpg70iTrF0pIgXd9pjLWdwmeF7IF9e3c5IgjA4I\n" +
                    "Nml1qe4g/8baXynfhX90vUGdy3UfpFgzT//Danxa/W8jg8Bfj0irgqyIGQIDAQAB\n" +
                    "AoGAEgqsRHleDyiLTmCscw2B31NLhjAAq9oVvynwUqDRJAQeKKThMaWDCOnG2AcQ\n" +
                    "jZRFrGjSURK7J2m/jz7XaqaxOv651Nf7xckc8Wr+CVKWcQp0Ekv6kbF6+gbsje9R\n" +
                    "bP0MUJLtrd4SmNZgOItz7IC+kYuhd/SChDoX447G4HV49AECQQDU59b5PjqYmY81\n" +
                    "T/bJl6kP2rT55KVVPGOh38EMGy3VZua9HFx4QihxTYOTpmc9cxyfuwMcppyGnm19\n" +
                    "mgQhdBC5AkEA0HbuJbhXNyxGL2i+XR+jc5ccSqjMR9noXpgG62/BA+6ahOL9ZxBQ\n" +
                    "1HwIVUs/qQf8cXR71sfmKKu33L0/KjbCYQJAFrYQgY/40jR3SVmZWtHZz/4llg6k\n" +
                    "8F27xxXGUxNHJV+Pt5ah6pYsGEILiiGTG8P+xq89Wr4PLnER/vcB/8uQyQJAV6or\n" +
                    "6+DhjGop+bXql+6+JdXeJ+dkQLL6bQ0xm8CbQrQMduWd+sF5vGGMf5Hta3/YQT3i\n" +
                    "9ieKOoA8Ca/r6CyvAQJBAImOZakR3BndNXJNqQT/a5OPaGTT5D5P1i4GyzixxV7n\n" +
                    "yZgoxrg+Us6rm30byXCyqF8JC68O/CXaGLlHGPRgiLA=\n";


    private static final String PRI_KEY_PKCS8 =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK0WifknLXvJ+F5R\n" +
                    "nrhvzOyVMFpCoeYJ+TTRUOBJwiJNX2xBAnpPr3b/et9Kfiza1NEYuBBQfpPHcwlZ\n" +
                    "akXl3bi1FvuLukRGC31FOuBHWIs82H7ZrDh2AD+TH8E2uldDXCxaA1dTDSp+SGcc\n" +
                    "KgQWhM1zRxSbYD9wXX40AYBJT8lvAgMBAAECgYA8k7VRGjmMZ+jBVc53XUV6jVIh\n" +
                    "XM2dnWL27cTg4l+LssmRMQVleir+OjauHDuhhirSTVTp/Mn3/WdQ39bWRLc+f6wL\n" +
                    "X5r2PbVtZgBO1lmfxRtMNkP4qPnB2qBqKlUvzPeNeN2cqrEidWLSdioe64ypd5TX\n" +
                    "ZI3LyU54BdyeYeRz6QJBALkFGAeCFd5YbzB+hQDqEqWbH3IoIP19OT2bIzOXb+65\n" +
                    "NPGCTpShJ9gdHyBtjpVGluo6Xh4AF3amGIEHXMdvi6cCQQDvfZxsWO4abDJVYo99\n" +
                    "w4vdGlKim2+vLMoMQKoO3OYXCg7ZBnYA5Ru0tBYZKXEsIcpsvcUF2m9cleHGaa/k\n" +
                    "tez5AkBXwdfE7+nJfa63lLsnVN8lV6+RqQEE2xmEZApNVT4NQCGhj8oP62SHuEBk\n" +
                    "VDK2ETZRwP+VQTvf1iZ3RPO30OuvAkEApaM739f1/kJc4Z9EXRg1ENwW0S4uJXsV\n" +
                    "1zmWJKd1X/Pt5v5H+UB8VBPFwOtfwcUxKtppfS3hnahwOfl41PdaiQJAbBOABM9h\n" +
                    "bVwhT8Qh2GOs5kVZkMo8sbEPfTNMD2/GuTjAWupnqYCSvDZkvQWb54KlPpi7cGsg\n" +
                    "5JEy2OnlhuNXWw==";


    /**
     * 用私钥解密
     *
     * @param encryptedData 经过encryptedData()加密返回的byte数据
     * @return String 解密结果
     */
    public static String decryptData(String encryptedData) {
        try {
            return decryptByPriKey(encryptedData, PRI_KEY_PKCS8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * MD5 加密
     *
     * @param content 加密内容
     * @return String 加密结果
     */
    public static String encryptMd5(String content) {
        //用于加密的字符  
        try {
            //使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中  
            byte[] btInput = content.getBytes();
            //信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
            MessageDigest mdInst = MessageDigest.getInstance(MD5);
            //MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
            mdInst.update(btInput);
            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                // i = 0
                // 95
                str[k++] = MD5_STRING[byte0 >>> 4 & 0xf];
                // 5
                str[k++] = MD5_STRING[byte0 & 0xf];
                // F
            }
            //返回经过加密后的字符串  
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 二行制转字符串
     *
     * @param b 二进制数据
     * @return String 转换结果
     */
    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String sTmp;
        for (int n = 0; b != null && n < b.length; n++) {
            sTmp = Integer.toHexString(b[n] & 0XFF);
            if (sTmp.length() == 1) {
                hs.append('0');
            }
            hs.append(sTmp);
        }
        return hs.toString().toUpperCase();
    }

    private static final int HEX_BYTE_LENGTH = 2;
    private static final int HEX_RADIX = 16;

    public static byte[] hex2byte(byte[] b) {
        if ((b.length % HEX_BYTE_LENGTH) != 0) {
            throw new IllegalArgumentException();
        }
        byte[] b2 = new byte[b.length / HEX_BYTE_LENGTH];
        for (int n = 0; n < b.length; n += HEX_BYTE_LENGTH) {
            String item = new String(b, n, HEX_BYTE_LENGTH);
            b2[n / HEX_BYTE_LENGTH] = (byte) Integer.parseInt(item, HEX_RADIX);
        }
        return b2;
    }

    public static SecretKey generateDesKey(String key) throws Exception {
        DESKeySpec keySpec = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        return keyFactory.generateSecret(keySpec);
    }

    /**
     * DES算法，加密
     *
     * @param data 待加密字符串
     * @param key  加密私钥，长度不能够小于8位
     * @return 加密后的字节数组，一般结合Base64编码使用
     */
    public static String encodeDes(String key, String data) {
        if (data == null) {
            return null;
        }
        try {
            Key secretKey = parseKey(key);
            Cipher cipher = Cipher.getInstance(DES_CIPHER);
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(DES_SPEC.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
            byte[] bytes = cipher.doFinal(data.getBytes());
            return byte2hex(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    /**
     * DES算法，解密
     *
     * @param data 待解密字符串
     * @param key  解密私钥，长度不能够小于8位
     * @return 解密后的字节数组
     * @throws Exception 异常
     */
    public static String decodeDes(String key, String data) throws Exception {
        if (data == null) {
            return null;
        }
        Key secretKey = parseKey(key);
        Cipher cipher = Cipher.getInstance(DES_CIPHER);
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(DES_SPEC.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
        return new String(cipher.doFinal(hex2byte(data.getBytes())));
    }

    private static Key parseKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        //key的长度不能够小于8位字节
        return keyFactory.generateSecret(dks);
    }

    /**
     * DES加密
     *
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回加密后的数据
     */
    public static byte[] encryptDes(byte[] src, byte[] key) throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密匙数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        // 创建一个密匙工厂，然后用它把DESKeySpec转换成
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        // 一个SecretKey对象
        SecretKey secureKey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES_CIPHER_NO_PADDING);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, secureKey, sr);
        // 执行加密操作
        return cipher.doFinal(src);
    }

    /**
     * 解密
     *
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回解密后的原始数据
     */
    public static byte[] decryptDes(byte[] src, byte[] key) throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密匙数据创建一个DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        // 一个SecretKey对象
        SecretKey secureKey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES_CIPHER_NO_PADDING);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, secureKey, sr);
        // 执行解密操作
        return cipher.doFinal(src);
    }

    /**
     * 数据解密
     *
     * @param data 数据
     * @param key  密钥
     * @return String 解密结果
     */
    public static String decryptDes(String data, String key) throws Exception {
        return new String(decryptDes(hex2byte(data.getBytes()), key.getBytes()));
    }

    /**
     * 数据加密
     *
     * @param data 加密数据
     * @param key  密钥
     * @return String 加密结果
     */
    public static String encryptDes(String data, String key) {
        if (data == null) {
            return null;
        }
        try {
            return byte2hex(encryptDes(data.getBytes(), key.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成密钥对
     */
    private static Map<String, String> initKey() throws Exception {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance(RSA_KEY_ALGORITHM);
        SecureRandom secRand = new SecureRandom();

        // 初始化随机产生器
        secRand.setSeed("initSeed".getBytes());
        // 初始化密钥生成器
        keygen.initialize(KEY_SIZE, secRand);
        KeyPair keys = keygen.genKeyPair();

        byte[] pubKey = keys.getPublic().getEncoded();
        String publicKeyString = Base64.encodeBase64String(pubKey);

        byte[] priKey = keys.getPrivate().getEncoded();
        String privateKeyString = Base64.encodeBase64String(priKey);

        Map<String, String> keyPairMap = new HashMap<>(2);
        keyPairMap.put("publicKeyString", publicKeyString);
        keyPairMap.put("privateKeyString", privateKeyString);

        return keyPairMap;
    }

    /**
     * 密钥转成字符串
     *
     * @param key 密钥
     * @return String
     */
    public static String encodeBase64String(byte[] key) {
        return Base64.encodeBase64String(key);
    }

    /**
     * 密钥转成byte[]
     *
     * @param key 密钥
     * @return String
     */
    public static byte[] decodeBase64(String key) {
        return Base64.decodeBase64(key);
    }

    /**
     * 公钥加密
     *
     * @param data      加密前的字符串
     * @param publicKey 公钥
     * @return 加密后的字符串
     */
    public static String encryptByPubKey(String data, String publicKey) throws Exception {
        byte[] pubKey = decodeBase64(publicKey);
        byte[] enSign = encryptByPubKey(data.getBytes(), pubKey);
        return Base64.encodeBase64String(enSign);
    }

    /**
     * 公钥加密
     *
     * @param data   待加密数据
     * @param pubKey 公钥
     * @return 加密结果
     */
    public static byte[] encryptByPubKey(byte[] data, byte[] pubKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 私钥加密
     *
     * @param data       加密前的字符串
     * @param privateKey 私钥
     * @return 加密后的字符串
     */
    public static String encryptByPriKey(String data, String privateKey) throws Exception {
        byte[] priKey = decodeBase64(privateKey);
        byte[] enSign = encryptByPriKey(data.getBytes(), priKey);
        return Base64.encodeBase64String(enSign);
    }

    /**
     * 私钥加密
     *
     * @param data   待加密的数据
     * @param priKey 私钥
     * @return 加密后的数据
     */
    public static byte[] encryptByPriKey(byte[] data, byte[] priKey) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥解密
     *
     * @param data   待解密的数据
     * @param pubKey 公钥
     * @return 解密后的数据
     */
    public static byte[] decryptByPubKey(byte[] data, byte[] pubKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥解密
     *
     * @param data      解密前的字符串
     * @param publicKey 公钥
     * @return 解密后的字符串
     */
    public static String decryptByPubKey(String data, String publicKey) throws Exception {
        byte[] pubKey = decodeBase64(publicKey);
        byte[] design = decryptByPubKey(Base64.decodeBase64(data), pubKey);
        return new String(design);
    }

    /**
     * 私钥解密
     *
     * @param data   待解密的数据
     * @param priKey 私钥
     * @return 解密结果
     */
    public static byte[] decryptByPriKey(byte[] data, byte[] priKey) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 私钥解密
     *
     * @param data       解密前的字符串
     * @param privateKey 私钥
     * @return 解密后的字符串
     */
    public static String decryptByPriKey(String data, String privateKey) throws Exception {
        byte[] priKey = decodeBase64(privateKey);
        byte[] design = decryptByPriKey(Base64.decodeBase64(data), priKey);
        return new String(design);
    }

    /**
     * RSA签名
     *
     * @param data   待签名数据
     * @param priKey 私钥
     * @return 签名
     */
    public static String sign(byte[] data, byte[] priKey) throws Exception {
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        // 生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 实例化Signature
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        // 初始化Signature
        signature.initSign(privateKey);
        // 更新
        signature.update(data);
        return Base64.encodeBase64String(signature.sign());
    }

    /**
     * RSA校验数字签名
     *
     * @param data   待校验数据
     * @param sign   数字签名
     * @param pubKey 公钥
     * @return boolean 校验成功返回true，失败返回false
     */
    public boolean verify(byte[] data, byte[] sign, byte[] pubKey) throws Exception {
        // 实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        // 初始化公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        // 产生公钥
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        // 实例化Signature
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        // 初始化Signature
        signature.initVerify(publicKey);
        // 更新
        signature.update(data);
        // 验证
        return signature.verify(sign);
    }
}
