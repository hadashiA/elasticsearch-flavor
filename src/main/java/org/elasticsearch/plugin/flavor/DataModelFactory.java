package org.elasticsearch.plugin.flavor;

import org.apache.mahout.cf.taste.common.TasteException;

import org.apache.mahout.cf.taste.model.DataModel;

public interface DataModelFactory {
    public DataModel createItemBasedDataModel(final long itemId) throws TasteException;
    public DataModel createUserBasedDataModel(final long userId) throws TasteException;
}
