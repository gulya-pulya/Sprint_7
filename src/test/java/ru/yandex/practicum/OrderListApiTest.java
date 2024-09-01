package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
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

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Order list must be not empty")
    public void OrderListMustBeNotEmpty() {
        String login = UUID.randomUUID().toString();
        String password = "Qwerty231";
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(login, password, "test");
        CourierLoginRequest courierLoginRequest = new CourierLoginRequest(login, password);

        CourierUtils.createCourier(courierCreateRequest);
        OrderCreateResponse orderCreateResponse = OrderUtils.createPlainOrder();

        CourierLoginResponse courierLoginResponse = CourierUtils.loginCourier(courierLoginRequest);

        OrderIdResponse orderIdResponse = OrderUtils.getOrder(orderCreateResponse);
        OrderUtils.acceptOrder(orderIdResponse.getOrder().getId(), courierLoginResponse.getId());

        ValidatableResponse response = ordersList(courierLoginResponse);
        checkOrderListIsNotEmpty(response);

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
