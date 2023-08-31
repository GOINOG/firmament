package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController(("userOrderController"))
@Slf4j
@RequestMapping("/user/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    /**
     * user place order
     * @param submitDTO
     * @return
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO submitDTO){
        log.info("user place order");
        OrderSubmitVO submitVO = orderService.submitOrder(submitDTO);
        return Result.success(submitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * get history orders for the user
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> history(int page, int pageSize, Integer status) {
        Long id = BaseContext.getCurrentId();
        log.info("get history orders of user id = {}", id);
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status, id);
        return Result.success(pageResult);
    }

    /**
     * get order details by id
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetails(@PathVariable Long id){
        log.info("check order details, order id = {}", id);
        OrderVO orderVO = orderService.orderDetails(id);
        return Result.success(orderVO);
    }

    /**
     * cancel order
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) throws Exception {
        log.info("cancel order, order id = {}", id);
        orderService.cancel(id);
        return Result.success();
    }

    /**
     * order again
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id){
        log.info("order again, order id = {}", id);
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * client reminder
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id){
        log.info("client reminder, orderId = {}", id);
        orderService.reminder(id);
        return Result.success();
    }
}
