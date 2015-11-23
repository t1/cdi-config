package com.github.t1.config;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.time.LocalDate;

import javax.enterprise.context.*;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(Arquillian.class)
public class ConfigIT extends AbstractIT {
    @Value
    public static class Pojo {
        String value;
    }

    static class ToBeConfigured implements Serializable {
        private static final long serialVersionUID = 1L;

        private static int nextId = 0;

        private final int id = nextId++;

        @Config
        String string;

        @Config
        int i;

        @Config
        boolean bool;

        @Config
        LocalDate date;

        @Config(name = "test-string")
        String testString;

        @Config(name = "alt-string")
        String altString;

        @Config(name = "alt2-string")
        String alt2string;

        @Config(name = "alt3-string")
        String alt3string;

        @Config(name = "xml-string")
        String xmlString;

        @Config
        String resolvedString;

        @Config(name = "user.home")
        String home;

        @Config(name = "PATH")
        String path;

        @Config(name = "user.name")
        String userName;

        @Produces
        Pojo producePojo() {
            return new Pojo(bool ? "foo" : "bar");
        }

        @Override
        public String toString() {
            return "#" + id;
        }
    }

    @Inject
    ToBeConfigured tbc;

    @Inject
    Pojo pojo;

    @Rule
    public TestLoggerRule logger = new TestLoggerRule();

    @Test
    public void shouldConfigureString() {
        assertEquals("test-value", tbc.string);
    }

    @Test
    public void shouldConfigureInt() {
        assertEquals(123, tbc.i);
    }

    @Test
    public void shouldConfigureBoolean() {
        assertEquals(true, tbc.bool);
    }

    @Test
    public void shouldConfigureLocalDate() {
        assertEquals(LocalDate.of(2014, 12, 31), tbc.date);
    }

    @Test
    public void shouldUseConfigName() {
        assertEquals("test-value2", tbc.testString);
    }

    @Test
    public void shouldConfigureFromFirstImportedFile() {
        assertEquals("alt-value", tbc.altString);
    }

    @Test
    public void shouldConfigureFromSecondImportedFile() {
        assertEquals("alt2-value", tbc.alt2string);
    }

    @Test
    public void shouldConfigureFromIndirectlyImportedFile() {
        assertEquals("alt3-value", tbc.alt3string);
    }

    @Test
    public void shouldConfigureFromXmlFile() {
        assertEquals("xml-value", tbc.xmlString);
    }

    @Test
    public void shouldResolveValueWithExpession() {
        assertEquals("--" + System.getProperty("user.home") + "--", tbc.resolvedString);
    }

    @Test
    public void shouldConfigureFromSystemProperty() {
        assertEquals(System.getProperty("user.home"), tbc.home);
    }

    @Test
    public void shouldConfigureFromEnvironmentVariable() {
        assertEquals(System.getenv("PATH"), tbc.path);
    }

    @Test
    public void shouldOverwritePropertyValueWithSystemProperty() {
        assertEquals(System.getProperty("user.name"), tbc.userName);
    }

    @Test
    public void shouldCallProducerMethodWithConfigInjected() {
        assertEquals("foo", pojo.value);
    }

    @RequestScoped
    public static class ConversationScopedBean {
        @Inject
        Conversation conversation;

        @Config
        String string;

        public void run() {
            log.debug("conversation is transient: {}", conversation.isTransient());
            // conversation.begin();
            log.debug("begin conversation {}", conversation.getId());

            assertEquals("test-value", string);

            // conversation.end();
            log.debug("ended conversation {}", conversation.getId());
        }
    }

    @Inject
    ConversationScopedBean conversationScopedBean;

    @Test
    @Ignore("ContextNotActiveException? WTF?!?")
    public void shouldDisposeAfterScope() {
        conversationScopedBean.run();
    }
}
