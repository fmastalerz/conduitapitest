package basictestsv2;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jsons.UserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import wrappers.UserRequestWrapper;
import wrappers.UserResponseWrapper;

public class BasicTestsV2 {
  private static String URL = "https://conduit.productionready.io/api";
  private static String TOKEN;

  @Test
  @DisplayName(
      "Authentication - log user, check his ID and status code, but with using ObjectMapper")
  void logUserCheckIdAndStatusWithObjectMapper() throws IOException {
    // GIVEN
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    UserRequest user = new UserRequest("adam@mail.com", "adam1234");
    UserRequestWrapper userWrapper = new UserRequestWrapper(user);

    UserResponseWrapper response = mapper.readValue(given()
        .contentType("application/json")
        .body(userWrapper)
        .when()
        .post(URL + "/users/login")
        .body()
        .prettyPrint(), UserResponseWrapper.class);

    TOKEN = response.userResponse.token;

    System.out.println("Token: " + TOKEN);
    System.out.println("Username: " + response.userResponse.username);
  }
}