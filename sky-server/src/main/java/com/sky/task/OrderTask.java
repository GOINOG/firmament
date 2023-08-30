package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * process timeout order(not late than 15min)
     */
    @Scheduled(cron = "0 * * * * ?")//check every minute
    public void processTimeoutOrder() {
        log.info("process timeout order");
        List<Orders> orders = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (orders != null && orders.size() > 0){
            for (Orders order : orders
            ) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason(MessageConstant.ORDER_TIMEOUT);
                orderMapper.update(order);
            }
        }
    }

    /**
     * process delivered order at 1:00 a.m. daily
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("process delivered order");
        List<Orders> orders = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        if (orders != null && orders.size() > 0){
            for (Orders order : orders
            ) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
