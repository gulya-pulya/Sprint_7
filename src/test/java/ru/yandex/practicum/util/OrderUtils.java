package ru.yandex.practicum.util;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.yandex.practicum.dto.request.OrderCancelRequest;
import ru.yandex.practicum.dto.request.OrderCreateRequest;
import ru.yandex.practicum.dto.response.OrderCreateResponse;
import ru.yandex.practicum.dto.response.OrderIdResponse;

import java.util.List;

import static io.restassured.RestAssured.given;

public class OrderUtils {

    private static OrderCreateRequest orderCreateRequest = new OrderCreateRequest(
            "test",
            "test",
            "test address",
            4,
            "+7 800 355 35 35",
            2,
            "2020-06-06",
            "comment",
            List.of("BLACK")
    );

    @Step("Create new order")
    public static ValidatableResponse createOrder(OrderCreateRequest orderCreateRequest) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/orders")
                .then();
    }

    @Step("Create plain new order")
    public static OrderCreateResponse createPlainOrder() {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/orders")
                .body()
                .as(OrderCreateResponse.class);
    }

    @Step("Accept order")
    public static void acceptOrder(Long orderId, Long courierId) {
        given()
                .header("Content-type", "application/json")
                .and()
                .queryParam("courierId", courierId)
                .when()
                .put("/api/v1/orders/accept/{orderId}", orderId);
    }

    @Step("Get order")
    public static OrderIdResponse getOrder(OrderCreateResponse orderCreateResponse) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .queryParam("t", orderCreateResponse.getTrack())
                .when()
                .get("/api/v1/orders/track")
                .body()
                .as(OrderIdResponse.class);
    }

    @Step("Cancel order")
    public static void cancelOrder(OrderCreateResponse orderCreateResponse) {
        OrderCancelRequest orderCancelRequest = new OrderCancelRequest(orderCreateResponse.getTrack());
        given()
                .header("Content-type", "application/json")
                .and()
                .body(orderCancelRequest)
                .when()
                .put("/api/v1/orders/cancel");
    }
}
