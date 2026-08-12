package com.example.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogbackend.dto.CreateCommentRequest;
import com.example.blogbackend.entity.ArticleEntity;
import com.example.blogbackend.entity.CommentEntity;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.mapper.ArticleMapper;
import com.example.blogbackend.mapper.CommentMapper;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.vo.CommentResponse;
import com.example.blogbackend.vo.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public CommentService(CommentMapper commentMapper, ArticleMapper articleMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    public CommentResponse createComment(
            Long articleId,
            Long currentUserId,
            CreateCommentRequest request) {

        checkPublishedArticle(articleId);

        CommentEntity comment = new CommentEntity();

        comment.setArticleId(articleId);
        comment.setContent(
                request.content().trim()
        );
        comment.setUserId(currentUserId);
        comment.setStatus(1);
        comment.setCreateTime(
                LocalDateTime.now()
        );

        int affectRows = commentMapper.insert(comment);

        if (affectRows != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "评论发布失败"
            );
        }

        UserEntity user = userMapper.selectById(currentUserId);

        String nickname = user == null
                ? null
                : user.getNickname();

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUserId(),
                nickname,
                comment.getCreateTime()
        );
    }

    public PageResponse<CommentResponse> listComments(
            Long articleId,
            long current,
            long size) {
        checkPublishedArticle(articleId);

        Page<CommentEntity> page = new Page<>(current, size);

        LambdaQueryWrapper<CommentEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper
                .eq(
                        CommentEntity::getArticleId, articleId)
                .eq(CommentEntity::getStatus, 1)
                .orderByAsc(CommentEntity::getCreateTime);

        Page<CommentEntity> resultPage = commentMapper.selectPage(page, wrapper);
        Map<Long,UserEntity> userMap =
                queryCommentUsers(resultPage.getRecords());

        List<CommentResponse> records =
                resultPage.getRecords()
                        .stream()
                        .map(comment -> {
                            UserEntity user = userMap.get(comment.getUserId());

                            String nickname =
                                    user == null
                                    ?"未知用户" : user.getNickname();

                            return new CommentResponse(
                                    comment.getId(),
                                    comment.getContent(),
                                    comment.getUserId(),
                                    nickname,
                                    comment.getCreateTime()
                            );
                        })
                        .toList();

        return new PageResponse<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal(),
                resultPage.getPages(),
                records
        );
    }

    private void checkPublishedArticle(
            Long articleId) {
        ArticleEntity article =
                articleMapper.selectById(articleId);

        if(article==null
                || !Integer.valueOf(1)
                .equals(article.getStatus())) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "文章不存在"
            );
        }
    }
    private Map<Long,UserEntity> queryCommentUsers(List<CommentEntity> comments) {
        if(comments.isEmpty()){
            return Collections.emptyMap();
        }

        Set<Long> userIds = comments
                .stream()
                .map(CommentEntity::getUserId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper
                .select(UserEntity::getId, UserEntity::getNickname)
                .in(UserEntity::getId, userIds);

        return userMapper
                .selectList(wrapper)
                .stream()
                .collect(Collectors.toMap(
                        UserEntity::getId,
                        Function.identity()
                ));
    }
}

