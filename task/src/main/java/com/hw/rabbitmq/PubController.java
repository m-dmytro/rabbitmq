package com.hw.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/send")
public class PubController {
    Logger logger = Logger.getLogger(PubController.class.getName());

    private static final String ROUTING_KEY_HEADER = "myRoutingKey";

    @Autowired
    StreamBridge streamBridge;

    @PostMapping("/{message}")
    public void sendMessageToQueue(@PathVariable("message") String message) {
        logger.info("Message received at the endpoint: '" + message + "'");

        streamBridge.send( "source-out-0",
                MessageBuilder
                        .withPayload(message)
                        .setHeader(ROUTING_KEY_HEADER, "routing-queue-1")
                        .build()
        );

        IntStream.range(0, 7).forEach(i -> {
            streamBridge.send("source-out-0",
                    MessageBuilder
                            .withPayload(i + ": " + message)
                            .setHeader(ROUTING_KEY_HEADER, "routing-queue-2")
                            .build()
            );
        });

        streamBridge.send("source-out-0",
                MessageBuilder
                        .withPayload(message)
                        .setHeader(ROUTING_KEY_HEADER, "routing-queue-3")
                        .build()
        );
    }
}
