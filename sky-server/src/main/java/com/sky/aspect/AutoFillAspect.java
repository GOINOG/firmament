package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面, 实现公共填充
 */
@Aspect
@Component
@Slf4j
//TODO springboot AOP原理
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointerCut(){}

    /**
     * before advice，为公共字段赋值
     */
    @Before("autoFillPointerCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        //获取当前被拦截的方法的数据库类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        //获取当前方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return;
        }

        Object o = args[0];//约定第一个arg是需要的实体对象,可以修改

        //准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //对公共字段进行赋值修改
        if (operationType == OperationType.INSERT){
            //为四个字段赋值
            try {
                Method setCreateTime = o.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, now.getClass());
                Method setCreateUser = o.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, currentId.getClass());
                Method setUpdateUser = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, currentId.getClass());
                Method setUpdateTime = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, now.getClass());

                //赋值
                setCreateTime.invoke(o, now);
                setCreateUser.invoke(o, currentId);
                setUpdateTime.invoke(o, now);
                setUpdateUser.invoke(o, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE){
            //为两个字段赋值
            try {
                Method setUpdateUser = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, currentId.getClass());
                Method setUpdateTime = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, now.getClass());

                //赋值
                setUpdateTime.invoke(o, now);
                setUpdateUser.invoke(o, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
