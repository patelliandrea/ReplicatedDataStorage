package it.polimi.inginf.distsys.replication;

public class Message {
	int value;
	MessageType type;
	
	public Message(MessageType type) {
		this.type = type;
	}
	
	public Message(MessageType type, int value) {
		this(type);
		this.value = value;
	}
	
	public Message() {}
	
	public int getValue() {
		return this.value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public MessageType getType() {
		return this.type;
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}
}
