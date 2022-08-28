package com.hw.rabbitmq.messaging;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.hw.rabbitmq.messaging.MessageProducer.ROUTING_KEY_HEADER;
import static com.hw.rabbitmq.messaging.MessageProducer.UUID_HEADER;

public class RegularMessageHandler implements Consumer<Message<String>> {

    Logger log = Logger.getLogger(RegularMessageHandler.class.getName());

    private final String consumerName;
    private final StreamBridge streamBridge;

    public RegularMessageHandler(String consumerName, StreamBridge streamBridge) {
        this.consumerName = consumerName;
        this.streamBridge = streamBridge;
    }

    @Override
    public void accept(Message<String> in) {
        log.info(consumerName + ", headers = " + in.getHeaders() + ", payload = " + in.getPayload());

        /* Check Retry Exchanges/Queues logic - an exception occurs when the server is processing a message
            (on the example of the Consumer1) */
        if (consumerName.equals("Consumer1")) {
            var deathHeader = in.getHeaders().get("x-death", List.class);
            var death = deathHeader != null && deathHeader.size() > 0
                    ? (Map<String, Object>) deathHeader.get(0)
                    : null;
            if (death != null && (long) death.get("count") > 2) {
                // giving up - don't send to DLX, instead send to failed exchange
                streamBridge.send("failed-out-0", MessageBuilder
                        .withPayload(in.getPayload())
                        .setHeader(ROUTING_KEY_HEADER, in.getHeaders().get(ROUTING_KEY_HEADER))
                        .setHeader(UUID_HEADER, in.getHeaders().get(UUID_HEADER))
                        .build());

                throw new ImmediateAcknowledgeAmqpException(consumerName + ": Failed to process message : '" + in.getPayload() + "' after 3 attempts");
            }
            // nack and do not re-queue
            throw new AmqpRejectAndDontRequeueException(consumerName + ": failed to process message : '" + in.getPayload() + "'");
        }

        /* Check DeadLetter Queue logic - when the server processes the message for a long time and does not return a response
            (on the example of the Consumer2) */
        if (consumerName.equals("Consumer2")) {
            safeSleep(Duration.ofSeconds(5));
        }
    }

    private static void safeSleep(Duration duration) {
        try {
            Thread.sleep(duration.getSeconds() * 1000);
        } catch (InterruptedException e) {
            //won't do
        }
    }

}
