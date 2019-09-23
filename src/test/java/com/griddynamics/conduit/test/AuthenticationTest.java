package com.griddynamics.conduit.test;


import static com.griddynamics.conduit.helpers.Endpoint.USERS_LOGIN;
import static com.griddynamics.conduit.helpers.RequestSpecificationDetails.APPLICATION_JSON;
import static com.griddynamics.conduit.helpers.StatusCode.CODE_200;

import com.griddynamics.conduit.helpers.Endpoint;
import com.griddynamics.conduit.helpers.TestDataProvider;
import com.griddynamics.conduit.jsons.UserRequest;
import com.griddynamics.conduit.jsonsdtos.UserRequestDto;
import com.griddynamics.conduit.jsonsdtos.UserResponseDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthenticationTest {
  protected int statusCode;
  protected Response response;
  protected UserRequest userBody;
  protected UserRequestDto requestBody;
  protected UserResponseDto userResponseDto;
  protected RequestSpecification requestSpecification;
  protected TestDataProvider testDataProvider = new TestDataProvider();

  @BeforeAll
  static void beforeAll() {
    RestAssured.baseURI = Endpoint.BASE_URI.getEndpoint();
  }


  @Test
  @DisplayName("Authentication - check if user will be logged and then check his ID")
  void logUserAndGetHisId() {
    // GIVEN
    userBody = new UserRequest(testDataProvider.getEmail(), testDataProvider.getPassword());
    UserRequestDto requestBody = new UserRequestDto(userBody);

    requestSpecification = RestAssured.given()
        .contentType(APPLICATION_JSON.getDetail()).body(requestBody);

    // WHEN
    response = requestSpecification.post(USERS_LOGIN.getEndpoint());
    userResponseDto = response.as(UserResponseDto.class);

    // THEN
    MatcherAssert.assertThat("Username is different than expected",
        userResponseDto.user.username, Matchers.equalTo(testDataProvider.getUsername()));
  }

  @Test
  @DisplayName("Authentication - check if user with incorrect password can be log into app")
  void logUserWithIncorrectPassword() {
    // GIVEN
    userBody = new UserRequest(testDataProvider.getEmail(), testDataProvider.getIncorrectPassword());
    requestBody = new UserRequestDto(userBody);

    requestSpecification = RestAssured.given()
        .contentType(APPLICATION_JSON.getDetail()).body(requestBody);

    //WHEN
    response = requestSpecification.post(USERS_LOGIN.getEndpoint());
    statusCode = response.statusCode();

    //THEN
    MatcherAssert.assertThat("Actual status code is not the one we expected",
        statusCode, Matchers.not(CODE_200.getValue()));
  }

}
