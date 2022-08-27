package com.hw.rabbitmq;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class QueueConsumer {
    Logger logger = Logger.getLogger(QueueConsumer.class.getName());

    @Bean
    @ConditionalOnProperty(name = "republish", havingValue = "true")
    public DeclarableCustomizer declarableCustomizer() {
        return declarable -> {
            if (declarable instanceof Queue) {
                var queue = (Queue) declarable;
                if (queue.getName().equals("stream-queue1") || queue.getName().equals("stream-queue2") || queue.getName().equals("stream-queue3")) {
                    queue.removeArgument("x-dead-letter-exchange");
                    queue.removeArgument("x-dead-letter-routing-key");

                }
            }
            return declarable;
        };
    }

    @Bean
    public Consumer<Message<String>> queue1Sink() {
        return new MessageHandler("Consumer1");
    }

    @Bean
    public Consumer<Message<String>> queue2Sink() {
        return new MessageHandler("Consumer2");
    }

    @Bean
    public Consumer<Message<String>> queue3Sink() {
        return new MessageHandler("Consumer3");
    }

    private static class MessageHandler implements Consumer<Message<String>> {
        Logger log = Logger.getLogger(MessageHandler.class.getName());

        private final String consumerName;

        public MessageHandler(String consumerName) {
            this.consumerName = consumerName;
        }

        @Override
        public void accept(Message<String> in) {
            log.info(consumerName + ", headers = " + in.getHeaders() + ", payload = " + in.getPayload());

            /* Check Retry Exchanges/Queues logic on the example of the Consumer1 */
            if (consumerName.equals("Consumer1")) {
                var deathHeader = in.getHeaders().get("x-death", List.class);
                var death = deathHeader != null && deathHeader.size() > 0
                        ? (Map<String, Object>) deathHeader.get(0)
                        : null;
                if (death != null && (long) death.get("count") > 2) {
                    // giving up - don't send to DLX
                    throw new ImmediateAcknowledgeAmqpException(consumerName + ": Failed to process message : '" + in.getPayload() + "' after 3 attempts");
                }
                // nack and do not re-queue
                throw new AmqpRejectAndDontRequeueException(consumerName + ": failed to process message : '" + in.getPayload() + "'");
            }
        }
    }
}
