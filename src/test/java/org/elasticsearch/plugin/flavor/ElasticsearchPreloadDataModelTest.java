package org.elasticsearch.plugin.flavor;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.elasticsearch.common.settings.Settings.Builder;

import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;



import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs;
import static org.junit.Assert.*;

public class ElasticsearchPreloadDataModelTest {
    private final static Logger LOGGER = Logger.getLogger(ElasticsearchPreloadDataModelTest.class.getName());
    
    private ElasticsearchClusterRunner runner;
    private ElasticsearchPreloadDataModel dataModel;
    private final String index = "myindex";
    private final String type  = "preference";

    @Before
    public void setUp() throws Exception {
        runner = new ElasticsearchClusterRunner();
        runner.onBuild(new ElasticsearchClusterRunner.Builder() {
            @Override
            public void build(final int number, final Builder settingsBuilder) {
                // elasticsearch.ymlに書くような内容を記述
                settingsBuilder.put("http.cors.enabled", true);
                settingsBuilder.put("index.number_of_replicas", 0);
            }
        }).build(newConfigs().numOfNode(1));

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
