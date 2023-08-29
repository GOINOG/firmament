package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * user place order
     * @param submitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO submitDTO) {
        //check addressBook
        AddressBook addressBook = addressBookMapper.getById(submitDTO.getAddressBookId());
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //check shopping cart
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.getByUserId(BaseContext.getCurrentId());
        if (shoppingCarts == null || shoppingCarts.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //insert into orders_table
        Orders orders = new Orders();
        BeanUtils.copyProperties(submitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());

        orderMapper.insert(orders);

        //insert into order_detail_table
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts){
            OrderDetail orderDetail = new OrderDetail();//订单明细
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置关联order id
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //clean shopping cart
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        //return VO object

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal("0.01"), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * get history orders for the user
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status, Long id) {
        //start pageHelper
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO opqDTO = new OrdersPageQueryDTO();
        opqDTO.setStatus(status);
        opqDTO.setUserId(id);

        //page query
        Page<Orders> page = orderMapper.pageQuery(opqDTO);
        List<OrderVO> list = new ArrayList<>();

        //query order details
        if (page != null && page.getTotal() > 0){
            for (Orders order: page) {
                List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(order.getId());

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetail);

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * get order details by id
     * @param orderId
     * @return
     */
    @Override
    public OrderVO orderDetails(Long orderId) {
        OrderVO orderVO = new OrderVO();

        //set order properties
        Orders orders = orderMapper.getById(orderId);
        BeanUtils.copyProperties(orders,orderVO);

        //get details and set detailsList
        List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(orderId);
        orderVO.setOrderDetailList(orderDetail);
        return orderVO;
    }
}
