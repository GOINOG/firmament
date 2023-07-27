package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    /**
     *insert dish and corresponding flavor
     * @param dishDTO
     * @return
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * dish page query
     * @param dpqDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dpqDTO);

    /**
     * dish batch delete by ids
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
