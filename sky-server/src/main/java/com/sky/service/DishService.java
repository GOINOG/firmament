package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {
    /**
     *insert dish and corresponding flavor
     * @param dishDTO
     * @return
     */
    void saveWithFlavor(DishDTO dishDTO);
}
