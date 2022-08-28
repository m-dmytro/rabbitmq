package com.hw.rabbitmq.domain;

import lombok.*;

import javax.persistence.*;

@Entity // Indicates that a class is a database model
@Table(name = "unprocessed_message") // sets the name of the table that the model will be mapped to
@Getter // Creates getters for all the variables in the class
@Setter
public class UnprocessedMessage {

    @Id
    @GeneratedValue
    private long id;

    private String uuid;

    private String source;

    @Column(name = "routing_key")
    private String routingKey;

    private String message;

    private UnprocessedMessageState state;

    public UnprocessedMessage() {
    }

    public UnprocessedMessage(String uuid, String source, String routingKey, String message, UnprocessedMessageState state) {
        this.uuid = uuid;
        this.source = source;
        this.routingKey = routingKey;
        this.message = message;
        this.state = state;
    }

}
