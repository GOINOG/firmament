package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

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

    /**
     * get dishes By Id With Flavor
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * update dishes With Flavor
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * change status by id
     * @param status
     * @param id
     */
    void statusChange(Integer status, Long id);

    /**
     * get by category id
     * @param categoryId
     * @return
     */
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
