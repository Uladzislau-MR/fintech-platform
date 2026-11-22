package com.innowise.userservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void createUserReturns201WithFilledBodyTest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Shura", "shura@goldencalf.ru")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Shura"))
                .andExpect(jsonPath("$.surname").value("Balaganov"))
                .andExpect(jsonPath("$.birthDate").value("1902-05-15"))
                .andExpect(jsonPath("$.email").value("shura@goldencalf.ru"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createUserIgnoresIdFromClientTest() throws Exception {
        long existingId = createUser("Shura", "shura@goldencalf.ru");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": %d, "name": "Ostap", "surname": "Bender", "birthDate": "1900-01-01",
                                 "email": "ostap@goldencalf.ru"}
                                """.formatted(existingId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ostap"));

        mockMvc.perform(get("/api/users/{id}", existingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Shura"));
    }

    @Test
    void createUserWithDuplicateEmailReturns409Test() throws Exception {
        createUser("Shura", "shura@goldencalf.ru");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Other", "shura@goldencalf.ru")))
                .andExpect(status().isConflict());
    }

    @Test
    void createUserWithInvalidBodyReturns400Test() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "surname": "Balaganov", "birthDate": "1902-05-15", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserReturnsFieldsAndCardsTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        createCard(userId, "4000123456789010", true);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Shura"))
                .andExpect(jsonPath("$.cards", hasSize(1)))
                .andExpect(jsonPath("$.cards[0].number").value("4000123456789010"))
                .andExpect(jsonPath("$.cards[0].userId").value(userId));
    }

    @Test
    void getUnknownUserReturns404Test() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsersPageFilteredByNameTest() throws Exception {
        createUser("Shura", "shura@goldencalf.ru");
        createUser("Ostap", "ostap@goldencalf.ru");

        mockMvc.perform(get("/api/users").param("name", "shu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Shura"));

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void updateUserPersistsChangesTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Alexander", "shura@goldencalf.ru")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alexander"));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(jsonPath("$.name").value("Alexander"));
    }

    @Test
    void updateUnknownUserReturns404Test() throws Exception {
        mockMvc.perform(put("/api/users/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Alexander", "a@goldencalf.ru")))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeStatusPersistsTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");

        mockMvc.perform(patch("/api/users/{id}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/users/{id}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void changeStatusWithoutBodyValueReturns400Test() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");

        mockMvc.perform(patch("/api/users/{id}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeStatusOfUnknownUserReturns404Test() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/status", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserReturns204AndRemovesCardsTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        long cardId = createCard(userId, "4000123456789010", true);

        // warm up caches so that stale entries would be visible
        mockMvc.perform(get("/api/users/{id}", userId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/cards/{id}", cardId)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", userId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/cards/{id}", cardId)).andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownUserReturns404Test() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
