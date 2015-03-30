package org.elasticsearch.plugin.flavor;

import org.elasticsearch.plugins.AbstractPlugin;

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
        
    }
}
