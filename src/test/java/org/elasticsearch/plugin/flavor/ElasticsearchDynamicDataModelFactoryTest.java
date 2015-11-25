package org.elasticsearch.plugin.flavor;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.elasticsearch.common.settings.Settings.Builder;

import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;


import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs;
import static org.junit.Assert.*;

public class ElasticsearchDynamicDataModelFactoryTest {
    private final static Logger LOGGER = Logger.getLogger(ElasticsearchDynamicDataModelFactory.class.getName());
    
    private ElasticsearchClusterRunner runner;
    private ElasticsearchDynamicDataModelFactory factory;
    private String index = "myindex";
    private String type  = "item";

    @Before
    public void setUp() throws Exception {
        runner = new ElasticsearchClusterRunner();
        runner.onBuild(new ElasticsearchClusterRunner.Builder() {
            @Override
            public void build(final int number, final Builder settingsBuilder) {
                settingsBuilder.put("http.cors.enabled", true);
                settingsBuilder.put("index.number_of_replicas", 0);
            }
        }).build(newConfigs().numOfNode(1));

        runner.ensureYellow();

        runner.createIndex(index, null);
        // user 1
        runner.insert(index, type, "1:101",
                      "{"
                      + "\"user_id\": " + 1 + ","
                      + "\"item_id\":" + 101 + ","
                      + "\"value\":" + 1
                      + "}"
                      );
        runner.insert(index, type, "1:102",
                      "{"
                      + "\"user_id\": " + 1 + ","
                      + "\"item_id\":" + 102 + ","
                      + "\"value\":" + 1
                      + "}"
                      );
        runner.insert(index, type, "1:103",
                      "{"
                      + "\"user_id\": " + 1 + ","
                      + "\"item_id\":" + 103 + ","
                      + "\"value\":" + 1
                      + "}"
                      );
        // user 2
        runner.insert(index, type, "2:102",
                      "{"
                      + "\"user_id\": " + 2 + ","
                      + "\"item_id\":" + 102 + ","
                      + "\"value\":" + 1
                      + "}"
                      );
        // user 3
        runner.insert(index, type, "3:101",
                      "{"
                      + "\"user_id\": " + 3 + ","
                      + "\"item_id\":" + 101 + ","
                      + "\"value\":" + 1
                      + "}"
                      );
        runner.insert(index, type, "3:103",
                      "{"
                      + "\"user_id\": " + 3 + ","
                      + "\"item_id\":" + 103 + ","
                      + "\"value\":" + 1
                      + "}"
                      );
        // user 4
        runner.insert(index, type, "4:105",
                      "{"
                      + "\"user_id\": " + 4 + ","
                      + "\"item_id\":" + 105 + ","
                      + "\"value\":" + 1
                      + "}"
                      );

        runner.refresh();

        this.factory = new ElasticsearchDynamicDataModelFactory(runner.client());
    }

    @After
    public void cleanUp() throws Exception {
        runner.close();
        runner.clean();
    }

    @Test
    public void testCreateItemBasedDataModel() throws Exception {
        DataModel dataModel = factory.createItemBasedDataModel(index, type, 101);
        assertEquals(2, dataModel.getNumUsers());
        assertEquals(3, dataModel.getNumItems());

        PreferenceArray user1 = dataModel.getPreferencesFromUser(1);
        assertEquals(3, user1.getIDs().length);

        PreferenceArray user3 = dataModel.getPreferencesFromUser(3);
        assertEquals(2, user3.getIDs().length);
    }

    @Test
    public void testCreateUserBasedDataModel() throws Exception {
        DataModel dataModel = factory.createUserBasedDataModel(index, type, 1);
        assertEquals(3, dataModel.getNumUsers());
        assertEquals(3, dataModel.getNumItems());

        PreferenceArray user1 = dataModel.getPreferencesFromUser(1);
        assertEquals(3, user1.getIDs().length);

        PreferenceArray user2 = dataModel.getPreferencesFromUser(2);
        assertEquals(1, user2.getIDs().length);

        PreferenceArray user3 = dataModel.getPreferencesFromUser(3);
        assertEquals(2, user3.getIDs().length);
    }
}
