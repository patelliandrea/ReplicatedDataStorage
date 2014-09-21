package it.polimi.inginf.distsys.replication;

import java.rmi.Remote;

public interface Replica extends Remote {
	public int read(int id);
	public void write(int id, int data);
}
