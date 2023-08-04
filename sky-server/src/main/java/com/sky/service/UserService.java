package com.sky.service;

import com.sky.dto.MyUserLoginDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    /**
     * wx login
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * signup
     * @param user
     */
    void signup(User user);

    /**
     * my login
     * @param userLoginDTO
     * @return
     */
    User login(MyUserLoginDTO userLoginDTO);
}
