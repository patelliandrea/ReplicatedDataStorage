package it.polimi.inginf.distsys.replication;

/**
 * Created by patelliandrea on 21/09/14.
 */
public class MainClass {
    public static void main(String[] args) {
        try {
            System.out.println("running");
            Sequencer s = new Sequencer();
            ReplicaImpl r = new ReplicaImpl("1");
            ReplicaImpl r2 = new ReplicaImpl("2");
            ReplicaImpl r3 = new ReplicaImpl("3");
            ReplicaImpl r4 = new ReplicaImpl("4");

            r.write(2, 15);
            r.write(7, 13);
            r2.write(17, 1);
            r3.write(9, 9);
            r2.write(9, 8);
            r.write(2, 7);
            r2.write(12, 1);
            r3.write(35, 83);

            for(int i = 0; i < 1000000; i++);
            System.out.println("REPLICA 1");
            System.out.println(r.read(2));
            System.out.println(r.read(7));
            System.out.println(r.read(17));
            System.out.println(r.read(9));
            System.out.println(r.read(12));
            System.out.println(r.read(35));

            System.out.println("REPLICA 2");
            System.out.println(r2.read(2));
            System.out.println(r2.read(7));
            System.out.println(r2.read(17));
            System.out.println(r2.read(9));
            System.out.println(r2.read(12));
            System.out.println(r2.read(35));

            System.out.println("REPLICA 3");
            System.out.println(r3.read(2));
            System.out.println(r3.read(7));
            System.out.println(r3.read(17));
            System.out.println(r3.read(9));
            System.out.println(r3.read(12));
            System.out.println(r3.read(35));

            System.out.println("REPLICA 4");
            System.out.println(r4.read(2));
            System.out.println(r4.read(7));
            System.out.println(r4.read(17));
            System.out.println(r4.read(9));
            System.out.println(r4.read(12));
            System.out.println(r4.read(35));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
