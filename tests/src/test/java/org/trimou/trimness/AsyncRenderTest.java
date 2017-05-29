package org.trimou.trimness;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;
import static org.trimou.trimness.util.Strings.RESULT_ID;

import org.hamcrest.core.StringContains;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.trimou.exception.MustacheProblem;
import org.trimou.trimness.util.Strings;

import io.restassured.RestAssured;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class AsyncRenderTest extends TrimnessTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .addSystemProperty(TEMPLATE_DIR.get(),
                        "src/test/resources/templates")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(AsyncRenderTest.class))
                .build();
    }

    @Rule
    public Timeout globalTimeout = Timeout.millis(50000);

    @RunAsClient
    @Test
    public void testOnetimeAsync() {
        String resultId = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"async\" : true, \"templateContent\" : \"Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!\", \"model\" : [ \"me\", \"Lu\", \"foo\" ]}")
                .post("/render").then().assertThat().statusCode(200).extract()
                .path(RESULT_ID);
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/result/" + resultId + "?resultType=raw").then()
                .assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testAsync() {
        String resultId = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"async\" : true, \"templateId\" : \"hello.txt\", \"model\" : [ \"me\", \"Lu\", \"foo\" ], \"timeout\" : 0}")
                .post("/render").then().assertThat().statusCode(200)
                .header(Strings.HEADER_CONTENT_TYPE, is(Strings.APP_JSON))
                .extract().path(RESULT_ID);

        RestAssured.given().get("/result/" + resultId + "?resultType=raw")
                .then().assertThat().statusCode(200)
                .header(Strings.HEADER_CONTENT_TYPE, is(Strings.TEXT_PLAIN))
                .body(equalTo("Hello me, Lu, foo!"));

        RestAssured.given().delete("/result/" + resultId).then().assertThat()
                .statusCode(200);
        RestAssured.given().get("/result/" + resultId + "?resultType=raw")
                .then().assertThat().statusCode(404);
    }

    @RunAsClient
    @Test
    public void testOnetimeAsyncFailure() {
        String resultId = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"async\" : true, \"templateContent\" : \"{{#each}}\", \"model\" : [ \"me\", \"Lu\", \"foo\" ]}")
                .post("/render").then().assertThat().statusCode(200).extract()
                .path(RESULT_ID);

        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/result/" + resultId + "?resultType=raw").then()
                .assertThat().statusCode(200)
                .header(Strings.HEADER_CONTENT_TYPE, is(Strings.APP_JSON))
                .body(StringContains.containsString(
                        MustacheProblem.COMPILE_INVALID_TEMPLATE.toString()));
    }

    @RunAsClient
    @Test
    public void testLink() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"async\" : true, \"templateContent\" : \"Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!\", \"model\" : [ \"me\", \"Lu\", \"foo\" ], \"linkId\":\"test-link\"}")
                .post("/render").then().assertThat().statusCode(200);
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/result/link/test-link?resultType=raw").then()
                .assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testInvalidLink() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"async\" : true, \"templateContent\" : \"Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!\", \"model\" : [ \"me\", \"Lu\", \"foo\" ], \"linkId\":\"test link\"}")
                .post("/render").then().assertThat().statusCode(400);
    }

}
