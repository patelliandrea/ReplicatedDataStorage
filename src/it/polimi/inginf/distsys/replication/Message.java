package it.polimi.inginf.distsys.replication;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
	int value;
    int id;
    UUID messageId;
	MessageType type;
	int order;

	public Message(MessageType type) {
        this();
		this.type = type;
	}
	
	public Message(MessageType type, int id, int value) {
		this(type);
		this.value = value;
        this.id = id;
	}

    public Message(MessageType type, int order, UUID messageId) {
        this.type = type;
        this.order = order;
        this.messageId = messageId;
    }
	
	public Message() {
        this.messageId = UUID.randomUUID();
    }
	
	public int getValue() {
		return this.value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
	
	public MessageType getType() {
		return this.type;
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public UUID getMessageId() {
        return this.messageId;
    }

    public String toString() {
        return String.format("id: %d, value: %d", this.id, this.value);
    }
}
