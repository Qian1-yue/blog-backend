package com.example.blogbackend.entity;

public class UserEntity {
    private Long id;
    private String username;
    private String nickname;

    public UserEntity(Long id, String username, String nickname) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }
}

