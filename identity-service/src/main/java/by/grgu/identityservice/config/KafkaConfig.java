package by.grgu.identityservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

//    @Bean
//    NewTopic createTopic(){
//        return TopicBuilder.name("auth-user-got-events-topic")
//                .partitions(3)
//                .replicas(3)
//                .configs(Map.of("min.insync.replicas","2")) // 1 должен быть в синхроне с лидером (2 - это лидер и фоловер)
//                .build();
//    }
}
