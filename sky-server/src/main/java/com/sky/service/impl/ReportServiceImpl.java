package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * get turnover statistics
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //get date list
        List<LocalDate> dateList = new ArrayList<>();

        int daysDiff = (int) ChronoUnit.DAYS.between(begin, end);
        for (int i = 0; i <= daysDiff; i++) {
            dateList.add(begin.plusDays(i));
        }

        String dateString = StringUtils.join(dateList, ",");

        //get turnover list
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime ending = LocalDateTime.of(date, LocalTime.MAX);

            Double turnover = orderMapper.getSumByOrderTimeAndStatus(start, ending, Orders.COMPLETED);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }

        String turnoverString = StringUtils.join(turnoverList, ",");

        //boxing and return
        return TurnoverReportVO.builder()
                .dateList(dateString)
                .turnoverList(turnoverString)
                .build();
    }

    /**
     * get user statistics
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserReport(LocalDate begin, LocalDate end) {
        //get date list
        List<LocalDate> dateList = new ArrayList<>();

        int daysDiff = (int) ChronoUnit.DAYS.between(begin, end);
        for (int i = 0; i <= daysDiff; i++) {
            dateList.add(begin.plusDays(i));
        }

        String dateString = StringUtils.join(dateList, ",");

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime ending = LocalDateTime.of(date, LocalTime.MAX);

            //get total user list
            Integer totalUser = userMapper.getByCreateTime(null, ending);
            totalUserList.add(totalUser == null ? 0 : totalUser);

            //get new user list
            Integer newUser = userMapper.getByCreateTime(start, ending);
            newUserList.add(newUser == null ? 0 : newUser);
        }

        String totalUserString = StringUtils.join(totalUserList, ",");
        String newUserString = StringUtils.join(newUserList, ",");

        return UserReportVO.builder()
                .dateList(dateString)
                .totalUserList(totalUserString)
                .newUserList(newUserString)
                .build();
    }

    /**
     * get order statistics
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderReport(LocalDate begin, LocalDate end) {
        //get date list
        List<LocalDate> dateList = new ArrayList<>();

        int daysDiff = (int) ChronoUnit.DAYS.between(begin, end);
        for (int i = 0; i <= daysDiff; i++) {
            dateList.add(begin.plusDays(i));
        }

        String dateString = StringUtils.join(dateList, ",");

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime ending = LocalDateTime.of(date, LocalTime.MAX);

            //get order count list for each day
            Integer orderCount = orderMapper.getCountByOrderTimeAndStatus(start, ending, null);
            orderCountList.add(orderCount == null ? 0 : orderCount);

            //get valid order count list each day
            Integer validOrderCount = orderMapper.getCountByOrderTimeAndStatus(start, ending, Orders.COMPLETED);
            validOrderCountList.add(validOrderCount == null ? 0 : validOrderCount);

        }

        String orderCountString = StringUtils.join(orderCountList, ",");
        String validOrderCountString = StringUtils.join(validOrderCountList, ",");


        //get total order count
        Integer totalOrderCount = orderMapper.getCountByOrderTimeAndStatus(null, LocalDateTime.now(),null);
        totalOrderCount = totalOrderCount == null? 0: totalOrderCount;

        //get total order count
        Integer validTotalOrderCount = orderMapper.getCountByOrderTimeAndStatus(null, LocalDateTime.now(),Orders.COMPLETED);
        validTotalOrderCount = validTotalOrderCount == null? 0: validTotalOrderCount;

        //get order Completion Rate
        double orderCompletionRate = -1;
        if (totalOrderCount != 0){
            orderCompletionRate = (double) validTotalOrderCount / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(dateString)
                .orderCountList(orderCountString)
                .validOrderCountList(validOrderCountString)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validTotalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * get top10 goods
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getGoodsReport(LocalDate begin, LocalDate end) {

        LocalDateTime start = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime ending = LocalDateTime.of(end, LocalTime.MAX);

        //get valid order id
        List<BigInteger> ids = orderMapper.getValidIdByCreateTime(start, ending);

        //get top10 details
        StringBuilder nameList = new StringBuilder();
        StringBuilder numberList = new StringBuilder();
        List<GoodsSalesDTO> top10List = orderDetailMapper.getSumByNameGroupAndIds(ids);
        for (GoodsSalesDTO each: top10List) {
            nameList.append(each.getName()).append(",");
            numberList.append(each.getNumber()).append(",");
        }

        String nameString = nameList.substring(0, nameList.length()-1);
        String numberString = numberList.substring(0, numberList.length()-1);

        return SalesTop10ReportVO.builder()
                .nameList(nameString)
                .numberList(numberString)
                .build();
    }
}
