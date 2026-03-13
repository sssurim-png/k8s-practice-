package com.example.post.common.configs;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;



    ///    연결 빈 객체 필요-redis정보(port가 뭔데 등등)
    @Bean
///    Qualifier: 같은 Bean객체가 여러개 있을 경우 Bean객체를 구분하기 위한 어노테이션 - 빈객체 거의 여기서만 쓰임 -잘 쓰이지는 않음
    @Qualifier("rtInventory") // 빈객체의 이름을 붙여줌(매개변수로 받아서 구별을 위해)
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("localhost");
        configuration.setPort(port); //변경가능성이 있어 yml에서 땡겨오자
        configuration.setDatabase(0);


        return new LettuceConnectionFactory(configuration);
    }


    @Bean
    @Qualifier("rtInventory")
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {//redis들어가면 일단 뭐가 들어오든 일단 string, List가 들어와도 string으로 바뀜
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // 키값을 스트링으로 만들어서 넣겠다
        redisTemplate.setValueSerializer(new StringRedisSerializer()); //값을 스트링으로 만들어서 넣겠다 (List, set 스트링이지만 태깅은 가지고 있다)
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }



}