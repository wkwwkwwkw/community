package cf.vsing.community;

import cf.vsing.community.dao.*;
import cf.vsing.community.entity.*;
import cf.vsing.community.event.EventProducer;
import cf.vsing.community.service.ArticleService;
import cf.vsing.community.service.CommentService;
import cf.vsing.community.service.SearchService;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public final class MapperTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private InfoMapper infoMapper;
    @Autowired
    private MessageMapper messageMapper;

    int offset = 0, limit = 3;


    @Autowired
    private SearchService searchService;
    @Autowired
    private ElasticsearchClient esClient;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private EventProducer eventProducer;

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
                            .properties("name", Property.of(p -> p
                                    .text(TextProperty.of(l -> l
                                            .analyzer("ik_max_word")
                                            .searchAnalyzer("ik_smart")))))
                            .properties("createdDateTime", Property.of(p ->
                                    p.date(DateProperty.of(l -> l
                                            .format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")))))
                            .properties("lastModifiedDateTime", Property.of(p ->
                                    p.date(DateProperty.of(l -> l
                                            .format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")))))
                            .properties("eTag", Property.of(p ->
                                    p.text(TextProperty.of(l -> l
                                            .index(false)))))
                            .properties("cTag", Property.of(p ->
                                    p.text(TextProperty.of(l -> l
                                            .index(false)))))
                            .properties("size", Property.of(p ->
                                    p.long_(LongNumberProperty.of(l -> l
                                            .index(false)))))
                            .properties("parentReference", Property.of(p ->
                                    p.object(ObjectProperty.of(l -> l
                                            .properties("id", Property.of(s ->
                                                    s.text(TextProperty.of(n ->
                                                            n.index(false)))))
                                            .properties("path", Property.of(h ->
                                                    h.text(TextProperty.of(o -> o
                                                            .analyzer("ik_max_word")
                                                            .searchAnalyzer("ik_smart")))))))))
                            .properties("file", Property.of(p ->
                                    p.object(ObjectProperty.of(l -> l
                                            .properties("childCount", Property.of(s ->
                                                    s.long_(LongNumberProperty.of(n ->
                                                            n.index(false)))))
                                            .properties("mimeType", Property.of(s ->
                                                    s.text(TextProperty.of(n ->
                                                            n.index(false)))))
                                            .properties("quickXorHash", Property.of(s ->
                                                    s.text(TextProperty.of(n ->
                                                            n.index(false)))))))))
                            .properties("extra", Property.of(p ->
                                    p.text(TextProperty.of(l -> l
                                            .index(false)))))

                    )
            );
            return response.acknowledged();
        } catch (IOException e) {
            String message = DateUtils.formatYMDHMS19(new Date()) + "[ERROR](Elasticsearch) 索引创建失败: createIndex(String index) 方法抛出IOException\n";
            String args = String.format("参数列表:[ index : '%s' ]:+\n", index);
            return false;
        }
    }

    private class Item {
        String id;
        String name;
        String cTag;
        String eTag;
        Date createdDateTime;
        Date lastModifiedDateTime;
        long size;
        Map<String, Object> parentReference;
        Map<String, Object> file;
        String extra;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getcTag() {
            return cTag;
        }

        public String geteTag() {
            return eTag;
        }

        public Date getCreatedDateTime() {
            return createdDateTime;
        }

        public Date getLastModifiedDateTime() {
            return lastModifiedDateTime;
        }

        public long getSize() {
            return size;
        }

        public Map<String, Object> getParentReference() {
            return parentReference;
        }

        public Map<String, Object> getFile() {
            return file;
        }

        public String getExtra() {
            return extra;
        }

        public Item(String id, String name, String cTag, String eTag, Date createdDateTime, Date lastModifiedDateTime, long size, Map<String, Object> parentReference, Map<String, Object> file, String extra) {
            this.id = id;
            this.name = name;
            this.cTag = cTag;
            this.eTag = eTag;
            this.createdDateTime = createdDateTime;
            this.lastModifiedDateTime = lastModifiedDateTime;
            this.size = size;
            this.parentReference = parentReference;
            this.file = file;
            this.extra = extra;
        }
    }

    public boolean save(Item item) {
        if (item == null)
            return false;
        IndexResponse indexResponse = null;
        try {
            indexResponse = esClient.index(s -> s
                    .index("onedrive")
                    .id(String.valueOf(item.getId()))
                    .document(item)
            );
            return indexResponse.result().toString().equals("Created");
        } catch (IOException e) {
            String message = DateUtils.formatYMDHMS19(new Date()) + "[ERROR](Elasticsearch) 文档保存时失败: save(Article article,String index) 方法抛出IOException\n";
            String args = String.format("参数列表:[ %s ]:+\n", item);
            System.out.println(message + args + e);
            return false;
        }
    }

    @Test
    public void kafka() throws IOException {
        File fp = new File("D:/desktop/response.json");
        String temp = JSONUtil.readJSON(fp, StandardCharsets.UTF_8).toJSONString(0);
        JSONObject book = JSONObject.parse(temp);
        List<JSONObject> fileSet = book.getList("values", JSONObject.class);
        //createIndex("onedrive");
        for (JSONObject file : fileSet) {
            Map<String, Object> parentReference = new HashMap<>();
            Map<String, Object> f = new HashMap<>();
            String extra;
            parentReference.put("id", file.getJSONObject("parentReference").getString("id"));
            parentReference.put("path", file.getJSONObject("parentReference").getString("path"));
            f.put("childCount", file.containsKey("folder") ? file.getJSONObject("folder").getIntValue("childCount") : 0);
            f.put("mimeType", file.containsKey("file") ? file.getJSONObject("file").getString("mimeType") : "");
            f.put("quickXorHash", file.containsKey("file") ? file.getJSONObject("file").getString("quickXorHash") : "");
            if (file.containsKey("video")) {
                extra = "{\"video\":" + file.getJSONObject("video").toJSONString() + "}";
            } else if (file.containsKey("audio")) {
                extra = "{\"audio\":" + file.getJSONObject("audio").toJSONString() + "}";
            } else if (file.containsKey("image")) {
                extra = "{\"image\":" + file.getJSONObject("image").toJSONString() + "}";
            } else if (file.containsKey("location")) {
                extra = "{\"location\":" + file.getJSONObject("location").toJSONString() + "}";
            } else if (file.containsKey("package")) {
                extra = "{\"package\":" + file.getJSONObject("package").toJSONString() + "}";
            } else if (file.containsKey("photo")) {
                extra = "{\"photo\":" + file.getJSONObject("photo").toJSONString() + "}";
            } else {
                extra = "";
            }

            Item item = new Item(
                    file.getString("id"),
                    file.getString("name"),
                    file.getString("cTag"),
                    file.getString("eTag"),
                    file.getDate("createdDateTime"),
                    file.getDate("lastModifiedDateTime"),
                    file.getLongValue("size"),
                    parentReference, f, extra);

            save(item);
        }


    }

    @Test//OK
    public void UserTest() {
        int id = 150;
        String name = "www";
        String email = "nowcoder132@sina.com";
        String phone = "17838648458";
        int authority = 1;
        int status = 2;
        User test = new User()
                .setPassword("123")
                .setSalt("123")
                .setAuthority(1)
                .setStatus(1)
                .setName("UserTest")
                .setHeader("https://p.vsing.cf/header/ebucas1ef")
                .setDetail("Test")
                .setEmail("1471241365@qq.com")
                .setPhone("17838648457")
                .setCreateTime(new Date())
                .setActivationCode("abcd");


        System.out.println("\n--------------------------------INSERT------------------------------\n");
        userMapper.insertUser(test);
        System.out.println("InsertUser(Test):\n" + userMapper.selectById(test.getId()));

        System.out.println("\n--------------------------------DELETE------------------------------\n");


        System.out.println("\n--------------------------------SELECT------------------------------\n");
        System.out.println("SelectMaxId():" + userMapper.selectMaxId());
        System.out.println("SelectById(" + id + "):\n" + userMapper.selectById(id));
        System.out.println("SelectByName(" + name + "):\n" + userMapper.selectByName(name));
        System.out.println("SelectByEmail(" + email + "):\n" + userMapper.selectByEmail(email));
        System.out.println("SelectByPhone(" + phone + "):\n" + userMapper.selectByPhone(phone));
        System.out.println("SelectByAuthority(" + authority + "):\n" + userMapper.selectByAuthority(authority));
        System.out.println("SelectByStatus(" + status + "):\n" + userMapper.selectByStatus(status));

        System.out.println("\n--------------------------------UPDATE------------------------------\n");
        userMapper.updateAuthority(test.getId(), 2);
        System.out.println("UpdateAuthority(" + test.getId() + ",2):" + userMapper.selectById(test.getId()).getAuthorities());
        userMapper.updateDetail(test.getId(), "Test(Changed)");
        System.out.println("UpdateDetail(" + test.getId() + ",\"Test(Changed)\"):" + userMapper.selectById(test.getId()).getDetail());
        userMapper.updateEmail(test.getId(), "2089844544@qq.com(Changed)");
        System.out.println("UpdateEmail(" + test.getId() + ",\"2089844544@qq.com(Changed)\"):" + userMapper.selectById(test.getId()).getEmail());
        userMapper.updateHeader(test.getId(), "https://p.vsing.cf/header/avdhavyeu(Changed)");
        System.out.println("UpdateHeader(" + test.getId() + ",\"https://p.vsing.cf/header/avdhavyeu(Changed)\"):" + userMapper.selectById(test.getId()).getHeader());
        userMapper.updateName(test.getId(), "UsrTs(Cgd)");
        System.out.println("UpdateName(" + test.getId() + ",\"UsrTs(Cgd)\"):" + userMapper.selectById(test.getId()).getName());
        userMapper.updatePassword(test.getId(), "456(Changed)");
        System.out.println("UpdatePassword(" + test.getId() + ",\"456(Changed)\"):" + userMapper.selectById(test.getId()).getPassword());
        userMapper.updatePhone(test.getId(), "17516085371");
        System.out.println("UpdatePhone(" + test.getId() + ",17516085371):" + userMapper.selectById(test.getId()).getPhone());
        userMapper.updateStatus(test.getId(), 2);
        System.out.println("UpdateStatus(" + test.getId() + ",2):" + userMapper.selectById(test.getId()).getStatus());

        System.out.println("\n-------------------------------SPECIAL------------------------------\n");
    }

    @Test//OK
    public void ArticleTest() {
        int aid = 270;
        int uid = 150;
        Article test = new Article()
                .setId(999)
                .setTitle("ArticleMapper Test")
                .setContent("tttttttttttttt")
                .setUserId(999)
                .setStatus(1)
                .setCreateTime(new Date())
                .setCommentCount(1)
                .setType(1);


        System.out.println("\n--------------------------------INSERT------------------------------\n");
        articleMapper.insertArticle(test);
        System.out.println("InsertArticle(Test):\n" + articleMapper.selectArticleById(test.getId()));

        System.out.println("\n--------------------------------DELETE------------------------------\n");


        System.out.println("\n--------------------------------SELECT------------------------------\n");
        System.out.println("SelectMaxId():" + articleMapper.selectMaxId());
        System.out.println("SelectArticleById(" + aid + "):\n" + articleMapper.selectArticleById(aid));
        System.out.println("SelectArticleByUserId(0" + "," + offset + "," + limit + "):\n" + Arrays.toString(articleMapper.selectArticleByUserId(0, offset, limit).toArray()));
        System.out.println("SelectArticleByUserId(" + uid + "," + offset + "," + limit + "):\n" + Arrays.toString(articleMapper.selectArticleByUserId(uid, offset, limit).toArray()));

        System.out.println("\n--------------------------------UPDATE------------------------------\n");
        articleMapper.updateCommentCount(test.getId(), 2);
        System.out.println("UpdateCommentCount(" + test.getId() + ",2):" + articleMapper.selectArticleById(test.getId()).getCommentCount());

        System.out.println("\n-------------------------------SPECIAL------------------------------\n");
        System.out.println("SelectArticleRows(" + uid + "):" + articleMapper.selectArticleRows(uid));
        System.out.println("SelectArticleRows(0):" + articleMapper.selectArticleRows(0));
    }

    @Test//OK
    public void CommentTest() {
        int cid = 235;
        int eid = 232;
        int etp = 2;
        Comment test = new Comment()
                .setId(999)
                .setEntityId(1)
                .setEntityType(1)
                .setUserId(999)
                .setTargetId(1000)
                .setStatus(1)
                .setContent("CommentMapper Test")
                .setCreateTime(new Date());


        System.out.println("\n--------------------------------INSERT------------------------------\n");
        commentMapper.insertComment(test);
        System.out.println("InsertComment(Test):\n" + commentMapper.selectById(test.getId()));

        System.out.println("\n--------------------------------DELETE------------------------------\n");

        System.out.println("\n--------------------------------SELECT------------------------------\n");
        System.out.println("SelectMaxId():" + commentMapper.selectMaxId());
        System.out.println("SelectById(" + cid + "):\n" + commentMapper.selectById(cid));
        System.out.println("SelectByEntityId(" + etp + "," + eid + "," + offset + "," + limit + "):\n" + Arrays.toString(commentMapper.selectByEntityId(etp, eid, offset, limit).toArray()));

        System.out.println("\n--------------------------------UPDATE------------------------------\n");

        System.out.println("\n-------------------------------SPECIAL------------------------------\n");
        System.out.println("CountByEntityId(" + etp + "," + eid + "):\n" + commentMapper.countByEntityId(etp, eid));

    }

    @Test
    public void InfoTest() {
        int uid = 1;
        int iid = 1;
        String event = "comment";
        List<Integer> ids = new ArrayList<>();
        ids.add(7);
        ids.add(8);
        ids.add(9);
        Info test = new Info()
                .setFromId(1)
                .setToId(2)
                .setEvent("test")
                .setContent("tttttttest")
                .setStatus(0)
                .setCreateTime(new Date());


        System.out.println("\n--------------------------------INSERT------------------------------\n");
        infoMapper.insertInfo(test);
        System.out.println("InsertInfo(Test):\n" + infoMapper.selectEvent(test.getToId(), offset, limit));

        System.out.println("\n--------------------------------DELETE------------------------------\n");


        System.out.println("\n--------------------------------SELECT------------------------------\n");
        System.out.println("SelectMaxId()" + infoMapper.selectMaxId());
        System.out.println("SelectEvent(" + uid + "," + offset + "," + limit + "):\n" + infoMapper.selectEvent(uid, offset, limit));
        System.out.println("SelectDetail(" + uid + "," + offset + "," + limit + "):\n" + infoMapper.selectDetail(uid, event, offset, limit));

        System.out.println("\n--------------------------------UPDATE------------------------------\n");
        System.out.println("updateStatus(" + ids + ",1):\n" + infoMapper.updateStatus(ids, 1));


        System.out.println("\n-------------------------------SPECIAL------------------------------\n");
        System.out.println("countEvent(" + uid + "):\n" + infoMapper.countEvent(uid));
        System.out.println("countDetail(" + uid + ",\'comment\'):\n" + infoMapper.countDetail(uid, "comment"));
        System.out.println("countUnread(" + uid + ",\'comment\'):\n" + infoMapper.countUnread(uid, "comment"));
    }

    @Test
    public void MessageTest() {
        int uid = 1;
        int iid = 1;
        String conversationId = uid < iid ? uid + "_" + iid : iid + "_" + uid;
        List<Integer> ids = new ArrayList<>();
        ids.add(7);
        ids.add(8);
        ids.add(9);
        Message test = new Message()
                .setFromId(uid)
                .setToId(iid)
                .setConversationId(conversationId)
                .setContent("Test")
                .setStatus(0)
                .setCreateTime(new Date());


        System.out.println("\n--------------------------------INSERT------------------------------\n");
        messageMapper.insertMessage(test);
        List<Message> result = messageMapper.selectDetail(test.getConversationId(), offset, limit);
        System.out.println("InsertMessage(Test):\n" + result);

        System.out.println("\n--------------------------------DELETE------------------------------\n");

        System.out.println("\n--------------------------------SELECT------------------------------\n");
        System.out.println("SelectMaxId()" + infoMapper.selectMaxId());
        System.out.println("SelectConversation(" + uid + "," + offset + "," + limit + "):\n" + messageMapper.selectConversation(uid, offset, limit));
        System.out.println("SelectDetail(" + uid + "," + conversationId + "," + offset + "," + limit + "):\n" + messageMapper.selectDetail(conversationId, offset, limit));

        System.out.println("\n--------------------------------UPDATE------------------------------\n");
        System.out.println("updateStatus(" + ids + ",1):\n" + messageMapper.updateStatus(ids, 1));

        System.out.println("\n-------------------------------SPECIAL------------------------------\n");
        System.out.println("countConversation(" + uid + "):\n" + messageMapper.countConversation(uid));
        System.out.println("countDetail(" + conversationId + "):\n" + messageMapper.countDetail(conversationId));
        System.out.println("countUnread(" + uid + "," + conversationId + "):\n" + messageMapper.countUnread(uid, conversationId));
    }
}