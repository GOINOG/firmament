package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * get by ShoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * increase number
     * @param data
     */
    void increaseNumber(ShoppingCart data);

    /**
     * insert new dishes or setmeal
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart  (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time)" +
            "values (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);
}
