package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * 
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    @Select("select * from user where username = #{username}")
    User getByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone
     * @return
     */
    @Select("select * from user where phone = #{phone}")
    User getByPhone(String phone);

    /**
     * 更新用户信息
     *
     * @param user
     */
    void update(User user);

    /**
     * 插入数据
     * 
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    /**
     * 根据动态条件统计用户数量
     * 
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 根据用户名或手机号查询用户
     * 
     * @param username
     * @param phone
     * @return
     */
    @Select("select * from user where username = #{username} or phone = #{phone}")
    User getByUsernameOrPhone(String username, String phone);
}
