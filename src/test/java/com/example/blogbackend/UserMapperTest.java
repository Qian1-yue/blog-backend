package com.example.blogbackend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(TestContainersConfiguration.class)
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
