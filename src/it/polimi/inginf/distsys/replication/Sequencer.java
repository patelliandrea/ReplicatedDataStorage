package it.polimi.inginf.distsys.replication;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sequencer {
    private int multicastPort;
    private int replicaPort;
    private String group;
    private MulticastSocket multicastSocket;
    private DatagramSocket replicaSocket;
    private int last;
    private Map<Integer, UUID> orderMap;
    private Map<UUID, Message> history;
    private ScheduledExecutorService keepAlive;

    public Sequencer() {
        this.history = new HashMap<UUID, Message>();
        this.orderMap = new HashMap<Integer, UUID>();
        this.last = 0;
        this.multicastPort = 5000;
        this.group = "225.4.5.6";
        this.replicaPort = 5003;
    }

    public void start() throws IOException {
        this.multicastSocket = new MulticastSocket(multicastPort);
        multicastSocket.joinGroup(InetAddress.getByName(group));

        this.replicaSocket = new DatagramSocket(replicaPort);
        Runnable keepAliveTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Message keepAliveMessage = new Message(MessageType.KEEP_ALIVE);
                    keepAliveMessage.setOrder(last);
                    System.out.println("[SEQUENCER] sending keep_alive " + Integer.toString(keepAliveMessage.getOrder()));
                    byte[] byteMessage = messageToByte(keepAliveMessage);
                    DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, InetAddress.getByName(group), multicastPort);
                    multicastSocket.send(packet);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        keepAlive = Executors.newSingleThreadScheduledExecutor();
        keepAlive.scheduleWithFixedDelay(keepAliveTask, 2, 2, TimeUnit.SECONDS);
        this.listen();
    }
    private void listen() {
        Runnable listenTask = new Runnable() {
            public void run() {
                byte[] buffer = new byte[65535];
                ByteArrayInputStream byteInput = new ByteArrayInputStream(buffer);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(true) {
                    try {
                        replicaSocket.receive(packet);
                        ObjectInputStream objectInput = new ObjectInputStream(byteInput);
                        Message message = (Message) objectInput.readObject();
                        System.out.println("[SEQUENCER] received message UUID " + message.getMessageId().toString());

                        if(message.getType() == MessageType.WRITE) {
                            handleWriteMessage(message);
                        } else if(message.getType() == MessageType.NACK) {
                            handleNackMessage(message);
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

    private void handleWriteMessage(Message message) throws IOException {
        boolean alreadySent = true;
        Message orderMessage = history.get(message.getMessageId());
        if (orderMessage == null) {
            orderMessage = new Message(MessageType.ORDER, last, message.getMessageId());
            alreadySent = false;
        }
        orderMessage.setId(message.getId());
        orderMessage.setValue(message.getValue());
        history.put(message.getMessageId(), orderMessage);
        orderMap.put(orderMessage.getOrder(), message.getMessageId());
        byte[] byteMessage = messageToByte(orderMessage);
//        if (last < 10 || alreadySent) {
            DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, InetAddress.getByName(group), multicastPort);
            System.out.println("[SEQUENCER] sending order: " + Integer.toString(orderMessage.getOrder()) + " for message UUID: " + orderMessage.getMessageId().toString());
            multicastSocket.send(packet);
//        }
        if(!alreadySent) {
            last++;
        }
    }

    private void handleNackMessage(Message message) throws IOException {
        int lastReceived = message.getOrder();
        for(; lastReceived < last; lastReceived++) {
            UUID uuid = orderMap.get(lastReceived);
            Message response = history.get(uuid);
            handleWriteMessage(response);
        }
    }

    private byte[] messageToByte(Message message) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);

        objectOutput.writeObject(message);
        return byteOutput.toByteArray();
    }
}
