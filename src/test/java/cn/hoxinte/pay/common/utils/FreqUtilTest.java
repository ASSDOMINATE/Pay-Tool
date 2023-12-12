package cn.hoxinte.pay.common.utils;

import org.junit.Test;

public class FreqUtilTest {

    @Test
    public void testApiKeyFreq() {
        String key = "key";
        for (int i = 0; i < 10000; i++) {
            if (i % 10 == 0) {
                FreqUtil.addFreqApiKey(key,1);
                System.out.println("延迟释放一次Key");
            } else {
                boolean isAvailable = FreqUtil.waitFreqForApiKey(key);
                System.out.println("拿到可以用的key " + isAvailable);
            }
            System.out.println("请求次数 " + i);
        }
    }
}
