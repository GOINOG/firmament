package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * insert order
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * page query
     * @param opqDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO opqDTO);

    /**
     * get by order id
     * @param orderId
     * @return
     */
    @Select("select * from orders where id = #{orderId}")
    Orders getById(Long orderId);

    /**
     * get amount by status
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer getAmountByStatus(Integer status);

    /**
     * status change
     * @param id
     * @param status
     */
    @Update("update orders set status = #{status} where id = #{id}")
    void statusChange(Long id, Integer status);

    /**
     * get timeout orders
     * @return
     * @param ddl
     */
    @Select("select * from orders where status = #{status} and order_time <= #{ddl};" )
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime ddl);
}
