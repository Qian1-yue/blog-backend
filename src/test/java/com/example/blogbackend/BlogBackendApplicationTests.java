package com.example.blogbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class BlogBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
