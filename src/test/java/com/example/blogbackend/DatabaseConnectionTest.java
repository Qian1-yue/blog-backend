package com.example.blogbackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DatabaseConnectionTest {
    private final DataSource dataSource;

    @Autowired
    DatabaseConnectionTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Test
    void shouldConnectToDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {

            assertTrue(connection.isValid(2));

            System.out.println(
                    "数据库连接成功：" +
                            connection.getMetaData().getURL()
            );
        }
    }
}
