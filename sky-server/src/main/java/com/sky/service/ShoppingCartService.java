package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingCartService {
    /**
     * add dishes to shopping cart
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * get shopping cart list by userid
     * @return
     */
    List<ShoppingCart> list();

    /**
     * clean shopping cart
     */
    void clean();
}
