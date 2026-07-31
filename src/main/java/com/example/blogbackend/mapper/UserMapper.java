package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("""
        SELECT  id,
                username,
                password,
                nickname,
                status,
                create_time,
                update_time
        FROM blog_user
        WHERE username = #{username}
        LIMIT 1
    """)
    UserEntity selectByUsername(@Param("username") String username);

    @Select("""
            SELECT COUNT(*)
            FROM blog_user
            WHERE status = #{status}
            """)
    Long countByStatus(@Param("status") Integer status);
}
