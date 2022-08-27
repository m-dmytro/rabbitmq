package com.hw.rabbitmq;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.logging.Logger;

@SpringBootApplication
@EnableBinding({Source.class, StreamApp.CustomSink.class})
public class StreamApp {
    Logger logger = Logger.getLogger(StreamApp.class.getName());

    private static final String ROUTING_KEY_HEADER = "myRoutingKey";

    public interface CustomSink {
        String QUEUE1 = "input-queue1";

        @Input(QUEUE1)
        SubscribableChannel inputQueue1();

        String QUEUE2 = "input-queue2";

        @Input(QUEUE2)
        SubscribableChannel inputQueue2();
    }

    public static void main(String[] args) {
        SpringApplication.run(StreamApp.class, args);
    }

    @Bean
    public ApplicationRunner runner(Source source) {
        return args -> {
            source.output().send(
                    MessageBuilder
                            .withPayload("Pizza;Cake")
                            .setHeader(ROUTING_KEY_HEADER, "routing-queue-1")
                            .build()
            );
            source.output().send(
                    MessageBuilder
                            .withPayload("Milk;Juice")
                            .setHeader(ROUTING_KEY_HEADER, "routing-queue-2")
                            .build()
            );
        };
    }

    @StreamListener(CustomSink.QUEUE1)
    public void listenQueue1(Message<String> in) {
        doListen("consumer1", in);
    }

    @StreamListener(CustomSink.QUEUE2)
    public void listenQueue2(Message<String> in) {
        doListen("consumer2", in);
    }

    private void doListen(String consumerName, Message<String> in) {
        logger.info(consumerName + ", headers = " + in.getHeaders() + ", payload = " + in.getPayload());

        if (consumerName.equals("consumer1")) {
            throw new RuntimeException("Exception");
        }
    }

}
