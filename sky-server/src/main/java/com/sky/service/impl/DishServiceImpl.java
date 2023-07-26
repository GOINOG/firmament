package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;
    /**
     *insert dish and corresponding flavor
     * @param dishDTO
     * @return
     */
    @Override
    @Transactional//需要操作两张表
    public void saveWithFlavor(DishDTO dishDTO) {

        //向dish_table插入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        //获取生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向flavor_table插入n条数据
            dishFlavorsMapper.insertBatch(flavors);
        }
    }

    /**
     * dish page query
     * @param dpqDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dpqDTO) {
        PageHelper.startPage(dpqDTO.getPage(), dpqDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dpqDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
