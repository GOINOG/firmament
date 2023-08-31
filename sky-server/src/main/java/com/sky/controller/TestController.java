package com.sky.controller;

import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Autowired
    private OrderService orderService;

    /*@PutMapping("/paySuccess")
    public Result testPaySuccess(String num){
        log.info("test paySuccess method with num: {}", num);
        orderService.paySuccess(num);
        return Result.success();
    }*/
}
