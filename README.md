# CDI Config

[![Dependency Status](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd/badge.svg?style=flat)](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd)

Configure your Java EE (or other CDI-) application with a simple `@Config` annotation.

## One Minute Tutorial

1. Add a dependency on `com.github.t1:cdi-config-client` (i.e. the `cdi-config.jar`) to your `war`.
1. Add a file `WEB-INF/classes/configuration.properties` with property `*import*=file:~/my-app.properties` to your `war`.
1. Add a file `my-app.properties` with a property `foo=bar` to the home directory.
1. Add a `@Config` annotation to one of your fields, e.g.:

```java
        @Config
        String foo;
```

After deploying your war, the field `foo` will be set to the value `bar`. If you change the config file, the value is updated at runtime.

## Conversion

If your field is not a `String`, the value from the properties file has to be converted. Most conversions work just out of the box, i.e. the property `foo=2014-12-31` is automatically converted for `@Config LocalDate foo`, no matter if the `LocalDate` is from JDK 8+ or from JodaTime ;-) If you need to define a custom converter, see the [Joda-Convert User Guide](http://www.joda.org/joda-convert/userguide.html); `cdi-config` uses `StringConvert.INSTANCE`.

## Config Sources

The source of your configurations is set with the system property `cdi-config.config-source`. This has to be an URI like `http://example.org/my-app.properties`. A `file` URI can be relative to the working directory of your web container, e.g. `file:my-app.properties`, or relative to the home directory of the web container's user, e.g. `file:~/.my-app.properties`. `file` URIs are watched for changes, i.e. if the file changes, all configuration points are updated to the new value (see Thread Safety below).

The default configuration source URI is `classpath:configuration.properties`. The `classpath` scheme can be used to load a file from the current class loader, i.e. _in_ your war/jar/whatever.

Configs can also be set with system properties. They overwrite all other configurations. If a system property is changed, the configuration points will be updated (but you can't start or stop overwriting). Also environment variables can be used to configure, which is useful e.g. for Jenkins jobs. System properties overwrite environment variables.

If you want to implement your own config source, you can use the `java` scheme and the fully qualified name of a class implementing the `com.github.t1.config.ConfigSource` interface, e.g. `java:com.example.MyConfigSource`.

Properties that start with `*import*` will be used as additional URIs to load more configuration.

## Thread Safety

Setting values in a multi threaded environment can be dangerous, and really strange things can happen. Not only that a value changes between two accesses, other, really nasty things like partial updates can happen, e.g., on some processor architectures it may happen that one half of a long is updated while the other half still is the old value.

To prevent these issues, config fields that are accessed by multiple threads should be marked as `volatile` or packaged into an `AtomicReference`, if their config source is updated.

## Reserved For Future Use

Keys starting with a non-letter are reserved, e.g. for meta properties used to configure cdi-config itself (e.g. for `*import*` properties).

Values should escape dollar signs `$` with a second, i.e. `$$`. Single `$` are reserved for future extensions, e.g. for expressions like `${other.property}`.

## Ideas For Future Versions

1. Default values (maybe just by assignment)
1. Documentation annotations on configs
1. Automatic qualifiers: App, Version, Host, Locale. Resolve to matrix params / file names.
1. Config-Service to proxy config source
1. SSE for updates over http
1. DB-Source
1. Expressions in values
1. Dynamically overwrite with system properties
1. Manual qualifiers
1. Dynamic qualifiers from e.g. session (e.g. Market, http-Language, etc.)
1. Comprehensive, consecutive examples
