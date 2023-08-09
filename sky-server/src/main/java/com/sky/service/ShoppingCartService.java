package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import org.springframework.stereotype.Service;

@Service
public interface ShoppingCartService {
    /**
     * add dishes to shopping cart
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);
}
