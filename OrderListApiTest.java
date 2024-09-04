package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.CourierCreateRequest;
import ru.yandex.practicum.dto.request.CourierLoginRequest;
import ru.yandex.practicum.dto.response.CourierLoginResponse;
import ru.yandex.practicum.dto.response.OrderCreateResponse;
import ru.yandex.practicum.dto.response.OrderIdResponse;
import ru.yandex.practicum.util.CourierUtils;
import ru.yandex.practicum.util.OrderUtils;

import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class OrderListApiTest {

    private CourierCreateRequest courierCreateRequest;
    private OrderCreateResponse orderCreateResponse;
    private CourierLoginRequest courierLoginRequest;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        String login = UUID.randomUUID().toString();
        String password = "Qwerty231";
        this.courierLoginRequest = new CourierLoginRequest(login, password);
        this.courierCreateRequest = new CourierCreateRequest(login, password, "test");
        this.orderCreateResponse = OrderUtils.createPlainOrder();
    }

    @Test
    @DisplayName("Order list must be not empty")
    public void orderListMustBeNotEmpty() {
        CourierUtils.createCourier(courierCreateRequest);

        CourierLoginResponse courierLoginResponse = CourierUtils.loginCourier(courierLoginRequest);

        OrderIdResponse orderIdResponse = OrderUtils.getOrder(orderCreateResponse);
        OrderUtils.acceptOrder(orderIdResponse.getOrder().getId(), courierLoginResponse.getId());

        ValidatableResponse response = ordersList(courierLoginResponse);
        checkOrderListIsNotEmpty(response);
    }

    @After
    public void clean() {
        CourierUtils.deleteCourier(courierCreateRequest);
        OrderUtils.cancelOrder(orderCreateResponse);
    }

    @Step("check order list is not empty")
    private void checkOrderListIsNotEmpty(ValidatableResponse response) {
        response
                .assertThat()
                .body("orders", CoreMatchers.not(Collections.emptyList()))
                .and()
                .statusCode(200);
    }


    @Step("Get orders list")
    private ValidatableResponse ordersList(CourierLoginResponse courierLoginResponse) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .queryParam("courierId", courierLoginResponse.getId())
                .when()
                .get("/api/v1/orders")
                .then();
    }
}
