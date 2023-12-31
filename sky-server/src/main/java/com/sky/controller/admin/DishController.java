package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *insert dish
     * @param dishDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增dish: {}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //Clear cached data by dishDTO
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();
    }

    /**
     * dish page query
     * @param dpqDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dpqDTO){
        log.info("dish page query: {}", dpqDTO);
        PageResult pageResult = dishService.pageQuery(dpqDTO);
        return Result.success(pageResult);
    }

    /**
     * batch delete by ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("dish batch delete: {} ",ids);
        dishService.deleteBatch(ids);

        //Clear all cached data, cuz it will affect more than one category
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * select by id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("get by id: {}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * update dishes
     * @return
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("update dishDTO: {}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //Clear all cached data, cuz this method will affect more than one category
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * status change
     * @param status
     * @param id
     * @return
     */
    @PostMapping("status/{status}")
    public Result statusChange(@PathVariable Integer status, Long id){
        log.info("status changing call, id: {}, status: {}", id, status);
        dishService.statusChange(status, id);

        //Clear all cached data, cuz this method will affect more than one category
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * get by category id
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId){
        log.info("get dishes by category id: {}", categoryId);
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> dishes = dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }

    /**
     * clean all cache
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
