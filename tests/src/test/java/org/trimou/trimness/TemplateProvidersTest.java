package org.trimou.trimness;

import static org.hamcrest.core.IsEqual.equalTo;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.trimou.trimness.util.Jsons;
import org.trimou.trimness.util.Strings;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 *
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class TemplateProvidersTest extends TrimnessTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addAsManifestResource(
                                new StringAsset(
                                        "{{#each model}}{{.}}{{/each}}"),
                                "templates/hello.html")
                        .addClasses(TemplateProvidersTest.class))
                .build();
    }

    @RunAsClient
    @Test
    public void testClassPathTemplateRepository() {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .body(Jsons.objectBuilder().add("templateId", "hello.html")
                        .add("model", Jsons.arrayBuilder("me", "Lu", "foo"))
                        .add("contenType", "text/plain").build().toString())
                .post("/render");
        response.then().assertThat().statusCode(200).body(equalTo("meLufoo"));
    }

}
