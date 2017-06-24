package org.trimou.trimness.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.trimou.trimness.config.TrimnessKey.GLOBAL_JSON_FILE;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;
import static org.trimou.trimness.test.Timeouts.DEFAULT_TIMEOUT;
import static org.trimou.trimness.util.Strings.RESULT;
import static org.trimou.trimness.util.Strings.RESULT_ID;
import static org.trimou.trimness.util.Strings.STATUS;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.VALUE;

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
import org.trimou.trimness.render.Result;
import org.trimou.trimness.util.Jsons;
import org.trimou.trimness.util.Strings;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class RenderEndpointsTest extends TrimnessTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .addSystemProperty(TEMPLATE_DIR.get(),
                        "src/test/resources/templates")
                .addSystemProperty(GLOBAL_JSON_FILE.get(),
                        "src/test/resources/global-data.json")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(RenderEndpointsTest.class))
                .build();
    }

    @Rule
    public Timeout globalTimeout = Timeout
            .millis(Timeouts.DEFAULT_GLOBAL_TIMEOUT);

    @RunAsClient
    @Test
    public void testOnetimeHello() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder()
                        .add("templateContent",
                                "Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .build().toString())
                .post("/render").then().assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testOnetimeInvalidInput() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("foo", "bar").build()
                        .toString())
                .post("/render").then().assertThat().statusCode(400);
    }

    @RunAsClient
    @Test
    public void testHello() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("templateId", "hello.txt")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .add("contentType", "text/plain").build().toString())
                .post("/render").then().assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testHelloMetadata() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("templateId", "hello.txt")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .add("contentType", "text/plain")
                        .add("resultType", "metadata").build().toString())
                .post("/render").then().assertThat().statusCode(200)
                .body(VALUE, equalTo("Hello me, Lu, foo!"))
                .body(TEMPLATE_ID, equalTo("hello.txt"));
    }

    @RunAsClient
    @Test
    public void testHelloGlobalData() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder()
                        .add("templateId", "hello-global-data.txt")
                        .add("model", Jsons.objectBuilder().add("name", 1))
                        .add("contentType", "text/plain").build().toString())
                .post("/render").then().assertThat().statusCode(200)
                .body(equalTo("##hello-global-data.txt## Hello 1 and bar!"));
    }

    @RunAsClient
    @Test
    public void testMetadata() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder()
                        .add("templateContent",
                                "{{meta.config.host}}:{{meta.config.port}}")
                        .build().toString())
                .post("/render").then().assertThat().statusCode(200)
                .body(equalTo("localhost:8080"));
    }

    @RunAsClient
    @Test
    public void testOnetimeAsync() throws InterruptedException {
        String resultId = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("async", true)
                        .add("templateContent",
                                "Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .build().toString())
                .post("/render").then().assertThat().statusCode(200).extract()
                .path(RESULT_ID);

        assertResultSuccess(resultId);

        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/result/" + resultId + "?resultType=raw").then()
                .assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testAsync() throws InterruptedException {
        String resultId = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("async", true)
                        .add("templateId", "hello.txt")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .add("timeout", 0).build().toString())
                .post("/render").then().assertThat().statusCode(200)
                .header(Strings.HEADER_CONTENT_TYPE, is(Strings.APP_JSON))
                .extract().path(RESULT_ID);

        assertResultSuccess(resultId);

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
    public void testOnetimeAsyncFailure() throws InterruptedException {
        String resultId = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("async", true)
                        .add("templateContent", "{{#each}}")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .build().toString())
                .post("/render").then().assertThat().statusCode(200).extract()
                .path(RESULT_ID);

        assertResult(resultId, Result.Status.FAILURE.toString());

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
    public void testLinkAsync() throws InterruptedException {
        String resultId = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("async", true)
                        .add("templateContent",
                                "Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .add("linkId", "test-link").build().toString())
                .post("/render").then().assertThat().statusCode(200).extract()
                .path(RESULT_ID);

        assertResultSuccess(resultId);

        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/result/link/test-link?resultType=raw").then()
                .assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testInvalidLinkAsync() {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("async", true)
                        .add("templateContent",
                                "Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .add("linkId", "test link").build().toString())
                .post("/render").then().assertThat().statusCode(400);
    }

    private void assertResultSuccess(String resultId)
            throws InterruptedException {
        assertResult(resultId, Result.Status.SUCESS.toString());
    }

    private void assertResult(String resultId, String status)
            throws InterruptedException {
        Timer.of(DEFAULT_TIMEOUT).stopIf(() -> {
            Response response = RestAssured.given()
                    .get("/result/" + resultId + "?resultType=metadata");
            if (response.getStatusCode() == 200
                    && Jsons.asJsonObject(response.getBody().asString())
                            .getJsonObject(RESULT).getString(STATUS, "")
                            .equalsIgnoreCase(status)) {
                return true;
            }
            return false;
        }).setFailIfElapsed(true).countDown();
    }

}
