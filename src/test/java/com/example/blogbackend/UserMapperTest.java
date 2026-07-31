package com.example.blogbackend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class UserMapperTest {
    private final UserMapper userMapper;

    @Autowired
    UserMapperTest(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Test
    void shouldSelectAllUsers() {
        List<UserEntity> users = userMapper.selectList(null);

        assertNotNull(users);

        users.forEach(user ->
                System.out.println(
                        user.getId() + " " +
                        user.getUsername() + " " +
                        user.getNickname()
                )

        );
    }

    @Test
    void shouldQueryUsersByCondition() {
        LambdaQueryWrapper<UserEntity> wrapper =
                Wrappers.lambdaQuery(UserEntity.class);

        wrapper.eq(UserEntity::getStatus,1)
                .like(UserEntity::getUsername,"zhang")
                .orderByDesc(UserEntity::getCreateTime);

        List<UserEntity> users = userMapper.selectList(wrapper);

        assertNotNull(users);
    }
}
