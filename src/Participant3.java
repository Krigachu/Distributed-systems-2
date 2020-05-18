import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.net.*;


class Participant3 extends Thread{
    private int processID;
    private int portSelected;
    private String hostName;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Participant3(String hostName,int portSelected){
        this.portSelected = portSelected;
        this.hostName = hostName;
    }

    public void run() {
        try {

            socket = new Socket("Kri", 4323);
            //Socket socket = new Socket(hostName, portSelected);
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //String line;
            processID = socket.getLocalPort();
            System.out.println(processID);
            out.println("JOIN " + socket.getLocalPort());
            out.flush();

            String line;
            for (int i = 0; i < 5; i++) { //while true loop here to ensure thread never shuts?
                out.println("TCP message " + i + " from sender "+ socket.getLocalPort());
                out.flush();                                               //required to send messages to receiver, flush = push stream into socket
                System.out.println("TCP message " + i + " sent");
                //Thread.sleep(1000);
                line = in.readLine();
                System.out.println(line + " received");
                //Thread.sleep(100);
            }
            stopConnection();
        }catch(Exception e){
            System.out.println("error"+e);
        }
    }
    public void startConnection(String host, int portSelected) throws IOException {
        socket = new Socket("Kri", 4323);
        //Socket socket = new Socket(hostName, portSelected);
        out = new PrintWriter(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        processID = socket.getLocalPort();
        out.println("JOIN " + socket.getLocalPort());
        out.flush();
        run();
    }


    public void stopConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }


    public static void main(String[] args) {
        try {
            int processID;
            int portSelected;
            String hostName;
            Socket socket;
            PrintWriter out;
            BufferedReader in;
            socket = new Socket("Kri", 4323);
            //Socket socket = new Socket(hostName, portSelected);
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            out.println("JOIN " + socket.getLocalPort());
            Thread.sleep(2000);

            processID = socket.getLocalPort();
            //System.out.println(processID);
            out.flush();
            //line = in.readLine();
            for (int i = 0; i < 5; i++) { //while true loop here to ensure thread never shuts?
                //out.println("TCP message " + i + " from sender "+ socket.getLocalPort());
                out.println("TCP message " + i + " from sender 3");
                out.flush();                                                                   //required to send messages to receiver, flush = push stream into socket
                System.out.println("TCP message " + i + " sent");
                //Thread.sleep(1000);
                line = in.readLine();
                System.out.println(line + " received");
                //Thread.sleep(100);
            }
            //line = in.readLine();
            //System.out.println(line);


        }catch(Exception e){
            System.out.println("error"+e);
        }
    }

    /*
    public static void main(String[] args) {
        try {
            int processID;
            int portSelected;
            String hostName;
            Socket socket;
            PrintWriter out;
            BufferedReader in;
            socket = new Socket("Kri", 4323);
            //Socket socket = new Socket(hostName, portSelected);
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            out.println("JOIN " + socket.getLocalPort());

            processID = socket.getLocalPort();
            //System.out.println(processID);
            out.flush();
            for (int i = 0; i < 5; i++) { //while true loop here to ensure thread never shuts?
                //out.println("TCP message " + i + " from sender "+ socket.getLocalPort());
                out.println("TCP message " + i + " from sender 3");
                out.flush();                                                                   //required to send messages to receiver, flush = push stream into socket
                System.out.println("TCP message " + i + " sent");
                //Thread.sleep(1000);
                line = in.readLine();
                System.out.println(line + " received");
                //Thread.sleep(100);
            }
            line = in.readLine();
            System.out.println(line);


        }catch(Exception e){
            System.out.println("error"+e);
        }
    }
     */


}
//consensus can only be achieved on synchronous systems
//setSoTimeout {
