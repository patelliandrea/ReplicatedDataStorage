package it.polimi.inginf.distsys.replication;

import java.io.IOException;
import java.util.*;

/**
 * Created by patelliandrea on 21/09/14.
 */
public class MainClass {

    public static void main(String[] args) {
        if(args.length == 0) {
            startLocal();
        } else if(args.length == 2) {
            startCluster(args[0], Boolean.parseBoolean(args[1]));
        }
    }

    static private void startCluster(final String replicaName, boolean isSequencer) {
        if(isSequencer) {
            Sequencer s = new Sequencer();
            try {
                s.start();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        final Replica replica = new ReplicaImpl(replicaName);
        try {
            replica.start();
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    Random r = new Random();
                    int operation = r.nextInt(4);
                    if (operation == 0) {
                        replica.write(r.nextInt(5), r.nextInt(1000));
                    } else {
                        int id = r.nextInt(5);
                        int value = replica.read(id);
                        System.err.println("[MAIN] id: " + Integer.toString(id) + " value: " + Integer.toString(value) + " from replica " + replicaName);
                    }
                }
            }, 1000, 1000);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    static private void startLocal() {
        try {
            final List<ReplicaImpl> replicaList = new ArrayList<ReplicaImpl>();

            Sequencer s = new Sequencer();
            s.start();
            for(int i = 0; i < 10; i++) {
                replicaList.add(new ReplicaImpl(Integer.toString(i)));
                replicaList.get(i).start();
            }

            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    Random r = new Random();
                    int operation = r.nextInt(4);
                    if(operation == 0) {
                        int replicaIndex = r.nextInt(replicaList.size());
                        ReplicaImpl currentReplica = replicaList.get(replicaIndex);
                        currentReplica.write(r.nextInt(5), r.nextInt(1000));
                    } else {
                        int id = r.nextInt(5);
                        for(int i = 0; i < replicaList.size(); i++) {
                            int value = replicaList.get(i).read(id);
                            System.err.println("[MAIN] id: " + Integer.toString(id) + " value: " + Integer.toString(value) + " from replica " + Integer.toString(i));
                        }
                    }
                }
            }, 1000, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
