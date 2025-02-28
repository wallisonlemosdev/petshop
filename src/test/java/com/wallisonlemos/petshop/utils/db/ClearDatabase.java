package com.wallisonlemos.petshop.utils.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.test.context.TestPropertySource;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ClearDatabaseExtension.class)
@TestPropertySource(properties = "spring.flyway.clean-disabled=false")
public @interface ClearDatabase {
}
