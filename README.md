# CDI Config

[![Dependency Status](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd/badge.svg?style=flat)](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd)

Configure your Java EE (or other CDI-) application with a simple `@Config` annotation.

## One Minute Tutorial

1. Add a dependency on `com.github.t1:cdi-config-client` (i.e. the `cdi-config.jar`) to your `war`.
1. Add a file `WEB-INF/classes/configuration.properties` with properties like `foo=bar` to your `war`.
1. Add a `@Config` annotation to one of your fields, e.g.:

```java
        @Config
        String foo;
```

## Conversion

If your field is not a `String`, the value from the properties file has to be converted. We use [Joda-Convert](http://www.joda.org/joda-convert/) for that, so most conversions work just out of the box, i.e. the property `foo=2014-12-31` is automatically converted for `@Config LocalDate foo`, no matter if the `LocalDate` is from JDK 8+ or from JodaTime ;-)

If you need to define a custom converter, see the [Joda-Convert User Guide](http://www.joda.org/joda-convert/userguide.html).

## @Inject and @Config

Configuration happens before Injection does, so you can, e.g., write a producer that requires a configuration.

## Config Sources

The default config source is a file `configuration.properties` in the current class loader, i.e. in the war/jar/whatever. You can specify a different URI to load the config from by setting the system property `cdi-config.config-source`. This URI can 

`java` scheme:

If the

## Reserved For Future Use

Keys starting with a non-letter are meta properties used to configure cdi-config itself (more to that later).

Values should escape dollar signs `$` with a second, i.e. `$$`. We reserve single `$` for a future extensions of expression placeholders like `${...}`.

## Ideas For Future Versions

1. Push config changes (dynamic=true)
1. Expressions in values
1. Configurable URI-ConfigProviders
1. Config-Service
1. DB-Source
1. Documentation annotations on configs get pushed into db
1. Manual qualifiers
1. Automatic qualifiers: App+Version, Host, Locale, ...
1. Dynamic qualifiers from e.g. session (e.g. Market, http-Language, etc.)
1. Comprehensive, consecutive examples
