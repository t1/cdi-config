# CDI Config

[![Dependency Status](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd/badge.svg?style=flat)](https://www.versioneye.com/user/projects/53f72f0ae09da337bc0003bd)

Taking the principles of CDI to configuration of Java EE 7 beans... trivial to start, powerful ad infinitum.


## The missing abstraction

Traditional code works by 'pulling' the wiring into your application classes, so the application classes control and thereby depend on the wiring. The wiring can not inspect the application classes in order to decide on the wiring. 'Classic' [IoC](inversion of control) simply turns this around by defining the wiring outside of your application classes and 'pushing' it in; this requires referencing the application classes (and their properties) from the wiring. [DI](dependency injection) (CDI as well as some configuration modes in Spring) puts a very interesting and important twist to IoC: By using qualifiers (and the type of a property can be regarded as just one such qualifier; the name of a bean can also be used in this way, but often it's not), the wiring and the application classes are much more loosely coupled: You can refactor both without breaking that link, as the connection is based on a conceptual abstraction, expressed in types and qualifiers. This third level combines the best of both non-DI and DI.

Most applications require configuration. The container has to provide resources like data sources, queues, etc. for the application to be able to run. And the application directly needs configuration for e.g. application settings like remote endpoints or [feature toggles](fowler feature toggle). And it's just like with IoC/DI: Traditional code 'pulls' the configuration in, DI 'pushes' it from the outside. And configuration benefits from the same loose coupling principle, as all DI does. There are [some](http://www.adam-bien.com/roller/abien/entry/how_to_configure_java_ee) [very](http://seamframework.org/Seam3/ConfigModule) [interesting](http://antoniogoncalves.org/2011/06/10/debate-and-what-about-configuration-in-java-ee-7/) approaches that take the second level, but AFAIU not the third one.

This is what this project tries to achieve at it's core... the rest is for power and comfort: GUIs, versions, stages, etc.

## Start trivial

It's only three steps:

1. Add a dependency on `com.github.t1:cdi-config-client` (i.e. the `cdi-config.jar`) to your `war`.
1. Add a file `WEB-INF/classes/configuration.properties` with properties like `foo=bar` to your `war`.
1. Add a `@Config` annotation to one of your fields, e.g.:

```java
        @Config
        String foo;
```

You may say that this is not a big deal, but that's the point! Using the field name is just the simplest thing to do. For bigger applications with many configuration points, you'd use the `name` property of the `@Config` annotation or qualifiers to specify a logical abstraction for this setting, thereby reaching 'level 3'. And the source of the config is not limited to properties files within the war: You can (in time) use files in the file system, data bases, and maybe more.

## Conversion

If your field is not a `String`, the value from the properties file has to be converted. We use [Joda-Convert](http://www.joda.org/joda-convert/) for that, so most conversions work just out of the box, i.e. the property `foo=2014-12-31` is automatically converted for `@Config LocalDate foo`, no matter if the `LocalDate` is from JDK 8+ or from JodaTime ;-)

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
