package org.elasticsearch.plugin.flavor;

import java.util.Collection;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.plugins.AbstractPlugin;

import org.elasticsearch.rest.action.FlavorRestAction;

public class FlavorPlugin extends AbstractPlugin {
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
