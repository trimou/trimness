package org.trimou.trimness;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.trimou.trimness.util.Strings;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 *
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class MonitorResourcesTest extends TrimnessTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(MonitorResourcesTest.class))
                .build();
    }

    @RunAsClient
    @Test
    public void testPing() {
        RestAssured.head("/monitor/ping").then().assertThat().statusCode(200);
    }

    @RunAsClient
    @Test
    public void testHealthCheck() {
        Response response = RestAssured.get("/monitor/health");
        response.then().assertThat().statusCode(200)
                .body("result", equalTo(Strings.SUCCESS))
                .body("checks", not(empty()));

        ReadContext ctx = JsonPath.parse(response.asString());
        List<Map<String, Object>> checks = ctx.read(
                "$.checks[?(@.id == 'org.trimou.engine.MustacheEngine')]");
        assertEquals(1, checks.size());
        assertEquals(Strings.SUCCESS, checks.get(0).get("result"));
    }
}
