package com.example.post.common.service;

import com.example.post.common.dto.SseMessageDto;
import com.example.post.common.repository.SseEmitterRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener { //리스너 역할(받는 역할도 한다-구현해라)
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;

    private final RedisTemplate<String,String> redisTemplate;

    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper, @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }
//알림을 보내는 쪽 - emitter자료가 있으면 바로 전송, 아니면 redis로 전송 (pub/sub하도록)
    public void sendMessage(String receiver, String sender,String message) {//어디서 가져온거지;;

        SseMessageDto dto= SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .message(message)
                .build();

        try { //내 서버가 상대의 emitter가 없을 수 있다 -> redis로 전파
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);
            String data = objectMapper.writeValueAsString(dto);//직렬화 그냥 dto넣으면 주소뜬다
///            만약에 emitter객체가 현재 서버에 잇으면, 바로 알림발송. 그렇지 않으면 redis pub/sub활용
            if (sseEmitter != null) {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//                사용자가 새로고침후에 알림메시지를 조회하려면 DB에 추가적으로 저장 필요
            } else {

///            redis pub sub 기능을 활용하여 메시지 publish
                redisTemplate.convertAndSend("order-channel", data); //아래에서 메시지를 받아낸다
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
            e.printStackTrace(); ///브라우저 연결끊긴다고 에러처리해서 주문이 끊기면 안되니가 에러x ->로고만
        }
    }


//    Redis에서 넘어온 알림을 받아서 대신 전달하는 쪽(수신자)
    @Override //redis config에서 하다 왔다
    public void onMessage(Message message, byte[] pattern) { //메시지, 패턴에는 채널명이 담겨있다
//        message: 실질적으로 메시지가 담겨있는 객체
//        patter: 채널명
//        추후 여러개의 채널에 각기 메시지를 publish하고 subscribe할 경우, 채널명으로 분기처리 가능
        System.out.println(message.getBody());
        String channelName = new String(pattern);
        System.out.println("channelName:" + channelName);

        try { // -redis pub/sub
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class); //String형태로 들어갔던 메시지를 파싱해서 자바 객체 얻어냄
            String data =objectMapper.writeValueAsString(dto); //거기서 dto직렬화
            SseEmitter sseEmitter =sseEmitterRegistry.getEmitter(dto.getReceiver());//이 서버에 receiver(수신자)가 SSE로 연결돼 있는지 확인
///            해당 서버에 receiver의 emitter객체가 있으면 send
            if(sseEmitter !=null)
                sseEmitter.send(SseEmitter.event().name("orderded").data(data));
            System.out.println("'message:" + dto);
//        System.out.println("messageBody:" + message.getBody());
        }catch (Exception e){
//            throw new RuntimeException(e);
            e.printStackTrace();
        }

    }
}

