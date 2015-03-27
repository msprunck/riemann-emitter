package com.sprunck.druid.riemann;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.metamx.emitter.core.Emitter;
import io.druid.guice.JsonConfigProvider;
import io.druid.guice.ManageLifecycle;
import io.druid.initialization.DruidModule;

import javax.inject.Named;
import java.util.List;

/**
 * Druid module to inject the Riemann emitter.
 */
public class RiemannDruidModule implements DruidModule {
    /**
     * Emitter type to use in the druid configuration file (ex: druid.emitter=riemann).
     */
    public static final String EMITTER_TYPE = "riemann";

    @Override
    public List<? extends Module> getJacksonModules() {
        return ImmutableList.of();
    }

    @Override
    public void configure(Binder binder) {
        // Bind the Riemann configuration with the druid configuration
        JsonConfigProvider.bind(binder, "druid.emitter.riemann", RiemannEmitterConfig.class);
    }

    @Provides
    @ManageLifecycle
    @Named(EMITTER_TYPE)
    public Emitter makeEmitter(Supplier<RiemannEmitterConfig> config, ObjectMapper jsonMapper) {
        return new RiemannEmitter(config.get());
    }

    // To avoid multiple injection by Druid (See https://github.com/druid-io/druid/issues/1016)
    @Override
    public boolean equals(Object obj) {
      return getClass().getCanonicalName().equals(getClass().getCanonicalName());
    }

    @Override
    public int hashCode() {
      return getClass().getCanonicalName().hashCode();
    }
}
