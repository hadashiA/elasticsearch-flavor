package org.elasticsearch.plugin.flavor;

import java.util.Collection;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.plugins.Plugin;

import org.elasticsearch.plugin.flavor.FlavorRestAction;

public class FlavorPlugin extends Plugin {
    @Override
    public String name() {
        return "flavor";
    }

    @Override
    public String description() {
        return "This is a elasticsearch-flavor plugin.";
    }

    public void onModule(final RestModule module) {
        module.addRestAction(FlavorRestAction.class);
    }
}
