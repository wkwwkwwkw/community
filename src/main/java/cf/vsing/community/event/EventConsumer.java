package cf.vsing.community.event;

import cf.vsing.community.entity.Article;
import cf.vsing.community.entity.Event;
import cf.vsing.community.entity.Info;
import cf.vsing.community.service.InfoService;
import cf.vsing.community.service.SearchService;
import cf.vsing.community.util.StatusUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class EventConsumer {
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final InfoService infoService;
    private final SearchService searchService;

    @Autowired
    public EventConsumer(InfoService infoService, SearchService searchService) {
        this.infoService = infoService;
        this.searchService = searchService;
    }

    @KafkaListener(topics = {StatusUtil.TOPIC_COMMENT, StatusUtil.TOPIC_LIKE, StatusUtil.TOPIC_FOLLOW})
    public void handleInfo(ConsumerRecord<String,String> record) {
        if (record == null || record.value() == null) {
            log.error("[消息队列]   消息为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            log.error("[消息队列]   消息为空！");
            return;
        }
        //发送站内通知
        Info information = new Info();
        information.setFromId(event.getFromId());
        information.setToId(event.getToId());
        information.setEvent(event.getEvent());
        information.setCreateTime(new Date());
        information.setContent(JSONObject.toJSONString(event.getData()));

        infoService.sendInfo(information);

    }

    @KafkaListener(topics = {StatusUtil.TOPIC_ARTICLE})
    public void handleService(ConsumerRecord<String,String> record) {
        if (record == null || record.value() == null) {
            log.error("[消息队列]   消息为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            log.error("[消息队列]   消息为空！");
            return;
        }
        if (event.getData().containsKey("article")) {
            searchService.save(JSON.parseObject(event.getData().get("article").toString(), Article.class));
        } else {
            searchService.update(event.getData(), String.valueOf(event.getToId()));
        }

    }
}
