package com.example.post.product.service;

import com.example.post.product.dto.ProductStockUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.lang.runtime.ObjectMethods;

@Component
public class StockKafkaListener { //controller를 updateStock의  controller를 안쓰거 이게 대신이다
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public StockKafkaListener(ProductService productService, ObjectMapper objectMapper){
        this.productService =productService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener") //토픽마다 리슨가능
    public void stockConsumer(String message) throws JsonProcessingException {
        System.out.println("==kafka listener start====");
        ProductStockUpdateDto dto = objectMapper.readValue(message, ProductStockUpdateDto.class); //재고 감소
        productService.updateStock(dto);
    }

}

//    try
//
//    {
//        ProductStockUpdateDto dto = objectMapper.readValue(message, ProductStockUpdateDto.class); //재고 감소
//        productService.updateStock(dto);
//    }catch{
//        http요청- >오더쪽에 get맵핑 받아줘야하는거 있어야한다(오더에서 상태값도 있어야하다 -보상 트랜젝션)