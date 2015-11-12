# CDI Config

[![Dependency Status](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd/badge.svg?style=flat)](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd)

Configure your Java EE (or other CDI-) application with a simple `@Config` annotation.

## One Minute Tutorial

1. Add a dependency on `com.github.t1:cdi-config-client` (i.e. the `cdi-config.jar`) to your `war`.
1. Add a file `WEB-INF/classes/configuration.properties` with property `*import*=file:~/my-app.properties` to your `war`.
1. Add a file `my-app.properties` with a property `foo=bar` to the home directory of your container's user.
1. Add a `@Config` annotation to one of your fields, e.g.:

```java
        @Config
        String foo;
```
1. After deploying your war, the field `foo` will be set to the value `bar`.
1. Finally change `bar` to `baz` in the config file.
1. See the value updated at runtime.

## Conversion

If your field is not a `String`, the value from the properties file has to be converted. Most conversions work just out of the box, i.e. the property `foo=2014-12-31` is automatically converted for `@Config LocalDate foo`, no matter if the `LocalDate` is from JDK 8+ or from JodaTime ;-) If you need to define a custom converter, see the [Joda-Convert User Guide](http://www.joda.org/joda-convert/userguide.html); `cdi-config` uses `StringConvert.INSTANCE`.

## Config Sources

The source of your configurations is set with the system property `cdi-config.config-source`. This has to be an URI like `http://example.org/my-app.properties`. A `file` URI can be relative to the working directory of your web container, e.g. `file:my-app.properties`, or relative to the home directory of the web container's user, e.g. `file:~/.my-app.properties`. `file` URIs are watched for changes, i.e. if the file changes, all configuration points are updated to the new value (see Thread Safety below).

The default configuration source URI is `classpath:configuration.properties`. The `classpath` scheme can be used to load a file from the current class loader, i.e. _in_ your war/jar/whatever.

Configs can also be set with system properties. They overwrite all other configurations. If a system property is changed, the configuration points will be updated (but you can't start or stop overwriting). Also environment variables can be used to configure, which is useful e.g. for Jenkins jobs. System properties overwrite environment variables.

If you want to implement your own config source, you can use the `java` scheme and the fully qualified name of a class implementing the `com.github.t1.config.ConfigSource` interface, e.g. `java:com.example.MyConfigSource`.

Properties that start with `*import*` will be used as additional URIs to load more configuration. Note that property files allow only unique keys, so you can use suffixes for import keys; descriptive names have proven to be very helpful, e.g., `*import*config-dir`, `*import*central-uri`.

Values containing curly brackets `{` or `}` are resolved as configuration expressions like `{other.property}`. Currently only system properties can be resolved in this way.

## Thread Safety

There's not guarantee as to when a change from a configuration source will be applied to a configuration point; or in what order.

If you can live with that, you're fine. If not, here's some background:

Configuration changes can't reasonably be made atomic. So, even if you change multiple values in a config source at the same time, they will be applied one by one and it can happen that one value has already changed while another has not, yet. This happens also for single values used in different beans; and even several instances of the same bean are updated one by one.

Additionally, updating config values happens in a separate thread. `cdi-config` prevents multi-threading issues on beans containing at least one thread-unsafe configuration point by delaying config updates until all invocations on a bean instance have returned. A configuration point can be made thread-safe by using the `volatile` keyword or putting it into an `AtomicReference`.

Generally this is only relevant for beans that are under constant load, when it may take a long time until your config change is applied. If you do not what this delaying, you can make all configuration points thread-safe instead.

Note that configured beans have to set a lock before and another lock after the actual call, so that can be a performance impact for beans that are under heavy load.

## Reserved For Future Use

Keys starting with a non-letter are reserved, e.g. for meta properties used to configure cdi-config itself (e.g. for `*import*` properties).

## Ideas For Future Versions

1. Write `Config#description` as comment to properties file
1. JSON config files
1. YAML config files
1. XML config files
1. Authorize for http config sources (so we can read confidential configs)
1. Web-Interface to see and change configs
1. JMX config source
1. JNDI config source
1. DB config source
1. @Config on methods 
1. @Config on constructors (maybe requires @Inject)
1. Convert complex config objects from JSON or xml
1. REST-Service to proxy config source
1. SSE/Websockets for updates over http
1. Cluster support
1. Non-system property expressions in values
1. Dynamically overwrite (e.g. with system property)
1. Manual qualifiers
1. Dynamic qualifiers from e.g. session (e.g. Market, http-Language, etc.)
1. Automatic qualifiers: App, Version, Host, Locale.
1. Resolve to matrix params / file names.
1. Default values (maybe just by assignment)
1. Default values in value expressions (e.g. `{foo:bar}` resolves to the value of `foo` if configured, or `bar` if not)
1. Comprehensive, consecutive examples
