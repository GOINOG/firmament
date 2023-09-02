package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * get turnover statistics
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //get data list
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

            Double turnover = orderMapper.getSumByOrderTimeAndStatus(start, ending, 5);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }
        String turnoverString = StringUtils.join(turnoverList, ",");

        //boxing and return
        return TurnoverReportVO.builder()
                .dateList(dateString)
                .turnoverList(turnoverString)
                .build();
    }
}
