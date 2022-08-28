package com.hw.rabbitmq.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class MessageProducer {

    public static final String BINDING_NAME = "source-out-0";
    public static final String ROUTING_KEY_HEADER = "myRoutingKey";
    public static final String UUID_HEADER = "uuid";

    @Autowired
    StreamBridge streamBridge;

    public void sendMessageToQueues(String message) {
        sendMessage("routing-queue-1", message);

        IntStream.range(0, 7).forEach(i -> {
            sendMessage("routing-queue-2", i + ": " + message);
        });

        sendMessage("routing-queue-3", message);
    }

    public void sendMessage(String routingKey, String message) {
        sendMessage(routingKey, message, String.valueOf(UUID.randomUUID()));
    }

    public void sendMessage(String routingKey, String message, String uuid) {
        streamBridge.send(BINDING_NAME,
                MessageBuilder
                        .withPayload(message)
                        .setHeader(ROUTING_KEY_HEADER, routingKey)
                        .setHeader(UUID_HEADER, uuid)
                        .build()
        );
    }

}
