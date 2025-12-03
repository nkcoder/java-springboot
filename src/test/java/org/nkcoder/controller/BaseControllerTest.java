package org.nkcoder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nkcoder.config.JpaAuditingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/** Base class for controller slice tests. */
@ImportAutoConfiguration(exclude = {JpaAuditingConfig.class})
@WebMvcTest
public class BaseControllerTest {
  @Autowired protected ObjectMapper objectMapper;

  @Autowired protected MockMvc mockMvc;

  protected String toJson(Object object) throws Exception {
    return objectMapper.writeValueAsString(object);
  }
}
