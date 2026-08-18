package com.example.blogbackend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure
        .AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
@Transactional
class BlogApiIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    BlogApiIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldRejectUnauthenticatedArticleCreation()
            throws Exception {

        mockMvc.perform(
                        post("/api/articles")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "title": "测试文章",
                                          "summary": "测试摘要",
                                          "content": "测试正文"
                                        }
                                        """)
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.code").value(401)
                );
    }

    @Test
    void shouldRejectUnauthenticatedMyArticlesRequest()
            throws Exception {
        mockMvc.perform(
                        get("/api/articles/mine")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldRejectUnauthenticatedCurrentUserRequest()
            throws Exception {
        mockMvc.perform(
                        get("/api/auth/me")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldRejectInvalidPagination()
            throws Exception {

        mockMvc.perform(
                        get("/api/articles")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code").value(400)
                );
    }

    @Test
    void shouldReturn404ForMissingArticle()
            throws Exception {

        mockMvc.perform(
                        get("/api/articles/999999999")
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.code").value(404)
                );
    }

    @Test
    void shouldProtectArticleOwnershipAndRevokeDisabledUserToken()
            throws Exception {
        Session firstUser = registerAndLogin(
                "owner_user",
                "Owner123!",
                "文章作者"
        );

        String createResponse = mockMvc.perform(
                        post("/api/articles")
                                .header("Authorization", bearer(firstUser.token()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "权限测试文章",
                                          "summary": "验证文章所有权",
                                          "content": "只有作者可以修改"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.authorId").value(firstUser.userId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number articleId = JsonPath.read(createResponse, "$.data.id");
        Session secondUser = registerAndLogin(
                "other_user",
                "Other123!",
                "其他用户"
        );

        mockMvc.perform(
                        put("/api/articles/{id}", articleId.longValue())
                                .header("Authorization", bearer(secondUser.token()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "越权修改",
                                          "summary": "不应该成功",
                                          "content": "不应该成功"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(
                        delete("/api/users/{id}", firstUser.userId())
                                .header("Authorization", bearer(firstUser.token()))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/auth/me")
                                .header("Authorization", bearer(firstUser.token()))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    private Session registerAndLogin(
            String username,
            String password,
            String nickname) throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "%s",
                                          "nickname": "%s"
                                        }
                                        """.formatted(username, password, nickname))
                )
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(username, password))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(loginResponse, "$.data.token");
        Number userId = JsonPath.read(loginResponse, "$.data.user.id");
        return new Session(token, userId.longValue());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Session(String token, long userId) {
    }
}
