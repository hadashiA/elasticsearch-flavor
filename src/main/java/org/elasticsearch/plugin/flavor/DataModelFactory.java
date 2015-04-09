package org.elasticsearch.plugin.flavor;

import org.apache.mahout.cf.taste.common.TasteException;

import org.apache.mahout.cf.taste.model.DataModel;

public interface DataModelFactory {
    public DataModel createItemBasedDataModel(final String index,
                                              final String type,
                                              final long itemId) throws TasteException;
    public DataModel createUserBasedDataModel(final String index,
                                              final String type,
                                              final long userId) throws TasteException;
}
