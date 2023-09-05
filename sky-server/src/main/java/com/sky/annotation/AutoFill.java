package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * customize annotation
 */
@Target(ElementType.METHOD)//只能加在方法上
@Retention(RetentionPolicy.RUNTIME)
//TODO customize annotation 知识点
public @interface AutoFill {
    //数据库操作类型： UPDATA, INSERT
    OperationType value();
}
