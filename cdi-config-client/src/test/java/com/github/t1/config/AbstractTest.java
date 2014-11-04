package com.github.t1.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;


abstract class AbstractTest {
    @Deployment
    public static JavaArchive loggingInterceptorDeployment() {
        return ShrinkWrap
                .create(JavaArchive.class)
                .addClasses(Config.class, ConfigCdiExtension.class, ConfigurationPoint.class,
                        InjectionTargetWrapper.class) //
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml") //
        ;
    }
}
