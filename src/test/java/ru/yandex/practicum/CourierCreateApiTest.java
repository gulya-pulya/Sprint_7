package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.CourierCreateRequest;
import ru.yandex.practicum.util.CourierUtils;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;

public class CourierCreateApiTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Create new courier should return ok")
    public void createNewCourierShouldReturnOk() {
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(UUID.randomUUID().toString(), "Qwerty231", "test");

        ValidatableResponse response = CourierUtils.createCourier(courierCreateRequest);

        checkSuccessfulCourierCreateResponseFieldsAndStatus(response);

        CourierUtils.deleteCourier(courierCreateRequest);
    }

    @Test
    @DisplayName("Create new courier should not allowed create two same couriers")
    public void createNewCourierShouldNotAllowedCreateTwoSameCouriers() {
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(UUID.randomUUID().toString(), "Qwerty231", "test");

        CourierUtils.createCourier(courierCreateRequest);

        ValidatableResponse response = CourierUtils.createCourier(courierCreateRequest);

        checkFailedCourierCreateResponseFieldsAndStatus(response, 409, "Этот логин уже используется. Попробуйте другой.");

        CourierUtils.deleteCourier(courierCreateRequest);
    }

    @Test
    @DisplayName("Create new courier should not allowed create two couriers with same login")
    public void createNewCourierShouldNotAllowedCreateTwoCouriersWithSameLogin() {
        String login = UUID.randomUUID().toString();
        CourierCreateRequest courierCreateRequest1 = new CourierCreateRequest(login, "Qwerty231", "test");
        CourierCreateRequest courierCreateRequest2 = new CourierCreateRequest(login, "TestPassword", "TestName");

        CourierUtils.createCourier(courierCreateRequest1);

        ValidatableResponse response = CourierUtils.createCourier(courierCreateRequest2);

        checkFailedCourierCreateResponseFieldsAndStatus(response, 409, "Этот логин уже используется. Попробуйте другой.");

        CourierUtils.deleteCourier(courierCreateRequest1);
    }

    @Test
    @DisplayName("Create new courier should return error if login field is missing")
    public void createNewCourierShouldReturnErrorIfLoginFieldIsMissing()  {
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(null, "Qwerty231", "test");

        ValidatableResponse response = CourierUtils.createCourier(courierCreateRequest);

        checkFailedCourierCreateResponseFieldsAndStatus(response, 400, "Недостаточно данных для создания учетной записи");
    }

    @Test
    @DisplayName("Create new courier should return error if password field is missing")
    public void createNewCourierShouldReturnErrorIfPasswordFieldIsMissing()  {
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(UUID.randomUUID().toString(), null, "test");

        ValidatableResponse response = CourierUtils.createCourier(courierCreateRequest);

        checkFailedCourierCreateResponseFieldsAndStatus(response, 400, "Недостаточно данных для создания учетной записи");
    }

    @Test
    @DisplayName("Create new courier should return ok if firstName field is missing")
    public void createNewCourierShouldReturnOkIfFirstNameFieldIsMissing()  {
        CourierCreateRequest courierCreateRequest = new CourierCreateRequest(UUID.randomUUID().toString(), "Qwerty231", null);

        checkSuccessfulCourierCreateResponseFieldsAndStatus(CourierUtils.createCourier(courierCreateRequest));

        CourierUtils.deleteCourier(courierCreateRequest);
    }

    @Step("Check response fields and status of successfully created courier")
    private static void checkSuccessfulCourierCreateResponseFieldsAndStatus(ValidatableResponse response) {
        response.assertThat()
                .body("ok", equalTo(true))
                .and()
                .statusCode(201);
    }

    @Step("Check response fields and status of failed created courier")
    private static void checkFailedCourierCreateResponseFieldsAndStatus(ValidatableResponse response, int code, String message) {
        response.assertThat()
                .body("code", equalTo(code))
                .and()
                .body("message", equalTo(message))
                .and()
                .statusCode(code);
    }
}
