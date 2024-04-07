package cf.vsing.community.config;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    //注入IOC容器
    @Bean
    public ElasticsearchClient esClient() {
        RestClient httpClient = RestClient.builder(
                new HttpHost("localhost", 9200)
        ).build();  // 创建 HLRC

        ElasticsearchTransport transport = new RestClientTransport(
                httpClient,
                new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
