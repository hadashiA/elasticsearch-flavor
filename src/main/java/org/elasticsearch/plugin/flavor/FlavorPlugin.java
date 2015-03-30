package org.elasticsearch.plugin.flavor;

import java.util.Collection;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

import org.elasticsearch.plugin.flavor.FlavorRestModule;

public class FlavorPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "FlavorPlugin";
    }

    @Override
    public String description() {
        return "This is a elasticsearch-flavor plugin.";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = Lists.newArrayList();
        modules.add(FlavorRestModule.class);
        return modules;
    }
}
