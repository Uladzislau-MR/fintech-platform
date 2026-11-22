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

class PaymentCardControllerIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void createCardReturns201WithIdAndOwnerTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4000123456789010", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.number").value("4000123456789010"))
                .andExpect(jsonPath("$.holder").value("SHURA BALAGANOV"))
                .andExpect(jsonPath("$.expirationDate").value(FUTURE_DATE))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createCardForUnknownUserReturns404Test() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/cards", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4000123456789010", true)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCardWithExpiredDateReturns400Test() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number": "4000123456789010", "holder": "SHURA", "expirationDate": "2020-01-31"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sixthActiveCardReturns422Test() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        for (int i = 1; i <= 5; i++) {
            createCard(userId, "400012345678901" + i, true);
        }

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4000123456789099", true)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void inactiveCardsDoNotCountTowardsLimitTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        for (int i = 1; i <= 5; i++) {
            createCard(userId, "400012345678901" + i, true);
        }

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4000123456789099", false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void activatingSixthCardReturns422Test() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        for (int i = 1; i <= 5; i++) {
            createCard(userId, "400012345678901" + i, true);
        }
        long inactiveCardId = createCard(userId, "4000123456789099", false);

        mockMvc.perform(patch("/api/cards/{id}/activate", inactiveCardId))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getCardsOfUserAndCardByIdDoNotCollideTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        long cardId = createCard(userId, "4000123456789010", true);

        // both endpoints are called with the same numeric value on purpose
        mockMvc.perform(get("/api/cards/{id}", cardId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/{userId}/cards", cardId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/{userId}/cards", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(cardId));
        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));
    }

    @Test
    void getUnknownCardReturns404Test() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCardKeepsOwnerTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        long cardId = createCard(userId, "4000123456789010", true);

        mockMvc.perform(put("/api/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4000123456789999", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("4000123456789999"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(jsonPath("$.number").value("4000123456789999"))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void deactivateAndActivateCardPersistsTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        long cardId = createCard(userId, "4000123456789010", true);

        mockMvc.perform(patch("/api/cards/{id}/deactivate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/cards/{id}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deleteCardReturns204AndUpdatesUserTest() throws Exception {
        long userId = createUser("Shura", "shura@goldencalf.ru");
        long cardId = createCard(userId, "4000123456789010", true);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(jsonPath("$.cards", hasSize(1)));

        mockMvc.perform(delete("/api/cards/{id}", cardId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", cardId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(jsonPath("$.cards", hasSize(0)));
    }

    @Test
    void deleteUnknownCardReturns404Test() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
