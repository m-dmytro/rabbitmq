package com.hw.rabbitmq.messaging;

import com.hw.rabbitmq.service.UnprocessedMessageService;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.hw.rabbitmq.messaging.MessageProducer.ROUTING_KEY_HEADER;

public class FailedMessageHandler implements Consumer<Message<String>> {
    Logger log = Logger.getLogger(FailedMessageHandler.class.getName());

    private final UnprocessedMessageService unprocessedMessageService;
    private final String source;

    public FailedMessageHandler(UnprocessedMessageService unprocessedMessageService, String source) {
        this.unprocessedMessageService = unprocessedMessageService;
        this.source = source;
    }

    @Override
    public void accept(Message<String> in) {
        unprocessedMessageService.saveUnprocessedMessage(String.valueOf(in.getHeaders().get("uuid")), source, String.valueOf(in.getHeaders().get(ROUTING_KEY_HEADER)), in.getPayload());
    }

}
