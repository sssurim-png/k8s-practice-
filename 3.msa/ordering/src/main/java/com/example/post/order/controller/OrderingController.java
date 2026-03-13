package com.example.post.order.controller;

import com.example.post.order.dto.OrderCreateDto;
import com.example.post.order.dto.OrderListDto;
import com.example.post.order.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private OrderingService orderingService;
@Autowired
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

//    1. 주문하기

    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody List<OrderCreateDto> dto,String email){
    Long id = orderingService.createFeign(dto,email);
    return ResponseEntity.status(HttpStatus.CREATED).body(id);

    }

//    2. 주문목록 조회
    @GetMapping("/list")
    public List<OrderListDto> listDtos(){
    List<OrderListDto> orderListDto =orderingService.findByAll();
    return orderListDto;
    }


//    3. 내주문목록조회
    @GetMapping("/myorders")
    public List<OrderListDto> myorders(String email){
    List<OrderListDto> listDtos = orderingService.myorders(email);
    return listDtos;

    }


}
