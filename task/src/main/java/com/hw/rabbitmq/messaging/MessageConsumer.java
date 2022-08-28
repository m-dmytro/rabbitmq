package com.hw.rabbitmq.messaging;

import com.hw.rabbitmq.service.UnprocessedMessageService;
import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class MessageConsumer {
    Logger logger = Logger.getLogger(MessageConsumer.class.getName());

    @Autowired
    private UnprocessedMessageService unprocessedMessageService;

    @Bean
    public DeclarableCustomizer declarableCustomizer() {
        return declarable -> {
            if (declarable instanceof Queue) {
                var queue = (Queue) declarable;
                if (queue.getName().equals("stream-queue1") || queue.getName().equals("stream-queue2") || queue.getName().equals("stream-queue3")) {
                    queue.removeArgument("x-dead-letter-exchange");
                    queue.removeArgument("x-dead-letter-routing-key");

                    queue.addArgument("x-dead-letter-exchange", "deadletter-exchange");
                }
            }
            return declarable;
        };
    }

    @Bean
    public Consumer<Message<String>> queue1Sink(StreamBridge streamBridge) {
        return new RegularMessageHandler("Consumer1", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> queue2Sink(StreamBridge streamBridge) {
        return new RegularMessageHandler("Consumer2", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> queue3Sink(StreamBridge streamBridge) {
        return new RegularMessageHandler("Consumer3", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> failedSink() {
        return new FailedMessageHandler(unprocessedMessageService, "FAILED sink");
    }

    @Bean
    public Consumer<Message<String>> deadletterSink() {
        return new FailedMessageHandler(unprocessedMessageService, "DEADLETTER sink");
    }

}
