package com.griddynamics.conduit.test;

import static com.griddynamics.conduit.helpers.Endpoint.USERS;
import static com.griddynamics.conduit.helpers.RequestSpecificationDetails.APPLICATION_JSON;
import static com.griddynamics.conduit.helpers.StatusCode.CODE_200;

import com.griddynamics.conduit.helpers.Endpoint;
import com.griddynamics.conduit.helpers.TestDataProvider;
import com.griddynamics.conduit.jsons.RegistrationRequestUser;
import com.griddynamics.conduit.jsonsdtos.RegistrationRequestUserDto;
import com.griddynamics.conduit.jsonsdtos.UnprocessableEntityErrorDto;
import com.griddynamics.conduit.jsonsdtos.UserResponseDto;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RegistrationTest {
  private int statusCode;
  private UserResponseDto responseBody;
  private UnprocessableEntityErrorDto errorBody;
  private RegistrationRequestUserDto requestBody;
  private RequestSpecification requestSpecification;
  private TestDataProvider testDataProvider = new TestDataProvider();

  @BeforeAll
  static void beforeAll() {
    RestAssured.baseURI = Endpoint.BASE_URI.getEndpoint();
  }

  @Test
  @DisplayName("Register user with all fields valid, check if username match")
  void userWithValidData() {

    //todo: should I add logger and log test data to external file?!
    //todo: should I sent request with random fields in json file?!
    //todo: I'm checking one thing at the time: max username, min username, max pass, min pass
    // - should I mix those so I will test max username with min pass at the same time?
    // should I combine those in one TC?

    //todo: single test per single user (value) or multiple users (values) in loop?!
    for (RegistrationRequestUser user : testDataProvider.getValidUsers()) {
      // GIVEN
      prepareRequestBody(user);

      // WHEN
      responseBody = requestSpecification.post(USERS.getEndpoint()).as(UserResponseDto.class);

      // THEN
      MatcherAssert.assertThat(
          "Expected username is different than actual",
          responseBody.user.username,
          Matchers.equalTo(user.username));
    }
  }

  @Test
  @DisplayName("Register user with username which is already taken, check error message")
  void userWithIncorrectData() {
    // GIVEN
    prepareRequestBody(testDataProvider.getUserWithDuplicatedName());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Expected error message is different than actual",
        errorBody.errors.username,
        Matchers.hasItemInArray("has already been taken"));
  }

  @Test
  @DisplayName("Register user with empty username, check error message")
  void userWithEmptyUsername() {
    // GIVEN
    prepareRequestBody(testDataProvider.getUserWithEmptyName());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Expected error messages are different than actual",
        errorBody.errors.username,
        Matchers.arrayContaining("can't be blank", "is too short (minimum is 1 character)"));
  }

  @Test
  @DisplayName("Register user without specifying username, check error message")
  void userWithoutUsername() {
    // GIVEN
    prepareRequestBody(testDataProvider.getUserWithoutName());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    // todo: in response I've got error connected to too long username - because it is taking
    //  String full name and count it's letters
    MatcherAssert.assertThat(
        "Expected error messages are different than actual",
        errorBody.errors.username,
        Matchers.arrayContaining("can't be blank", "is too short (minimum is 1 character)", "is too long (maximum is 20 characters)"));
  }

  @Test
  @DisplayName("Register user with max+1 chars username, check error message")
  void userWithMaxPlusOneUsernameLength() {
    // GIVEN
    prepareRequestBody(testDataProvider.getUserWithMaxPlusName());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Expected error message is different than actual",
        errorBody.errors.username,
        Matchers.hasItemInArray("is too long (maximum is 20 characters)"));
  }


  // I believe there is no such thing as too long email - I put 1800 chars into it and register user
  // minimal email format is '*@*.*' but most of them already used, so it will be hard to generate
  @Test
  @DisplayName("Register user with in strange format, check username match")
  void userWithStrangeEmail() {

    for (RegistrationRequestUser user : testDataProvider.getUsersWithStrangeEmailFormat()) {
      //GIVEN
      prepareRequestBody(user);

      //WHEN
      responseBody = requestSpecification.post(USERS.getEndpoint()).as(UserResponseDto.class);

      //THEN
      MatcherAssert.assertThat(
          "Expected username is different than actual",
          responseBody.user.username,
          Matchers.equalTo(user.username));
    }
  }

  @Test
  @DisplayName("Register user with special characters in email, check username mathc")
  void userWithSpecialCharsInEmail() {
    //GIVEN
    // todo: change method so it will be returning requestBody?! requestBody = prepareRequestBody(user)
    //  so in assertion it will be more clear from where 'requestBody.user.username' came from ?!
    // todo:  stable: '!#$%^&@mail.com' vs random: '攫攫攫攫@mail.com'
    prepareRequestBody(testDataProvider.getUserWithSpecialCharsEmail());

    // WHEN
    responseBody = requestSpecification.post(USERS.getEndpoint()).as(UserResponseDto.class);

    //THEN
    MatcherAssert.assertThat(
        "Expected username is different than actual",
        responseBody.user.username,
        Matchers.equalTo(requestBody.user.username));

  }

  @Test
  @DisplayName("Register user with space inside email, check error message")
  void userWithSpaceInEmail() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithSpaceInEmail());

    //WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    //THEN
    MatcherAssert.assertThat(
        "Expected error message is different than expected",
        errorBody.errors.email,
        Matchers.hasItemInArray("is invalid"));

  }

  @Test
  @DisplayName("Register user with email which is incorrectly formatted, check error message")
  void userWithIncorrectlyFormattedEmail() {

    for (RegistrationRequestUser user : testDataProvider.getUsersWithWrongEmailFormat()) {
      // GIVEN
      prepareRequestBody(user);

      // WHEN
      errorBody =
          requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

      // THEN
      MatcherAssert.assertThat(
          "Expected error message is different than expected",
          errorBody.errors.email,
          Matchers.hasItemInArray("is invalid"));
    }
  }

  @Test
  @DisplayName("Register user with email which is already taken, check error message")
  void userWithTakenEmailAddress() {
    // GIVEN
    requestBody = new RegistrationRequestUserDto(testDataProvider.getValidRegistrationUser());
    RestAssured.given()
        .contentType(APPLICATION_JSON.getDetail())
        .body(requestBody)
        .post(USERS.getEndpoint());

    prepareRequestBody(testDataProvider.getValidRegistrationUser());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Expected and actual error messages are not the same",
        errorBody.errors.email,
        Matchers.hasItemInArray("has already been taken"));
  }
  
  @Test
  @DisplayName("Register user with empty email in request body, check error message")
  void userWithEmptyEmail() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithEmptyEmail());

    //WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    //THEN
    MatcherAssert.assertThat(
        "Expected error message is different than expected",
        errorBody.errors.email,
        Matchers.hasItemInArray("can't be blank"));
  }

  @Test
  @DisplayName("Register user without email, check error message")
  void userWithoutEmail() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithoutEmail());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    //THEN
    MatcherAssert.assertThat("Expected error message is different than expected", errorBody.errors.email,
        Matchers.hasItemInArray("can't be blank"));

  }


  @Test
  @DisplayName("Register user with minimal password length, check status code")
  void userWithMinPassLength() {
    // GIVEN
    prepareRequestBody(testDataProvider.getUserWithMinPassLength());

    // WHEN
    statusCode = requestSpecification.post(USERS.getEndpoint()).getStatusCode();

    // THEN
    MatcherAssert.assertThat(
        "Actual status code different than expected",
        statusCode,
        Matchers.equalTo(CODE_200.getValue()));
  }

  @Test
  @DisplayName("Register user with minimal password length, check error message")
  void userWithLessThanMinPass() {
    // GIVEN
    prepareRequestBody(testDataProvider.getUserWithLessThanMinPass());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Actual error message different than expected",
        errorBody.errors.password,
        Matchers.hasItemInArray("is too short (minimum is 8 characters)"));
  }

  @Test
  @DisplayName("Register user with empty password, check error message")
  void userWithEmptyPassword() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithEmptyPass());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Actual error is different than expected",
        errorBody.errors.password,
        Matchers.hasItemInArray("can't be blank"));
  }

  @Test
  @DisplayName("Register user without password being set, check error message")
  void userWithoutPassword() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithoutPassword());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Actual error message is different than expected",
        errorBody.errors.password,
        Matchers.hasItemInArray("can't be blank"));
  }

  @Test
  @DisplayName("Register user with max password length, check status code")
  void userWithMaxLengthPass() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithMaxLengthPass());

    //WHEN
    statusCode = requestSpecification.post(USERS.getEndpoint()).getStatusCode();

    // THEN
    MatcherAssert.assertThat(
        "Actual status code is different than expected",
        statusCode,
        Matchers.equalTo(CODE_200.getValue()));
  }

  @Test
  @DisplayName("Register user with max + 1 password length, check error message")
  void userWithTooLongPass() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithTooLongPass());

    //WHEN
    errorBody = requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Actual error message is different than expected",
        errorBody.errors.password,
        Matchers.hasItemInArray("is too long (maximum is 72 characters)"));
  }


  @Test
  @DisplayName("Register user with regular special characters in password, check status code")
  void userWithRegularSpecialCharsInPass() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserWithSpecCharsInPass());

    //WHEN
    statusCode = requestSpecification.post(USERS.getEndpoint()).getStatusCode();

    // THEN
    MatcherAssert.assertThat(
        "Actual status code is different than expected",
        statusCode,
        Matchers.equalTo(CODE_200.getValue()));
  }


  // space inside
  @Test
  @DisplayName("Register user with space inside password, check status code")
  void userWithSpaceInPass() {
    //GIVEN
    prepareRequestBody(testDataProvider.getUserInSpaceInPass());

    //WHEN
    statusCode = requestSpecification.post(USERS.getEndpoint()).getStatusCode();

    // THEN
    MatcherAssert.assertThat(
        "Actual status code is different than expected",
        statusCode,
        Matchers.equalTo(CODE_200.getValue()));

  }


  //GENERAL
  @Test
  @DisplayName("Register user with empty body, check error message")
  void userWithEmptyBody() {
    // GIVEN
    prepareRequestBody(new RegistrationRequestUser());

    // WHEN
    errorBody =
        requestSpecification.post(USERS.getEndpoint()).as(UnprocessableEntityErrorDto.class);

    // THEN
    MatcherAssert.assertThat(
        "Expected and actual error messages are not the same",
        errorBody.errors.email,
        Matchers.hasItemInArray("can't be blank"));
  }

  private void prepareRequestBody(RegistrationRequestUser user) {
    requestBody = new RegistrationRequestUserDto(user);
    requestSpecification =
        RestAssured.given().contentType(APPLICATION_JSON.getDetail()).body(requestBody);
  }

}