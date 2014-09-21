package it.polimi.inginf.distsys.publicInterface;

import java.rmi.Remote;

public interface PublicReplica extends Remote {
	public int read(long id);
	public void write(long id, int data);
}
