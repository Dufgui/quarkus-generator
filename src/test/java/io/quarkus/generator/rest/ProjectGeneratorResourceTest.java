package io.quarkus.generator.rest;

import io.quarkus.generator.rest.ProjectGeneratorResource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ProjectGeneratorResourceTest {

    @Test
    public void testMissingArgGenerator() {
        given()
          .when().get("/generator")
          .then()
             .statusCode(500);
    }

    @Test
    public void testGenerator() {
        given()
          .when().get("/generator?g=org.acme&a=artifactId&pv=1.0.0&cn=org.acme.TotoResource&e=io.quarkus:quarkus-resteasy")
          .then()
             .statusCode(200);
    }

}
