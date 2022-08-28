package com.hw.rabbitmq.controller;

import com.hw.rabbitmq.domain.UnprocessedMessage;
import com.hw.rabbitmq.domain.UnprocessedMessageState;
import com.hw.rabbitmq.messaging.MessageProducer;
import com.hw.rabbitmq.service.UnprocessedMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1")
public class PubController {
    Logger logger = Logger.getLogger(PubController.class.getName());

    @Autowired
    MessageProducer producer;
    @Autowired
    private UnprocessedMessageService unprocessedMessageService;

    @PostMapping("/send/{message}")
    public void sendMessageToQueue(@PathVariable("message") String message) {
        logger.info("Message received at the endpoint: '" + message + "'");
        producer.sendMessageToQueues(message);
    }

    @GetMapping("/resend_failed_messages")
    public void resendFailedMessages() {
        logger.info("Resend failed messages");
        List<UnprocessedMessage> failedMessages = unprocessedMessageService.getMessages();
        failedMessages.forEach(message -> {
            producer.sendMessage(message.getRoutingKey(), message.getRoutingKey(), message.getUuid());
            unprocessedMessageService.updateUnprocessedMessageState(message.getUuid(), UnprocessedMessageState.ARCHIVED);
            logger.info("Failed messages was resend to the queue: uuid " + message.getUuid() + ", routing key " + message.getRoutingKey() + ", message '" + message.getMessage() + "'");
        });
    }
}
