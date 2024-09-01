package ru.yandex.practicum.util;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.yandex.practicum.dto.request.CourierCreateRequest;
import ru.yandex.practicum.dto.request.CourierLoginRequest;
import ru.yandex.practicum.dto.response.CourierLoginResponse;

import static io.restassured.RestAssured.given;

public class CourierUtils {

    @Step("Create new courier")
    public static ValidatableResponse createCourier(CourierCreateRequest courierCreateRequest) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(courierCreateRequest)
                .when()
                .post("/api/v1/courier")
                .then();
    }

    @Step("Delete created courier")
    public static void deleteCourier(CourierCreateRequest courierCreateRequest) {
        CourierLoginRequest loginCourierDto = new CourierLoginRequest(courierCreateRequest.getLogin(), courierCreateRequest.getPassword());
        CourierLoginResponse courierLoginResponse = loginCourier(loginCourierDto);

        given()
                .header("Content-type", "application/json")
                .and()
                .body(loginCourierDto)
                .when()
                .delete("/api/v1/courier/{id}", courierLoginResponse.getId());
    }

    @Step("Login courier to receive id")
    public static CourierLoginResponse loginCourier(CourierLoginRequest loginCourierDto) {
        CourierLoginResponse courierLoginResponse = given()
                .header("Content-type", "application/json")
                .and()
                .body(loginCourierDto)
                .when()
                .post("/api/v1/courier/login")
                .body()
                .as(CourierLoginResponse.class);
        return courierLoginResponse;
    }
}
