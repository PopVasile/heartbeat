/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heartbeat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import static sun.io.Win32ErrorMode.initialize;


public class HeartBeat implements Runnable {

    static int self_id = 1;
    static int server_Port = 5531;
    String operation; // tip fir RECEIVER, HEARTBEAT
    static HashMap<Integer, String> processes = new HashMap<Integer, String>();
    static boolean received = false;
    static String my_ip;

    public HeartBeat(String operation) {
        this.operation = operation;
    }

    /* The main() method starts two threads, RECEIVER and HEARTBEAT */
    public static void main(String args[])
            throws UnknownHostException, IOException, InterruptedException {
        Thread.sleep(100);
        initialize();
       processes.put(1, "192.168.0.101");
        processes.put(2, "192.168.0.101");
        processes.put(3, "192.168.0.101");
        Runnable receiver = new HeartBeat("receiver");
        new Thread(receiver).start();
        Runnable heartbeat = new HeartBeat("heartbeat");
        new Thread(heartbeat).start();
        // bucla principala
    }

    @Override
    public void run() {
        if (operation.equals("receiver")) {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(server_Port);
                while (true) {
                    Socket socket = serverSocket.accept();
                    // System.out.println("Connection established.....");
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String option = in.readUTF();
                    if (option.equals("heartbeat")) {
                        int sender = Integer.parseInt(in.readUTF());
                        System.out.println("HEARTBEAT received from " + processes.get(sender));
                    }
                    socket.close();
                } // while
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operation.equals("heartbeat")) {
            while (true) {
                for (int key : processes.keySet()) {
                    if (key != self_id) {
                        String destination_server = processes.get(key);
                        try {
                            Thread.sleep(1250);
                            System.out.println("try to check " + destination_server);
                            Socket socket = new Socket(destination_server, 5532);
                            DataOutputStream out
                                    = new DataOutputStream(socket.getOutputStream());
                            out.writeUTF("heartbeat");
                            out.writeUTF(self_id + "");
                            System.out.println("Sent HEARTBEAT to: " + destination_server);
                        } catch (Exception e) {
                            System.out.println("\n***\tpeer has FAILED!\t***");
                        }
                    }
                }
            }
        }

    }
}
