package cn.lxtkj.gulimall.ssoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #sso 服务端端口：9080 客户端端口：9081
 * 127.0.0.1 ssoserver.com
 * 127.0.0.1 client1.com
 * 127.0.0.1 client2.com
 */
@SpringBootApplication
public class GulimallTestSsoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallTestSsoServerApplication.class, args);
    }

}
