package org.nkcoder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nkcoder.config.JpaAuditingConfig;
import org.nkcoder.user.TestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import(TestConfig.class)
@ImportAutoConfiguration(exclude = {JpaAuditingConfig.class})
public class BaseControllerTest {
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
