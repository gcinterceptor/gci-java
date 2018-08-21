package com.gcinterceptor.elasticsearch.plugin;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.is;

public class IntegrationTest extends ESIntegTestCase {
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singleton(GciPlugin.class);
    }

    @Test
    public void testPluginIsLoaded() throws Exception {
        NodesInfoResponse response = client().admin().cluster().prepareNodesInfo().setPlugins(true).get();
        for (NodeInfo nodeInfo : response.getNodes()) {
            boolean pluginFound = false;
            for (PluginInfo pluginInfo : nodeInfo.getPlugins().getPluginInfos()) {
                if (pluginInfo.getName().equals(GciPlugin.class.getName())) {
                    pluginFound = true;
                    break;
                }
            }
            assertThat(pluginFound, is(true));
        }
    }

    @Test
    public void testSimpleSearch() throws Exception {
        createIndex("index");
        IndexResponse indexResp = client().prepareIndex("index", "type", "1d")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("field", "value")
                        .endObject())
                .get();
        refresh("index");
        assertEquals(RestStatus.CREATED, indexResp.status());
        SearchResponse searchResp = client().prepareSearch()
                .setIndices("index")
                .setTypes("type")
                .setQuery(QueryBuilders.matchAllQuery())
                .get();
        assertEquals(RestStatus.OK, searchResp.status());
        assertEquals(1, searchResp.getHits().getTotalHits());
    }
}
