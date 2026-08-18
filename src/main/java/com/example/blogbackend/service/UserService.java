package com.example.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.blogbackend.dto.UpdateUserRequest;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.vo.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserResponse getUserById(Long id) {
        UserEntity user = userMapper.selectById(id);

        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "用户不存在"
            );
        }
        return convertToResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        LambdaUpdateWrapper<UserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserEntity::getId, id)
                .eq(UserEntity::getStatus, 1)
                .set(UserEntity::getNickname, request.nickname().trim())
                .set(UserEntity::getUpdateTime, LocalDateTime.now());

        if (userMapper.update(null, wrapper) != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        return getUserById(id);
    }

    public void disableUser(Long id) {
        LambdaUpdateWrapper<UserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserEntity::getId, id)
                .eq(UserEntity::getStatus, 1)
                .set(UserEntity::getStatus, 0)
                .set(UserEntity::getUpdateTime, LocalDateTime.now());

        if (userMapper.update(null, wrapper) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户状态已发生变化");
        }
    }

    private UserResponse convertToResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

}
