package it.polimi.inginf.distsys.replication;

import java.util.LinkedList;
import java.util.Queue;

public class Replica {
	private int value;
	private Queue<Message> messageQueue;
	
	public Replica() {
		this.messageQueue = new LinkedList<Message>();
	}
	
	
	public synchronized void setValue(int value) {
		this.value = value;
	}
	
	public synchronized int getValue() {
		return this.value;
	}
}
