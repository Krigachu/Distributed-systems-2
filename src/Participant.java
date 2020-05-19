import java.io.*;
import java.net.*;

class Participant{

    public static void main(String [] args) throws IOException {
        try {
            Socket socket = new Socket("Kri", 4323);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           // System.out.println("THIS IS MY LOCAL PORT " + socket.getLocalPort());
            String line;
            out.println("JOIN " + socket.getLocalPort());
            out.flush();
            Thread.sleep(2000);
            //line = in.readLine();
            //System.out.println(line);
            for (int i = 0; i < 5; i++) { // stage 1
                //out.println("TCP message " + i + " from sender " + socket.getLocalPort());
                out.println("TCP message " + i + " from sender 1");
                out.flush();                                               //required to send messages to receiver, flush = push stream into socket
                System.out.println("TCP message " + i + " sent");
                //Thread.sleep(1000);
                line = in.readLine();
                System.out.println(line + " received");
                //Thread.sleep(100);
            }
            for(;;){ // stage 2

            }
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

