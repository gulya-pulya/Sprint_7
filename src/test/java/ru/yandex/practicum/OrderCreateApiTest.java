package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.yandex.practicum.dto.request.OrderCreateRequest;
import ru.yandex.practicum.dto.response.OrderCreateResponse;
import ru.yandex.practicum.util.OrderUtils;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;

@RunWith(Parameterized.class)
public class OrderCreateApiTest {

    private final List<String> colors;

    public OrderCreateApiTest(List<String> colors) {
        this.colors = colors;
    }

    @Parameterized.Parameters
    public static Object[][] getSex() {
        return new Object[][]{
                {List.of("BLACK")},
                {List.of("GREY")},
                {List.of("BLACK, GREY")},
                {Collections.emptyList()},
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Create new order should return ok")
    public void createNewOrderShouldReturnOk() {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(
                "test",
                "test",
                "test address",
                4,
                "+7 800 355 35 35",
                2,
                "2020-06-06",
                "comment",
                colors
        );

        ValidatableResponse response = OrderUtils.createOrder(orderCreateRequest);
        checkSuccessfulOrderCreateResponseFieldsAndStatus(response);

        OrderCreateResponse orderCreateResponse = response
                .extract()
                .as(OrderCreateResponse.class);
        OrderUtils.cancelOrder(orderCreateResponse);
    }

    @Step("Check response fields and status of successfully created order")
    private void checkSuccessfulOrderCreateResponseFieldsAndStatus(ValidatableResponse response) {
        response
                .assertThat()
                .body("track", CoreMatchers.notNullValue())
                .and()
                .statusCode(201);
    }
}
