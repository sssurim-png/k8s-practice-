package com.example.post.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component // 싱글톤, 매번 new되면 안된다
public class SseEmitterRegistry {
//    SseEmitter객체는 사용자의 연결정보(ip,macaddresse등)을 의미
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
//    ConcurrentHashMap은 Thread-Safe한 map(동시성 이슈발생X)

    public void addSseEmitter(String email,SseEmitter sseEmitter){
        this.emitterMap.put(email, sseEmitter);
        System.out.println("add" + this.emitterMap.size());
    }

    public SseEmitter getEmitter(String email){
        return  this.emitterMap.get(email);
    }

    public void removeEmitter(String email){
        this. emitterMap.remove(email);
        System.out.println("remove"+ this.emitterMap.size());
    }
}
