package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

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
}
