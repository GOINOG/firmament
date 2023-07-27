package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * get setmeal id by dish ids
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdByDishIds(List<Long> dishIds);
}
