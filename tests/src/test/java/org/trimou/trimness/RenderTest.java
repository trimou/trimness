package org.trimou.trimness;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;
import static org.trimou.trimness.config.TrimnessKey.GLOBAL_JSON_FILE;
import static org.trimou.trimness.util.Strings.CODE;
import static org.trimou.trimness.util.Strings.RESULT;
import static org.trimou.trimness.util.Strings.SUCCESS;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.trimou.trimness.util.Strings;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class RenderTest extends TrimnessTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .addSystemProperty(TEMPLATE_DIR.get(),
                        "src/test/resources/templates")
                .addSystemProperty(GLOBAL_JSON_FILE.get(),
                        "src/test/resources/global-data.json")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(RenderTest.class))
                .build();
    }

    @Rule
    public Timeout globalTimeout = Timeout.millis(5000);

    @RunAsClient
    @Test
    public void testOnetimeHello() {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"content\" : \"Hello {{#each model}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!\", \"model\" : [ \"me\", \"Lu\", \"foo\" ]}")
                .post("/render");
        response.then().assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testOnetimeInvalidInput(TestContext context) {
        RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"foo\":\"bar\"}").post("/render").then().assertThat()
                .statusCode(400);
    }

    @RunAsClient
    @Test
    public void testHello() {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"id\" : \"hello.txt\", \"model\" : [ \"me\", \"Lu\", \"foo\" ], \"contentType\":\"text/plain\"}")
                .post("/render");
        response.then().assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testHelloMetadata() {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"id\" : \"hello.txt\", \"model\" : [ \"me\", \"Lu\", \"foo\" ], \"contentType\":\"text/plain\", \"resultType\":\"metadata\"}")
                .post("/render");
        response.then().assertThat().statusCode(200)
                .body(CODE, equalTo(SUCCESS))
                .body(RESULT, equalTo("Hello me, Lu, foo!"))
                .body(TEMPLATE_ID, equalTo("hello.txt"));
    }

    @RunAsClient
    @Test
    public void testHelloGlobalData() {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"id\" : \"hello-global-data.txt\", \"model\" : {\"name\":1}, \"contentType\":\"text/plain\"}")
                .post("/render");
        response.then().assertThat().statusCode(200)
                .body(equalTo("##hello-global-data.txt## Hello 1 and bar!"));
    }

    @RunAsClient
    @Test
    public void testMetadata() {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"content\" : \"{{meta.config.host}}:{{meta.config.port}}\" }")
                .post("/render");
        response.then().assertThat().statusCode(200)
                .body(equalTo("localhost:8080"));
    }

}
