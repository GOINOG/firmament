package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.MyUserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

   /* *//**
     * weixin login
     *
     * @param userLoginDTO
     * @return
     *//*
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信用户登录: {}", userLoginDTO);
        //wx log in
        User user = userService.wxLogin(userLoginDTO);
        //generate jwt token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO loginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(loginVO);
    }*/

    /**
     * login in
     * in this method, openid is username
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login (@RequestBody MyUserLoginDTO userLoginDTO){
        log.info("用户登录: {}", userLoginDTO);
        User user = userService.login(userLoginDTO);

        //generate jwt token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO loginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(loginVO);
    }

    @PostMapping("/signup")
    public Result signup(@RequestBody User user){
        log.info("用户注册 : {}", user);
        userService.signup(user);
        return Result.success("注册成功!");
    }



}
