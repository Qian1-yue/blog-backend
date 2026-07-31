package com.example.blogbackend.vo;

import java.util.List;

public record PageResponse<T> (
        long current,
        long size,
        long total,
        long pages,
        List<T> records
        ){

}
