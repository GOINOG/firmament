package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {
    /**
     * user place order
     * @param submitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO submitDTO);
}
