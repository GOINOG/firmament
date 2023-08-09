package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * get by user id
     * @param currentId
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{currentId}")
    List<ShoppingCart> getByUserId(Long currentId);

    /**
     * delete by user id
     * @param currentId
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteByUserId(Long currentId);
}
