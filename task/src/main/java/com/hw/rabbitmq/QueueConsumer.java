package com.hw.rabbitmq;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.amqp.core.Queue;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class QueueConsumer {
    Logger logger = Logger.getLogger(QueueConsumer.class.getName());

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
        return new MessageHandler("Consumer1", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> queue2Sink(StreamBridge streamBridge) {
        return new MessageHandler("Consumer2", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> queue3Sink(StreamBridge streamBridge) {
        return new MessageHandler("Consumer3", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> failedSink() {
        return in -> {
            logger.info("FAILED sink: headers = " + in.getHeaders() + ", payload = " + in.getPayload());
        };
    }

    @Bean
    public Consumer<Message<String>> deadletterSink() {
        return in -> {
            logger.info("DEADLETTER sink: headers = " + in.getHeaders() + ", payload = " + in.getPayload());
        };
    }

    private static class MessageHandler implements Consumer<Message<String>> {
        Logger log = Logger.getLogger(MessageHandler.class.getName());

        private final String consumerName;
        private final StreamBridge streamBridge;

        public MessageHandler(String consumerName, StreamBridge streamBridge) {
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
                    streamBridge.send("failed-out-0", MessageBuilder.withPayload(in.getPayload()).build());

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
    }

    private static void safeSleep(Duration duration) {
        try {
            Thread.sleep(duration.getSeconds() * 1000);
        } catch (InterruptedException e) {
            //won't do
        }
    }
}
