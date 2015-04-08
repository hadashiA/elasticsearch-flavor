package org.elasticsearch.plugin.flavor;

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.env.FailedToResolveConfigException;
import org.elasticsearch.plugins.PluginsService;

import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import org.elasticsearch.plugin.flavor.ElasticsearchPreloadDataModel;

import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs;
import static org.junit.Assert.*;

public class ElasticsearchPreloadDataModelTest {
    private final static Logger LOGGER = Logger.getLogger(ElasticsearchPreloadDataModelTest.class.getName());
    
    private ElasticsearchClusterRunner runner = new ElasticsearchClusterRunner();
    private ElasticsearchPreloadDataModel dataModel;
    private String index = "myindex";
    private String type  = "preference";

    @Before
    public void setUp() throws Exception {
        runner.onBuild(new ElasticsearchClusterRunner.Builder() {
            @Override
            public void build(final int number, final Builder settingsBuilder) {
                // elasticsearch.ymlに書くような内容を記述
                settingsBuilder.put("http.cors.enabled", true);
                settingsBuilder.put("index.number_of_replicas", 0);
            }
        }).build(newConfigs().ramIndexStore().numOfNode(1));

        // クラスタ状態がYellowになるのを待つ
        runner.ensureYellow();

        runner.createIndex(index, null);
        for (int i = 1; i <= 1000; i++) {
            final String source = "{\"user_id\": " + i + ", \"item_id\":" + i % 100 + ", \"value\":" + (i % 10 / 5) + "}";
            
            runner.insert(index, type, String.valueOf(i), source);
        }
        runner.refresh();
        this.dataModel = new ElasticsearchPreloadDataModel(runner.client(), index, type);
        dataModel.reload();
    }

    @After
    public void cleanUp() throws Exception {
        runner.close();
        runner.clean();
    }

    @Test
    public void testReload() throws Exception {
        assertEquals(1000, dataModel.getNumUsers());
        assertEquals(100, dataModel.getNumItems());
    }
}
