package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * add into shopping cart
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {

        //判断当前商品是否存在
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .dishId(shoppingCartDTO.getDishId())
                .setmealId(shoppingCartDTO.getSetmealId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果购物车中存在, amount + 1
        if (list != null && list.size()>0){
            ShoppingCart data = list.get(0);
            Integer number = data.getNumber();
            data.setNumber(number + 1);
            shoppingCartMapper.increaseNumber(data);
        }else {
            //不存在,insert
            // 判断是dish还是setmeal
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null){//dish
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
            }else {//setmeal
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }
}
