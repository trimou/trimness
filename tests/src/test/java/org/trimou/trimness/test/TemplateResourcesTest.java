package org.trimou.trimness.test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;
import static org.trimou.trimness.util.Strings.CONTENT;
import static org.trimou.trimness.util.Strings.ID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 *
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class TemplateResourcesTest extends TrimnessTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .addSystemProperty(TEMPLATE_DIR.get(),
                        "src/test/resources/templates")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(TemplateResourcesTest.class))
                .build();
    }

    @RunAsClient
    @Test
    public void testTemplateHandler() {
        Response response = RestAssured.given().get("/template/hello.html");
        response.then().assertThat().statusCode(200)
                .body(ID, equalTo("hello.html")).body(CONTENT, equalTo(
                        "<html><body>Hello {{#model}}{{this}}{{#if iter.hasNext}}, {{/if}}{{/model}}!</body></html>"));

        response = RestAssured.given().urlEncodingEnabled(false)
                .get("/template/deep%2Finto%2Fthis.html");
        response.then().assertThat().statusCode(200)
                .body(CONTENT, equalTo(
                        "<html><body>Hello \"{{model}}\"!</body></html>"));

        RestAssured.given().get("/neverexisted").then().assertThat()
                .statusCode(404);
    }

}
