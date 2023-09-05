package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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
     *
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
        Integer totalOrderCount = orderMapper.getCountByOrderTimeAndStatus(null, LocalDateTime.now(), null);
        totalOrderCount = totalOrderCount == null ? 0 : totalOrderCount;

        //get total order count
        Integer validTotalOrderCount = orderMapper.getCountByOrderTimeAndStatus(null, LocalDateTime.now(), Orders.COMPLETED);
        validTotalOrderCount = validTotalOrderCount == null ? 0 : validTotalOrderCount;

        //get order Completion Rate
        double orderCompletionRate = -1;
        if (totalOrderCount != 0) {
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
     *
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
        for (GoodsSalesDTO each : top10List) {
            nameList.append(each.getName()).append(",");
            numberList.append(each.getNumber()).append(",");
        }

        String nameString = nameList.substring(0, nameList.length() - 1);
        String numberString = numberList.substring(0, numberList.length() - 1);

        return SalesTop10ReportVO.builder()
                .nameList(nameString)
                .numberList(numberString)
                .build();
    }

    /**
     * export business data
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1.query database to obtain business data
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        //2.export data to an EXCEL file by POI

        //get inputStream from template/{file name}
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/Business_Data_Report_Template.xlsx");

        try {
            //create an excel file based on template file
            XSSFWorkbook excel = new XSSFWorkbook(is);

            //get sheet
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //fill date and time at row#2
            sheet.getRow(1).getCell(1).setCellValue("from " + begin + " to " + end);

            //fill overview business data
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //fill detailed business data from row#7 to row#37
            for (int i = 0; i < 30; i++) {
                LocalDate day = begin.plusDays(i);

                LocalDateTime start = LocalDateTime.of(day, LocalTime.MIN);
                LocalDateTime ending = LocalDateTime.of(day, LocalTime.MAX);
                BusinessDataVO eachDay = workspaceService.getBusinessData(start, ending);

                row = sheet.getRow(i + 7);
                row.getCell(1).setCellValue(String.valueOf(day));
                row.getCell(2).setCellValue(eachDay.getTurnover());
                row.getCell(3).setCellValue(eachDay.getValidOrderCount());
                row.getCell(4).setCellValue(eachDay.getOrderCompletionRate());
                row.getCell(5).setCellValue(eachDay.getUnitPrice());
                row.getCell(6).setCellValue(eachDay.getNewUsers());
            }

            //3. download EXCEL to client's browser through outputStream object
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //4.close resource
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
