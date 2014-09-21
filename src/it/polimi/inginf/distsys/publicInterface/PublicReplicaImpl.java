package it.polimi.inginf.distsys.publicInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class PublicReplicaImpl extends UnicastRemoteObject implements PublicReplica {
	private static final long serialVersionUID = -1196569555356780831L;

	protected PublicReplicaImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public int read(long id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(long id, int data) {
		// TODO Auto-generated method stub
	}

}
