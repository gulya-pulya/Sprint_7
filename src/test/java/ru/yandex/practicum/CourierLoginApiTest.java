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
import ru.yandex.practicum.util.CourierUtils;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CourierLoginApiTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Login courier should return ok")
    public void loginCourierShouldReturnOk() {
        String login = UUID.randomUUID().toString();
        String password = "Qwerty231";
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(login, password, "test");
        CourierLoginRequest courierLoginRequest = new CourierLoginRequest(login, password);

        CourierUtils.createCourier(courierCreateRequest);

        ValidatableResponse response = tryToLogin(courierLoginRequest);
        checkSuccessfulCourierLoginResponseFieldsAndStatus(response);

        CourierUtils.deleteCourier(courierCreateRequest);
    }

    @Test
    @DisplayName("Login courier should return error if login field not found")
    public void loginCourierShouldReturnErrorIfLoginFieldNotFound() {
        String password = "Qwerty231";
        CourierLoginRequest courierLoginRequest = new CourierLoginRequest(null, password);

        ValidatableResponse response = tryToLogin(courierLoginRequest);

        checkFailedCourierLoginResponseFieldsAndStatus(response, 400, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Login courier should return error if courier password don't match")
    public void loginCourierShouldReturnErrorIfCourierPasswordDontMatch() {
        String login = UUID.randomUUID().toString();
        String password = "Qwerty231";
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(login, password, "test");
        CourierLoginRequest courierLoginRequest = new CourierLoginRequest(login, "test");

        CourierUtils.createCourier(courierCreateRequest);

        ValidatableResponse response = tryToLogin(courierLoginRequest);

        checkFailedCourierLoginResponseFieldsAndStatus(response, 404, "Учетная запись не найдена");

        CourierUtils.deleteCourier(courierCreateRequest);
    }

    @Test
    @DisplayName("Login courier should return error if courier don't exists")
    public void loginCourierShouldReturnErrorIfCourierDontExists() {
        String login = UUID.randomUUID().toString();
        String password = "Qwerty231";
        CourierLoginRequest courierLoginRequest = new CourierLoginRequest(login, password);

        ValidatableResponse response = tryToLogin(courierLoginRequest);

        checkFailedCourierLoginResponseFieldsAndStatus(response, 404, "Учетная запись не найдена");
    }

    @Step("Try to login")
    private ValidatableResponse tryToLogin(CourierLoginRequest courierLoginRequest) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(courierLoginRequest)
                .when()
                .post("/api/v1/courier/login")
                .then();
    }

    @Step("Check response fields and status of successfully courier login")
    private static void checkSuccessfulCourierLoginResponseFieldsAndStatus(ValidatableResponse response) {
        response.assertThat()
                .body("id", CoreMatchers.notNullValue())
                .and()
                .statusCode(200);
    }

    @Step("Check response fields and status of failed courier login")
    private static void checkFailedCourierLoginResponseFieldsAndStatus(ValidatableResponse response, int code, String message) {
        response.assertThat()
                .body("code", equalTo(code))
                .and()
                .body("message", equalTo(message))
                .and()
                .statusCode(code);
    }
}
