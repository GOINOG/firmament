package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

/**
 * setmeal service management
 */
public interface SetmealService {
    /**
     * save with relationship with setmeal and dish
     * @param setmealDTO
     * @return
     */
    void saveWithSetmealDish(SetmealDTO setmealDTO);

    /**
     * page query
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * status change
     * @param status
     * @param id
     */
    void statusChange(Integer status, Long id);

    /**
     * batch delete by ids
     * @param ids
     * @return
     */
    void batchDelete(List<Long> ids);
}
