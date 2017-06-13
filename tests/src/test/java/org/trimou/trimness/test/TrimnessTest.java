package org.trimou.trimness.test;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.se.api.LaunchServices;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 *
 * @author Martin Kouba
 *
 */
public abstract class TrimnessTest {

    static ClassPath.Builder createDefaultClassPath() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(TrimnessLaunchServices.class))
                .addSystemProperty(
                        LaunchServices.SYSTEM_PROPERTY_LAUNCH_SERVICES_CLASS,
                        TrimnessLaunchServices.class.getName())
                .add(Maven.resolver().loadPomFromFile("pom.xml")
                        .resolve("org.trimou:trimness-core", "org.slf4j:slf4j-simple")
                        .withTransitivity().asFile());
    }

}
