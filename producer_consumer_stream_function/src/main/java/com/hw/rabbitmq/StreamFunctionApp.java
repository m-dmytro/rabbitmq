package com.hw.rabbitmq;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;
import java.util.logging.Logger;

@SpringBootApplication
public class StreamFunctionApp {
    Logger logger = Logger.getLogger(StreamFunctionApp.class.getName());

    private static final String ROUTING_KEY_HEADER = "myRoutingKey";

    public static void main(String[] args) {
        SpringApplication.run(StreamFunctionApp.class, args);
    }

    @Bean
    public ApplicationRunner runner(StreamBridge streamBridge) {
        return args -> {
            streamBridge.send( "source-out-0",
                    MessageBuilder
                            .withPayload("Pizza;Cake")
                            .setHeader(ROUTING_KEY_HEADER, "routing-queue-1")
                            .build()
            );
            streamBridge.send("source-out-0",
                    MessageBuilder
                            .withPayload("Milk;Juice")
                            .setHeader(ROUTING_KEY_HEADER, "routing-queue-2")
                            .build()
            );
        };
    }

    @Bean
    public Consumer<String> queue1Sink() {
        return payload -> logger.info("consumer1" + " payload = " + payload);
    }

    @Bean
    public Consumer<String> queue2Sink() {
        return payload -> logger.info("consumer2" + " payload = " + payload);
    }

}
