package org.trimou.basis;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.trimou.basis.BasisConfigurationKey.FS_TEMPLATE_REPO_DIR;
import static org.trimou.basis.Strings.RESULT_ID;

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

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class AsyncRenderTest extends BasisTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return BasisTest.createDefaultClassPath()
                .addSystemProperty(FS_TEMPLATE_REPO_DIR.get(),
                        "src/test/resources/templates")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(AsyncRenderTest.class))
                .build();
    }

    @Rule
    public Timeout globalTimeout = Timeout.millis(5000);

    @RunAsClient
    @Test
    public void testOnetimeAsync(TestContext context) {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"async\" : true, \"content\" : \"Hello {{#each data}}{{this}}{{#hasNext}}, {{/hasNext}}{{/each}}!\", \"data\" : [ \"me\", \"Lu\", \"foo\" ]}")
                .post("/render");

        response.then().assertThat().statusCode(200);
        String resultId = response.path(RESULT_ID).toString();
        assertNotNull(resultId);

        response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/render/" + resultId + "?resultType=raw");
        response.then().assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

    @RunAsClient
    @Test
    public void testAsync(TestContext context) {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body("{\"async\" : true, \"id\" : \"hello.txt\", \"data\" : [ \"me\", \"Lu\", \"foo\" ]}")
                .post("/render");

        response.then().assertThat().statusCode(200);
        String resultId = response.path(RESULT_ID).toString();
        assertNotNull(resultId);

        response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/render/" + resultId + "?resultType=raw");
        response.then().assertThat().statusCode(200)
                .body(equalTo("Hello me, Lu, foo!"));
    }

}
