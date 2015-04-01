// package org.elasticsearch.plugin.flavor;

// import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.*;
// import static org.junit.Assert.*;

// import java.util.Map;

// import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
// import org.codelibs.elasticsearch.runner.net.Curl;
// import org.codelibs.elasticsearch.runner.net.CurlResponse;
// import org.elasticsearch.action.index.IndexResponse;
// import org.elasticsearch.action.search.SearchResponse;
// import org.elasticsearch.common.settings.ImmutableSettings.Builder;
// import org.elasticsearch.common.xcontent.XContentBuilder;
// import org.elasticsearch.common.xcontent.XContentFactory;

// import org.junit.After;
// import org.junit.Before;
// import org.junit.Test;

// public class FlavorPluginTest {
//     private ElasticsearchClusterRunner runner;

//     @Before
//     public void setUp() throws Exception {
//         runner = new ElasticsearchClusterRunner();
//         // Cluster Runnerの初期化
//         runner.onBuild(new ElasticsearchClusterRunner.Builder() {
//             @Override
//             public void build(final int number, final Builder settingsBuilder) {
//                 // elasticsearch.ymlに書くような内容を記述
//                 settingsBuilder.put("http.cors.enabled", true);
//                 settingsBuilder.put("index.number_of_replicas", 0);
//             }
//             // Elasticsearchのノード数を1に設定
//         }).build(newConfigs().ramIndexStore().numOfNode(1));
//         runner.ensureYellow();

//         runner.createIndex("suzuri", null);
//         runner.ensureYellow();

//         // create a mapping
//         final XContentBuilder mappingBuilder = XContentFactory.jsonBuilder()
//             .startObject()
//             .startObject("product")
//             .startObject("properties")
//             // id
//             .startObject("id")
//             .field("type", "string")
//             .field("index", "not_analyzed")
//             .endObject()
//             // title
//             .startObject("title")
//             .field("type", "string")
//             .endObject();
//         runner.createMapping("suzuri", "product", mappingBuilder);

//         runner.insert("suzuri", "product", "1", "{\"id\": 1\", \"title\": \"hoge\"}");
//         runner.insert("suzuri", "product", "2", "{\"id\": 2\", \"title\": \"fuga\"}");
//         runner.insert("suzuri", "product", "3", "{\"id\": 3\", \"title\": \"moge\"}");
//         runner.refresh();
        
//     }

//     @After
//     public void cleanUp() throws Exception {
//         runner.close();
//         runner.clean();
//     }

//     @Test
//     public void testFoundSimilarItems() throws Exception {
//         CurlResponse curlResponse = Curl.get(runner.node(), "/suzuri/_flavor/similar_items/product/1").execute();

//         assertEquals(200, curlResponse.getHttpStatusCode());
//         // レスポンスで返却されたJSONをMapで取得
//         Map<String, Object> content = curlResponse.getContentAsMap();
//         // {"size":1} が返却される想定
//         assertEquals(1, content.get("size"));
//     }
// }
