package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * setmeal management
 */
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * save with relationship with setmeal and dish
     *
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("save setmeal:{}", setmealDTO);
        setmealService.saveWithSetmealDish(setmealDTO);
        return Result.success();
    }

    /**
     * page query
     *
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("setmeal page query: {}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * status change
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result statusChange(@PathVariable Integer status, Long id) {
        log.info("setmeal change status: {}, id: {}", status, id);
        setmealService.statusChange(status, id);
        return Result.success();
    }

    /**
     * batch delete by ids
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result batchDeletion(@RequestParam List<Long> ids) {
        log.info("batch delete : {}", ids);
        setmealService.batchDelete(ids);
        return Result.success();
    }

    /**
     * get setmeal by id
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("get by id:{}", id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    /**
     * update setmeal
     *
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("update setmeal: {}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
