package com.innowise.userservice.controller;

import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full flow: HTTP -> controller -> service -> repository -> PostgreSQL, with Redis cache (Testcontainers).
 */
@SpringBootTest
abstract class AbstractApiIntegrationTest extends AbstractIntegrationTest {

    protected static final String FUTURE_DATE = LocalDate.now().plusYears(3).toString();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected PaymentCardRepository paymentCardRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    protected MockMvc mockMvc;

    @BeforeEach
    void cleanUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.invalidate();
            }
        });
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected static String userJson(String name, String email) {
        return """
                {"name": "%s", "surname": "Balaganov", "birthDate": "1902-05-15", "email": "%s"}
                """.formatted(name, email);
    }

    protected static String cardJson(String number, Boolean active) {
        String activePart = active == null ? "" : ", \"active\": " + active;
        return """
                {"number": "%s", "holder": "SHURA BALAGANOV", "expirationDate": "%s"%s}
                """.formatted(number, FUTURE_DATE, activePart);
    }

    protected long createUser(String name, String email) throws Exception {
        String body = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(name, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    protected long createCard(long userId, String number, Boolean active) throws Exception {
        String body = mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(number, active)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
