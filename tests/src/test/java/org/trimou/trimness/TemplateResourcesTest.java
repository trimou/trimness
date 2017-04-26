package org.trimou.trimness;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.trimou.trimness.config.TrimnessConfigurationKey.FS_TEMPLATE_REPO_DIR;
import static org.trimou.trimness.util.Strings.CODE;
import static org.trimou.trimness.util.Strings.SUCCESS;
import static org.trimou.trimness.util.Strings.TEMPLATES;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.trimou.trimness.util.Strings;

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
                .addSystemProperty(FS_TEMPLATE_REPO_DIR.get(),
                        "src/test/resources/templates")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(TemplateResourcesTest.class))
                .build();
    }

    @RunAsClient
    @Test
    public void testAllTemplates() {
        Response response = RestAssured.given()
                .header(Strings.HEADER_CONTENT_TYPE, Strings.APP_JSON)
                .get("/template");
        response.then().assertThat().statusCode(200)
                .body(CODE, equalTo(SUCCESS)).body(TEMPLATES, hasItems(
                        "hello.txt", "hello.html", "hello-global-data.txt"));
    }

}
