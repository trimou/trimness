package org.trimou.basis;

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
public abstract class BasisTest {

    static ClassPath.Builder createDefaultClassPath() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(BasisLaunchServices.class))
                .addSystemProperty(
                        LaunchServices.SYSTEM_PROPERTY_LAUNCH_SERVICES_CLASS,
                        BasisLaunchServices.class.getName())
                .add(Maven.resolver().loadPomFromFile("pom.xml")
                        .resolve("org.trimou:trimou-basis")
                        .withTransitivity().asFile());
    }

}
