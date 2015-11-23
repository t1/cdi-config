package com.github.t1.config;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.net.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.*;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfigIT {
    @Deployment(testable = false)
    public static WebArchive createArquillianDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addClass(ConfiguredResource.class);
        war.setWebXML(new FileAsset(new File("src/main/webapp/WEB-INF/web.xml")));
        war.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        war.addAsResource(new StringAsset("config-key=configured-value"), "configuration.properties");
        war.addAsLibraries(resolve("com.github.t1:cdi-config"));
        System.out.println("--------------->\n" + war.toString(true) + "\n-----------------");
        return war;
    }

    private static File[] resolve(String gav) {
        return Maven.resolver().loadPomFromFile("pom.xml").resolve(gav).withTransitivity().asFile();
    }

    @ArquillianResource
    URI baseUri;

    private String GET(String request) throws MalformedURLException, IOException {
        URL resource = new URL(baseUri + request);

        try (InputStream response = (InputStream) resource.getContent()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            return reader.readLine();
        }
    }

    @Test
    public void shouldGetConfiguredValue() throws Exception {
        String line = GET("configured?sleep=10");

        assertThat(line).isEqualTo("[0 : configured-value]");
    }

    @Test
    public void shouldGetAllConfigs() throws Exception {
        String line = GET("configured/all");

        assertThat(line).isEqualTo(
                "[@com.github.t1.config.Config(name=config-key, description=the value to be configured, defaultValue=default-value)]");
    }
}
