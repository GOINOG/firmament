package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * setmeal service management
 */
public interface SetmealService {
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

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

    /**
     * get by id
     * @param id
     * @return
     */
    SetmealVO getById(Long id);

    /**
     * update setmeal
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);
}
