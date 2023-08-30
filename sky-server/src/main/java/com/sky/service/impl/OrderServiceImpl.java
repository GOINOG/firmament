package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
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
    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;

    /**
     * user place order
     *
     * @param submitDTO
     * @return
     */
    @Override
    //TODO Verify that the delivery range cannot exceed 5miles
    public OrderSubmitVO submitOrder(OrdersSubmitDTO submitDTO) {
        //check addressBook
        AddressBook addressBook = addressBookMapper.getById(submitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //check shopping cart
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.getByUserId(BaseContext.getCurrentId());
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
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
        for (ShoppingCart cart : shoppingCarts) {
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
     *
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
        if (page != null && page.getTotal() > 0) {
            for (Orders order : page) {
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
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderVO orderDetails(Long orderId) {
        OrderVO orderVO = new OrderVO();

        //set order properties
        Orders orders = orderMapper.getById(orderId);
        BeanUtils.copyProperties(orders, orderVO);

        //get details and set detailsList
        List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(orderId);
        orderVO.setOrderDetailList(orderDetail);
        return orderVO;
    }

    /**
     * cancel order
     *
     * @param id
     */
    @Override
    public void cancel(Long id) throws Exception {
        //get order
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //check status
        Integer status = order.getStatus();
        //if status > 2, cannot cancel order by user
        if (status > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //if status == 2, need to refund
        if (status.equals(Orders.TO_BE_CONFIRMED)) {
            weChatPayUtil.refund(
                    order.getNumber(),
                    order.getNumber(),
                    new BigDecimal("0.01"),
                    new BigDecimal("0.01")
            );

            order.setPayStatus(Orders.REFUND);
        }

        //update order
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("user cancel");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * order again
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        //get order details
        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);

        //convert to shopping_cart object
        for (OrderDetail each : list
        ) {
            ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
            if (each.getDishId() != null) {
                shoppingCartDTO.setDishId(each.getDishId());
            } else {
                shoppingCartDTO.setSetmealId(each.getSetmealId());
            }
            shoppingCartDTO.setDishFlavor(each.getDishFlavor());
            //insert into shopping_cart
            shoppingCartService.add(shoppingCartDTO);
        }
    }

    @Override
    //TODO: Learn how PageHelper works
    public PageResult pageQuery4Admin(OrdersPageQueryDTO opqDTO) {
        PageHelper.startPage(opqDTO.getPage(), opqDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(opqDTO);

        //need to get orderDishes
        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            //get each order in page, and build orderDishes
            for (Orders order : page) {
                OrderVO orderVO = new OrderVO();
                String orderDishes = "";
                BeanUtils.copyProperties(order, orderVO);

                // get dish or setmeal name by order_id in order_detail_table
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(order.getId());
                for (OrderDetail detail: orderDetails){
                    orderDishes = MessageFormat.format("{0}{1}*{2}, ", orderDishes, detail.getName(), detail.getNumber());
                }

                orderVO.setOrderDishes(orderDishes.substring(0, orderDishes.length() - 2));
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * Order quantity statistics for each status
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO statisticsVO = new OrderStatisticsVO();
        statisticsVO.setConfirmed(orderMapper.getAmountByStatus(Orders.CONFIRMED));
        statisticsVO.setToBeConfirmed(orderMapper.getAmountByStatus(Orders.TO_BE_CONFIRMED));
        statisticsVO.setDeliveryInProgress(orderMapper.getAmountByStatus(Orders.DELIVERY_IN_PROGRESS));
        return statisticsVO;
    }

    /**
     * confirm order
     * @param ocDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ocDTO) {
        orderMapper.statusChange(ocDTO.getId(), Orders.CONFIRMED);
    }

    /**
     * reject order
     * @param orDTO
     */
    @Override
    public void reject(OrdersRejectionDTO orDTO) throws Exception{
        Orders order = orderMapper.getById(orDTO.getId());

        if (order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //can only reject when status = 2
        if (order.getStatus() != 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //check payStatus and refund
        if (order.getPayStatus().equals(Orders.PAID)){
            /*weChatPayUtil.refund(
                    order.getNumber(),
                    order.getNumber(),
                    new BigDecimal("0.01"),
                    new BigDecimal("0.01")
            );*/
            order.setPayStatus(Orders.REFUND);
        }

        //update order
        order.setRejectionReason(orDTO.getRejectionReason());
        order.setCancelReason(orDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(Orders.CANCELLED);
        orderMapper.update(order);
    }

    /**
     * admin cancel order
     * @param ordersCancelDTO
     * @throws Exception
     */
    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders order = orderMapper.getById(ordersCancelDTO.getId());

        if (order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //支付状态
        Integer payStatus = order.getPayStatus();
        if (payStatus == 1) {
           /* //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    order.getNumber(),
                    order.getNumber(),
                    new BigDecimal("0.01"),
                    new BigDecimal("0.01"));
            log.info("申请退款：{}", refund);*/
            order.setPayStatus(Orders.REFUND);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间

        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * delivery order
     * @param id
     */
    @Override
    public void delivery(Long id) throws Exception{
        Orders order = orderMapper.getById(id);

        if (order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //only delivery when status == 3
        if (!order.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30));
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    /**
     * complete order
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders order = orderMapper.getById(id);

        if (order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //only complete when status == 4
        if (!order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        order.setDeliveryTime(LocalDateTime.now());
        order.setStatus(Orders.COMPLETED);
        orderMapper.update(order);
    }
}
