package com.example.post.common.Controller;

import com.example.post.common.repository.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
//ex. 주문이벤트가 끝났을때 알림 시작 직전 알아서 시작 -> orderservice에서 알림 이벤트 시작
//사용자 식별과 서버 연결
@RestController
@RequestMapping("/sse")
public class SseController {
    private final SseEmitterRegistry sseEmitterRegistry;

    @Autowired
    public SseController(SseEmitterRegistry sseEmitterRegistry) {
        this.sseEmitterRegistry = sseEmitterRegistry;
    }


    @GetMapping("/connect")
    public SseEmitter connect(@RequestHeader("X-User-Email")String  email) throws IOException {
        System.out.println("connect start");
        SseEmitter sseEmitter = new SseEmitter(60 * 60 * 1000L); //1시간짜리 인증. 유효시간 //매우 중요 계속 그냥 쌓이면 과부하걸린다
        sseEmitterRegistry.addSseEmitter(email, sseEmitter); //sseEmitter=절대 싱글톤 객체로 만들면안된다-사용자마다 새로 생성, sseEmitterRegistry=싱글톤

        sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));//sse를 통한 통신(http x)
        return sseEmitter;

    }


    //서버쪽에서 명시적으로 연결을 끊고 싶으르 때 사용
    @GetMapping("/disconnect")
    public void disconnect(@RequestHeader("X-User-Email")String  email) throws IOException {
        sseEmitterRegistry.removeEmitter(email);
        System.out.println("disconnect start");
    }
}
