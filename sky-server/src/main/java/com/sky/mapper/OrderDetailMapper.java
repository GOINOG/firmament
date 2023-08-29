package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * batch insert
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * get by order id
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{id}" )
    List<OrderDetail> getByOrderId(Long id);
}
