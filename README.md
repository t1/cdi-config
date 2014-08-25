# CDI Config

Taking the principles of CDI to configuration of Java EE 7 beans... trivial to start, powerful ad infinitum.


## The missing abstraction

Traditional, non-DI code works by 'pulling' the wiring into your application classes, so the application classes control and thereby depend on the wiring. The wiring can not inspect the application classes in order to decide on the wiring. 'classic' DI simply turns this around by defining the wiring outside of your application classes and 'pushing' it in; this requires referencing the application classes (and their properties) from the wiring. CDI puts a very interesting and important twist to 'classic' DI: By using qualifiers (and the type of a property can be regarded as just one such qualifier), the wiring and the application classes are much more loosely coupled: You can refactor both without breaking that link, as the connection is based on a conceptual abstraction expressed in types and qualifiers. This third level combines the best of both non-DI and DI.

For application settings (in addition to configuration of container resources like queues, etc.), the first two levels are just the same: Traditional code 'pulls' the configuration in, DI 'pushes' it from the outside. But configuration benefits from the same loose coupling principle, as all DI does. There are [some](http://www.adam-bien.com/roller/abien/entry/how_to_configure_java_ee) [very](http://seamframework.org/Seam3/ConfigModule) [interesting](http://antoniogoncalves.org/2011/06/10/debate-and-what-about-configuration-in-java-ee-7/) approaches that take the second level, but AFAIU not the third one.

This is what this project tries to achieve at it's core... the rest is for power and comfort.

## Start trivial

It's only three steps:

1. Add a dependency on `com.github.t1:cdi-config-client` (i.e. the `cdi-config.jar`) to your `war`.
1. Add a file `WEB-INF/classes/configuration.properties` with properties like `foo=bar` to your `war`.
1. Add a `@Config` annotation to one of your fields, e.g.:

```java
        @Config
        String foo;
```

You may say that this is not a big deal, but that's the point! Using the field name is just the simplest thing to do. For bigger applications with many configuration points, you'd use the `name` property of the `@Config` annotation to specify a logical name of this setting, thereby reaching 'level 3'.

## Conversion

If your field is not a `String`, the value from the properties file has to be converted. We use [Joda-Convert](http://www.joda.org/joda-convert/) for that, so most conversions work just out of the box, i.e. for the property `foo=2014-12-31` is automatically converted for `@Config LocalDate foo`, no matter if the `LocalDate` is from JDK 8+ or from JodaTime ;-)

If you need to define a custom converter, see the [Joda-Convert User Guide](http://www.joda.org/joda-convert/userguide.html).

## Property Names

A good convention is to use dot-delimited names, e.g. `x.y.z`. Keys starting with a `-` are meta properties used to configure cdi-config itself (more to that later). Your values should not contain expression placeholders like `${...}` as we reserve that for future extensions.

## @Inject and @Config

Configuration happens before Injection does, so you can, e.g., write a producer that requires a configuration.

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
