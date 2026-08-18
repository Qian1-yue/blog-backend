package com.example.blogbackend.service;

import com.example.blogbackend.dto.LoginRequest;
import com.example.blogbackend.dto.RegisterRequest;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.security.GeneratedToken;
import com.example.blogbackend.vo.LoginResponse;
import com.example.blogbackend.vo.LoginUserResponse;
import com.example.blogbackend.vo.UserResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {

        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String nickname = request.nickname().trim();

        UserEntity existingUser =
                userMapper.selectByUsername(username);

        if (existingUser != null) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "用户名已存在"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setNickname(nickname);
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);

        try {
            int affectedRows = userMapper.insert(user);

            if (affectedRows != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "注册失败"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "用户名已存在"
            );
        }

        return convertToUserResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();

        UserEntity user = userMapper.selectByUsername(username);

        if (user == null) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "用户名或密码错误"
            );
        }

        boolean passwordCorrect = passwordEncoder.matches(request.password(), user.getPassword());

        if (!passwordCorrect) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "用户名或密码错误"
            );
        }

        if(!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "用户已被禁用"
            );
        }

        GeneratedToken generatedToken =
                tokenService.createToken(user.getId(),
                        user.getUsername());

        LoginUserResponse loginUser =
                new LoginUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getNickname()
                );

        return new LoginResponse(
                generatedToken.token(),
                "Bearer",
                generatedToken.expiresInSeconds(),
                loginUser
        );

    }

    public void logout(String token) {
        tokenService.removeToken(token);
    }

    public LoginUserResponse getCurrentUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);

        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "登录用户不存在或已被禁用"
            );
        }

        return new LoginUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname()
        );
    }

    private UserResponse convertToUserResponse(
            UserEntity user) {

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
