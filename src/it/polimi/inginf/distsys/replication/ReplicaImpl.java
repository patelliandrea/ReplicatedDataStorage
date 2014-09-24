package it.polimi.inginf.distsys.replication;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicaImpl /*extends UnicastRemoteObject implements Replica*/ {
	private static final long serialVersionUID = -1196569555356780831L;
    private String name;
    private int multicastPort;
    private int sequencerPort;
    private String group;
    private String sequencerAddress;
    private MulticastSocket multicastSocket;
    private DatagramSocket sequencerSocket;
    private Map<Integer, Integer> storage;
    private Timer timeout;
    int last;
    boolean first;
    Message lastMessage;

	protected ReplicaImpl(String name) throws /*RemoteException,*/ IOException {
		super();
        this.name = name;
        this.first = true;
        this.last = 0;
        this.lastMessage = null;
        this.storage = new ConcurrentHashMap<Integer, Integer>();

        this.multicastPort = 5000;
        this.group = "225.4.5.6";
        this.multicastSocket = new MulticastSocket(multicastPort);
        multicastSocket.joinGroup(InetAddress.getByName(group));

        this.sequencerPort = 5003;
        this.sequencerAddress = "127.0.0.1";
        this.sequencerSocket = new DatagramSocket();

        this.listen();
	}

	//@Override
	public int read(int id) {
		return storage.get(id) == null ? -1 : storage.get(id);
	}

	//@Override
	public void write(int id, int data) {
        if(lastMessage == null || first) {
            System.out.println("write");
            this.first = false;
            Message message = new Message(MessageType.WRITE, id, data);
            lastMessage = message;
            try {
                this.TOMulticast(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    private void TOMulticast(Message message) throws IOException {
        boolean isNack = message.getType() == MessageType.NACK;
        byte[] byteMessage = messageToByte(message);
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, InetAddress.getByName(sequencerAddress), this.sequencerPort);
        sequencerSocket.send(packet);
        if(!isNack) {
            System.out.println("[REPLICA " + name + "] Sending message UUID: " + message.getMessageId().toString());
            this.timeout = new Timer();
//            timeout.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    try {
//                        System.out.println("[REPLICA " + name + "] Timed out for message UUID: " + lastMessage.getMessageId());
//                        TOMulticast(lastMessage);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 1000, 1000);
        } else {
            System.err.println("[REPLICA " + name + "] Sending NACK, last order: " + Integer.toString(this.last));
        }
    }

    private void listen() {
        Runnable listenTask = new Runnable() {
            public void run() {
                byte[] buffer = new byte[65535];
                ByteArrayInputStream byteInput = new ByteArrayInputStream(buffer);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(true) {
                    try {
                        multicastSocket.receive(packet);
                        ObjectInputStream objectInput = new ObjectInputStream(byteInput);
                        Message message = (Message) objectInput.readObject();

                        if(message.getType() == MessageType.ORDER) {
                            try {
                                handleOrderMessage(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        packet.setLength(buffer.length);
                        byteInput.reset();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread listenThread = new Thread(listenTask);
        listenThread.start();
    }

    private byte[] messageToByte(Message message) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);

        objectOutput.writeObject(message);
        return byteOutput.toByteArray();
    }

    private void handleOrderMessage(Message message) throws IOException {
        int order = message.getOrder();
        if(order < last)
            return;
        if(order > last) {
            sendNACK();
            return;
        } else {
            System.out.println("[REPLICA " + name + "] received order: " + Integer.toString(message.getOrder()) + " for message UUID: " + message.getMessageId().toString());
            this.save(message);
        }
    }

    public void save(Message message) {
        System.out.println("[REPLICA " + name + "] saving UUID: " + message.getMessageId().toString());
        storage.put(message.getId(), message.getValue());
        last = message.getOrder() + 1;
        if(lastMessage != null && message.getMessageId().equals(lastMessage.getMessageId())) {
            System.out.println("[REPLICA " + name + "] request completed UUID: " + message.getMessageId().toString());
            timeout.cancel();
            //timeout.purge();
            lastMessage = null;
        }
    }

    private void sendACK() {
        //TODO
    }

    private void sendNACK() throws IOException {
        Message message = new Message();
        message.setType(MessageType.NACK);
        message.setOrder(last);
        TOMulticast(message);
    }
}
