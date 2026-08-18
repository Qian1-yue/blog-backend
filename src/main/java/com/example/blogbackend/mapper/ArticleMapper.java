package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.ArticleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<ArticleEntity> {

    @Update("""
            UPDATE  blog_article
            SET view_count = view_count + 1
            WHERE id = #{id}
              AND status = 1
            """)

    int incrementViewCount(@Param("id") Long id);

    @Select("SELECT view_count FROM blog_article WHERE id = #{id} AND status = 1")
    Long selectPublishedViewCount(@Param("id") Long id);
}
