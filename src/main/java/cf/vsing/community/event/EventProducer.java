package cf.vsing.community.event;

import cf.vsing.community.entity.Event;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    private final KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    EventProducer(KafkaTemplate<String,String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }
    public void fireEvent(Event event) {
        kafkaTemplate.send(event.getEvent(), JSONObject.toJSONString(event));
    }

}
