package cn.hoxinte.pay.common.helper;

import cn.hoxinte.pay.entity.dto.UserAuthDTO;
import org.junit.Test;

public class AuthHelperTest {

    @Test
    public void testParse() {
        UserAuthDTO user = AuthHelper.parse("eyJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50SWQiOjEzMjQ3OCwic2VyaWFsVmVyc2lvblVJRCI6MSwicGhvbmUiOiIiLCJwZXJtaXNzaW9ucyI6W10sInNleCI6MCwibmFtZSI6Iua4uOWuoi0xMzI0NzgiLCJhbGlhcyI6Iua4uOWuoi0xMzI0NzgiLCJhdmF0YXIiOiIiLCJwbGF0Zm9ybUlkIjowLCJlbWFpbCI6IiIsInRva2VuIjoiMTY4MDc3NjcyMDIzNjM2OTg2MDM2MDcyMjMzMTE3ODUifQ.kQSFvZe3KWIuZtnecWPFlfRZEqOh2k2UsiN3TQ12lkU\n");
        System.out.println(user);
    }
}
