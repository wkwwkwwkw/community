package cf.vsing.community.service;

import cf.vsing.community.entity.Article;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.alibaba.fastjson2.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
    private final ElasticsearchClient esClient;
    @Autowired
    SearchService(ElasticsearchClient esClient){
        this.esClient = esClient;
    }
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    public boolean createIndex(String index) {
        try {
            if (esClient.indices().exists(e -> e.index(index)).value()) {
                return true;
            }
            CreateIndexResponse response = esClient.indices().create(c -> c
                    .index(index)
                    .mappings(m -> m
                            .properties("id", Property.of(p ->
                                    p.text(TextProperty.of(l -> l
                                            .index(false)))))
                            .properties("userId", Property.of(p ->
                                    p.integer(IntegerNumberProperty.of(l -> l
                                            .index(false)))))
                            .properties("title", Property.of(p -> p
                                    .text(TextProperty.of(l -> l
                                            .analyzer("ik_max_word")
                                            .searchAnalyzer("ik_smart")))))
                            .properties("content", Property.of(p -> p
                                    .text(TextProperty.of(l -> l
                                            .analyzer("ik_max_word")
                                            .searchAnalyzer("ik_smart")))))
                            .properties("type", Property.of(p ->
                                    p.integer(IntegerNumberProperty.of(l -> l
                                            .index(false)))))
                            .properties("status", Property.of(p ->
                                    p.integer(IntegerNumberProperty.of(l -> l
                                            .index(false)))))
                            .properties("createTime", Property.of(p -> p
                                    .date(DateProperty.of(l -> l
                                            .format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")))))
                            .properties("commentCount", Property.of(p ->
                                    p.integer(IntegerNumberProperty.of(l -> l
                                            .index(false)))))
                            .properties("score", Property.of(p ->
                                    p.double_(DoubleNumberProperty.of(l -> l
                                            .index(false)))))
                    )
            );
            return response.acknowledged();
        } catch (IOException e) {
            String message = DateUtils.formatYMDHMS19(new Date()) + "[ERROR](Elasticsearch) 索引创建失败: createIndex(String index) 方法抛出IOException\n";
            String args = String.format("参数列表:[ index : '%s' ]:+\n", index);
            log.error(message + args + e);
            return false;
        }
    }

    public boolean save(@NonNull Article article) {
        IndexResponse indexResponse;
        try {
            indexResponse = esClient.index(s -> s
                    .index("article")
                    .id(String.valueOf(article.getId()))
                    .document(article)
            );
            return indexResponse.result().toString().equals("Created");
        } catch (IOException e) {
            String message = DateUtils.formatYMDHMS19(new Date()) + "[ERROR](Elasticsearch) 文档保存时失败: save(Article article,String index) 方法抛出IOException\n";
            String args = String.format("参数列表:[ %s ]:+\n", article);
            log.error(message + args + e);
            return false;
        }
    }

    public boolean update(Map<String, Object> value, String id) {
        if (value.isEmpty()) {
            return true;
        }
        UpdateResponse<Article> response;
        try {
            response = esClient.update(e -> e
                            .index("article")
                            .id(id)
                            .doc(value),
                    Article.class
            );
            return response.result().toString().equals("Updated");
        } catch (IOException e) {
            String message = DateUtils.formatYMDHMS19(new Date()) + "[ERROR](Elasticsearch) 文档更新失败: update(Map<String,Object> value,String id) 方法抛出IOException\n";
            String args = String.format("参数列表:[ value : '%s', id : %s ]:+\n", value, id);
            log.error(message + args + e);
            return false;
        }
    }

    public Map<String, Object> search(String keyword, @Value("red") String highlightColor, int current, int limit, String... field) {
        Map<String, Object> result = new HashMap<>();
        List<Article> articles = new ArrayList<>();
        //空值判断
        if (StringUtils.isBlank(keyword) || limit == 0) {
            result.put("total", 0);
            result.put("articles", articles);
            return result;
        }
        //初始化构建条件
        Query.Builder query = new Query.Builder();
        Highlight.Builder highlight = new Highlight.Builder();

        if (field.length == 1) {
            //单域匹配查询条件构建，只匹配文章标题或正文
            query.match(m -> m
                    .field(field[0])
                    .query(keyword)
                    .analyzer("ik_smart"));
            highlight.fields(field[0], f -> f.preTags("< em style=\"color:" + highlightColor + ";\">").postTags("</em>"));
        } else {
            //多域匹配查询条件构建，同时匹配文章标题或正文
            query.multiMatch(m -> m
                    .fields("title", "content")
                    .query(keyword)
                    .analyzer("ik_smart"));
            highlight.fields("title", f -> f.preTags("<em style=\"color:" + highlightColor + ";\">").postTags("</em>"))
                    .fields("content", f -> f.preTags("<em style=\"color:" + highlightColor + ";\">").postTags("</em>"));
        }
        try {
            //进行查询
            SearchResponse<Article> response = esClient.search(q -> q
                            .index("article")
                            .query(query.build())
                            .highlight(highlight.build())
                            .from(current * limit).size(limit)
                            .sort(s1 -> s1.field(f2 -> f2.field("type")))
                            .sort(s2 -> s2.field(f2 -> f2.field("score")))
                            .sort(s3 -> s3.field(f3 -> f3.field("createTime"))),
                    Article.class
            );
            //高亮处理
            for (Hit<Article> hit : response.hits().hits()) {
                if (hit.source() == null)
                    continue;
                //TODO:消除高亮内容两端括号
                if (hit.highlight().containsKey("title")) {
                    hit.source().setTitle(StringUtils.join(hit.highlight().get("title")));
                }
                if (hit.highlight().containsKey("content")) {
                    hit.source().setContent(StringUtils.join(hit.highlight().get("content")));
                }
                articles.add(hit.source());
            }
            result.put("total", response.hits().total() == null ? 0 : (int) response.hits().total().value());
            result.put("articles", articles);
            return result;
        } catch (IOException e) {
            //异常日志
            String message = DateUtils.formatYMDHMS19(new Date()) + "[ERROR](Elasticsearch) 文档搜索失败: search(String keyword, @Value(\"red\") String highlightColor, String... field) 方法抛出IOException\n";
            String args = String.format("参数列表:[ keyword : '%s', highlightColor : '%s', current : %s, limit : %s, field : '%s' ]:+\n", keyword, highlightColor, current, limit, Arrays.toString(field));
            log.error(message + args + e);
            result.put("total", 0);
            result.put("articles", articles);
            return result;
        }
    }

    public boolean delete(String id) {
        try {
            DeleteResponse response = esClient.delete(s -> s
                    .index("article")
                    .id(id));
            return response.result().toString().equals("Deleted");
        } catch (IOException e) {
            String message = DateUtils.formatYMDHMS19(new Date()) + "[ERROR](Elasticsearch) 文档删除失败: delete(String id) 方法抛出IOException\n";
            String args = String.format("参数列表:[ id : %s ]:+\n", id);
            log.error(message + args + e);
            return false;
        }

    }
}
