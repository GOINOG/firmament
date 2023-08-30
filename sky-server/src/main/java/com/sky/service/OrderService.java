package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * user place order
     * @param submitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO submitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * get history orders for the user
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status, Long id);

    /**
     * get order details by id
     * @param id
     * @return
     */
    OrderVO orderDetails(Long id);

    /**
     * cancel order
     * @param id
     */
    void cancel(Long id) throws Exception;

    /**
     * order again
     * @param id
     */
    void repetition(Long id);

    /**
     * conditional search orders
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery4Admin(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * Order quantity statistics for each status
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * confirm order
     * @param ocDTO
     */
    void confirm(OrdersConfirmDTO ocDTO);

    /**
     * reject order
     * @param orDTO
     */
    void reject(OrdersRejectionDTO orDTO) throws Exception;

    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    void adminCancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * delivery order
     * @param id
     */
    void delivery(Long id) throws Exception;

    /**
     * complete order
     * @param id
     */
    void complete(Long id);
}
