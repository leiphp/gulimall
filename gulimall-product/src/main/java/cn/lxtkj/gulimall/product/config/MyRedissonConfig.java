package cn.lxtkj.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    @Value("${spring.redis.host}")
    private String redissonHost;

    @Value("${spring.redis.port}")
    private String redissonPort;

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+redissonHost+":"+redissonPort);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
