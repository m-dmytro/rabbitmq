package com.hw.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.logging.Logger;

@EnableRabbit
@SpringBootApplication
public class App {
    Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public ApplicationRunner runner(AmqpTemplate amqpTemplate) {
        return args -> {
            amqpTemplate.convertAndSend("amqp-exchange", "routing-queue1", "Pizza;Cake");
            amqpTemplate.convertAndSend("amqp-exchange", "routing-queue2", "Milk;Juice");
        };
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "amqp-queue1", durable = "true"),
                    key = "routing-queue1",
                    exchange = @Exchange(name = "amqp-exchange", type = ExchangeTypes.TOPIC)
            )
    )
    public void listenQueue1(Message<String> in) {
        doListen("consumer1", in);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "amqp-queue2", durable = "true"),
                    key = "routing-queue2",
                    exchange = @Exchange(name = "amqp-exchange", type = ExchangeTypes.TOPIC)
            )
    )
    public void listenQueue2(Message<String> in) {
        doListen("consumer2", in);
    }

    private void doListen(String consumerName, Message<String> in) {
        logger.info(consumerName + ", headers = " + in.getHeaders() + ", payload = " + in.getPayload());
    }

}
