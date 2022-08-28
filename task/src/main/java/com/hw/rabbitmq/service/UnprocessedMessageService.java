package com.hw.rabbitmq.service;

import com.hw.rabbitmq.domain.UnprocessedMessage;
import com.hw.rabbitmq.domain.UnprocessedMessageState;
import com.hw.rabbitmq.repository.UnprocessedMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UnprocessedMessageService {
    Logger log = Logger.getLogger(UnprocessedMessageService.class.getName());

    @Autowired
    private UnprocessedMessageRepository repository;

    public void saveUnprocessedMessage(String uuid, String source, String routingKey, String messageText) {
        UnprocessedMessage storedMessages = repository.findUnprocessedMessageByUuid(uuid);
        if (storedMessages != null) {
            storedMessages.setState(UnprocessedMessageState.DISCARDED);
            repository.save(storedMessages);
            log.info("Unprocessed message has been failed second time and been DISCARDED: routing key: " + storedMessages.getRoutingKey() + ", messageText '" + storedMessages.getMessage() + "', state " + UnprocessedMessageState.DISCARDED);
        } else {
            UnprocessedMessage message = new UnprocessedMessage(uuid, source, routingKey, messageText, UnprocessedMessageState.RETURNED);
            repository.save(message);
            log.info("Unprocessed message is stored: routing key: " + message.getRoutingKey() + ", messageText '" + message.getMessage() + "', state " + UnprocessedMessageState.RETURNED);
        }
    }

    public void updateUnprocessedMessageState(String uuid, UnprocessedMessageState state) {
        UnprocessedMessage storedMessages = repository.findUnprocessedMessageByUuid(uuid);
        storedMessages.setState(state);
        repository.save(storedMessages);
    }

    public List<UnprocessedMessage> getMessages() {
        List<UnprocessedMessage> messages = (List<UnprocessedMessage>) repository.findAll();
        return messages.stream()
                .filter(message -> message.getState().equals(UnprocessedMessageState.RETURNED))
                .collect(Collectors.toList());
    }

}
