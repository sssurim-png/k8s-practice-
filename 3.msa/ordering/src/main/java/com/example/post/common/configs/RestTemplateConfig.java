package com.example.post.common.configs;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration //유레카로 바로 가기 위해. (싱글톤,@LoadBalanced 내부 통신할 수 잇는것을 쓰려고)
public class RestTemplateConfig {

    @Bean
//    eureka에 등록된 서비스명을 사용해서 내부서비스 호출(내부통신)하는 어노테이션
    @LoadBalanced
    public RestTemplate makeResTemplate(){
        return new RestTemplate();
    }
}
