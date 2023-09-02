package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    /**
     * get by openid
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * get by id
     * @param id
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);

    /**
     * insert user
     * @param user
     */
    void insert(User user);

    /**
     * get user number by create time
     * @param start
     * @param ending
     * @return
     */
    Integer getByCreateTime(LocalDateTime start, LocalDateTime ending);
}
