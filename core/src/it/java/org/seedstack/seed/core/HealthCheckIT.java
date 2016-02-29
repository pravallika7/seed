/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheck.Result;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import io.nuun.kernel.api.Kernel;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.core.internal.metrics.HealthCheckMethodReplacer;
import org.seedstack.seed.core.internal.metrics.HealthcheckProvider;
import org.seedstack.seed.core.utils.DependencyClassProxy;
import org.seedstack.seed.spi.dependency.Maybe;

import javax.inject.Inject;

public class HealthCheckIT {
    private static Kernel kernel;
    private Injector injector;

    private static final String MY_HEALTHCHECK = "my-healthcheck";

    public static class MyHealthCheck {

        @Inject
        Maybe<HealthcheckProvider> healthCheckProvider;

        public void start() {
            if (healthCheckProvider.isPresent()) {
                DependencyClassProxy<HealthCheck> healthCheck = new DependencyClassProxy<>(HealthCheck.class, (HealthCheckMethodReplacer) Result::healthy);
                healthCheckProvider.get().register(MY_HEALTHCHECK, healthCheck.getProxy());
            }
        }
    }

    @BeforeClass
    public static void beforeClass() {
        kernel = Seed.createKernel();
    }

    @AfterClass
    public static void afterClass() {
        Seed.disposeKernel(kernel);
    }

    @Before
    public void before() {

        Module aggregationModule = new AbstractModule() {

            @Override
            protected void configure() {
                bind(MyHealthCheck.class);
            }
        };
        injector = kernel.objectGraph().as(Injector.class).createChildInjector(
                aggregationModule);
    }

    @Test
    public void test() {
        MyHealthCheck myHealthCheck = injector.getInstance(MyHealthCheck.class);
        myHealthCheck.start();
        Maybe<HealthcheckProvider> provider = injector.getInstance(Key.get(new TypeLiteral<Maybe<HealthcheckProvider>>() {
        }));
        Assertions.assertThat(provider.isPresent()).isTrue();
        HealthCheckRegistry registry = provider.get().getHealthCheckRegistry();
        Assertions.assertThat(registry.getNames().contains(MY_HEALTHCHECK)).isTrue();
        Assertions.assertThat(registry.runHealthCheck(MY_HEALTHCHECK)).isEqualTo(Result.healthy());
    }

}
