package com.innowise.userservice.config;

import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.innowise.userservice.config.CacheNames.CARDS;
import static com.innowise.userservice.config.CacheNames.CARD_PAGES;
import static com.innowise.userservice.config.CacheNames.USERS;
import static com.innowise.userservice.config.CacheNames.USER_CARDS;
import static com.innowise.userservice.config.CacheNames.USER_PAGES;

/**
 * Put on every method that changes a user or a card: the user DTO contains the cards and the card
 * filters depend on the user name, so the whole aggregate is evicted together.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CacheEvict(value = {USERS, USER_PAGES, CARDS, USER_CARDS, CARD_PAGES}, allEntries = true)
public @interface EvictAllCaches {
}
