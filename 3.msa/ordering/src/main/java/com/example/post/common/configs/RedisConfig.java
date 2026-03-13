package com.example.post.common.configs;

import com.example.post.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;






//        레디스 pubsub을 위한, publish를 위한 서버들이 subscribe하게 해주는  bean들  //호출시 레디스 환경 세팅(어떤 포트인지 등)
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("localhost");
        configuration.setPort(port);
///redis pub/sub기능은 sb에 값을 저장하는 기능이 아니므로, 특정db에 의존적이지 않음
//        configuration.setDatabase(1);


        return new LettuceConnectionFactory(configuration);
    }

    @Bean//서비스에서 넘어옴
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> ssePubSubredisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;

    }

///    redis리스너(subscribe)객체
///     호출구조: RedisMessageListenerContaier -> MessageListenerAdapter ->SseAlarmService(MessageListener)
    @Bean
    @Qualifier("ssePubSub") //리슨하고 있다가 들어오면 메시지 어댑터를 호출 -> 알람 서비스쪽????
//    Redis에서 메시지가 들어오면 → MessageListenerAdapter를 통해
    // → 최종적으로 알림 서비스(SseAlarmService.onMessage)가 호출됨
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory, @Qualifier("ssePubSub")MessageListenerAdapter messageListenerAdapter){
        RedisMessageListenerContainer container =new RedisMessageListenerContainer();
        // 어떤 Redis 서버에 붙어서 메시지를 들을지 설정
        // (host, port 등은 ConnectionFactory에 이미 세팅돼 있음)
        container.setConnectionFactory(redisConnectionFactory);
        // Redis 채널 구독 설정, "order-channel"에 메시지가 publish되면
        // → messageListenerAdapter가 메시지를 받음
        // → 내부적으로 onMessage(...) 메서드 호출
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel")); //왼쪽에 채널명, 처리하는 객체(아래)
//        만약에여러 채널을 구독해야 하는 경우, 여러개의 PatternTopic을 add하거나(addMessageListener를 여러 번 호출하거나), 별도의 Listener Bean객체 생성
        return  container;
    }

///     redis에서 수신된 메시지를 처리하는 객체
    @Bean //eservice가 처리해줘
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService){
///        채널로부터 수신되는 message처리를 SseAlarmService의 onMessage메서드로 위임
        return new MessageListenerAdapter(sseAlarmService,"onMessage");
    }




}