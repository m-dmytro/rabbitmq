package com.hw.rabbitmq.repository;

import com.hw.rabbitmq.domain.UnprocessedMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnprocessedMessageRepository extends CrudRepository<UnprocessedMessage, String> {

    UnprocessedMessage findUnprocessedMessageByUuid(String uuid);

}
