package com.example.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogbackend.dto.CreateUserRequest;
import com.example.blogbackend.dto.UpdateUserRequest;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.vo.PageResponse;
import com.example.blogbackend.vo.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getUserById(Long id) {
        UserEntity user = userMapper.selectById(id);

        if (user == null) {
            throw new BusinessException(
        HttpStatus.NOT_FOUND,
        "用户不存在"
);
        }
        return convertToResponse(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        String username = request.username().trim();

        UserEntity existingUser = userMapper.selectByUsername(username);

        if (existingUser != null) {
            throw new BusinessException(
        HttpStatus.CONFLICT,
        "用户名已存在"
);
        }

        LocalDateTime now  = LocalDateTime.now();

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname().trim());
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);

        int affectRows = userMapper.insert(user);

        if (affectRows != 1) {
            throw new BusinessException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "用户创建失败"
);
        }
        return convertToResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        UserEntity existingUser = userMapper.selectById(id);

        if (existingUser == null) {
            throw new BusinessException(
        HttpStatus.NOT_FOUND,
        "用户不存在"
);
        }
            UserEntity updateEntity = new UserEntity();
            updateEntity.setId(id);
            updateEntity.setNickname(request.nickname().trim());
            updateEntity.setStatus(request.status());
            updateEntity.setUpdateTime(LocalDateTime.now());

            int affectRows = userMapper.updateById(updateEntity);

            if (affectRows != 1) {
                throw new IllegalStateException("用户修改失败");
            }
            return getUserById(id);
    }
    public void deleteUser(Long id) {
        UserEntity existingEntity = userMapper.selectById(id);

        if (existingEntity == null) {
            throw new BusinessException(
        HttpStatus.NOT_FOUND,
        "用户不存在"
);
        }

        int affectRows = userMapper.deleteById(id);

        if (affectRows != 1) {
            throw new IllegalStateException("用户删除失败");
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

    public PageResponse<UserResponse> listUsers(
            long current,
            long size,
            String username,
            Integer status) {

        Page<UserEntity> page =
                new Page<>(current, size);

        LambdaQueryWrapper<UserEntity> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.like(
                        StringUtils.hasText(username),
                        UserEntity::getUsername,
                        username
                )
                .eq(
                        status != null,
                        UserEntity::getStatus,
                        status
                )
                .orderByDesc(UserEntity::getCreateTime);

        IPage<UserEntity> entityPage =
                userMapper.selectPage(page, wrapper);

        List<UserResponse> records =
                entityPage.getRecords()
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return new PageResponse<>(
                entityPage.getCurrent(),
                entityPage.getSize(),
                entityPage.getTotal(),
                entityPage.getPages(),
                records
        );
    }
}
