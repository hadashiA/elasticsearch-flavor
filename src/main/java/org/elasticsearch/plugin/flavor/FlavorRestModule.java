package org.elasticsearch.plugin.flavor;

import org.elasticsearch.common.inject.AbstractModule;

import org.elasticsearch.plugin.flavor.FlavorRestHandler;

public class FlavorRestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FlavorRestHandler.class).asEagerSingleton();
    }
}
