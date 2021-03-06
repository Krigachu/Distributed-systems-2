import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

class Participant2 extends Thread{

    private int processID;
    private int portSelected;
    private String hostName;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int port;

    public Participant2(){

    }

    public Participant2(String hostName,int portSelected){
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
            //int portNumber = Integer.parseInt(args[2]);         //port number
            int portNumber = 1235;
            Random RNG = new Random();
            String hostName;
            Socket socket;
            PrintWriter out;
            BufferedReader in;
            socket = new Socket("Kri", 4323);
            //Socket socket = new Socket(hostName, portSelected);
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ArrayList<String> votingOptions = new ArrayList<>();
            String voteChosen;

            //sends join msg
            String line;
            //out.println("JOIN " + socket.getLocalPort());
            out.println("JOIN " + portNumber);
            out.flush();
            Thread.sleep(2000);

            //reads other participants
            line = in.readLine();
            System.out.println(line);

            //reads voting options
            line = in.readLine();
            System.out.println(line);

            //adds the options for voting to participant
            for (int a = 1; a < line.split(" ").length ; a++){
                votingOptions.add(line.split(" ")[a]);
            }

            //testing printing voting options
            /*for (String voteOptionsTesting : votingOptions){
                System.out.println(voteOptionsTesting);
            }*/

            //participant is now choosing their vote.
            voteChosen = votingOptions.get(RNG.nextInt(votingOptions.size()));
            System.out.println(voteChosen);

            for (int i = 0; i < 5; i++) { //while true loop here to ensure thread never shuts? // stage 1
                //out.println("TCP message " + i + " from sender "+ socket.getLocalPort());
                out.println("TCP message " + i + " from sender 2");
                out.flush();                                                                   //required to send messages to receiver, flush = push stream into socket
                System.out.println("TCP message " + i + " sent");
                //Thread.sleep(1000);
                line = in.readLine();
                System.out.println(line + " received");
                //Thread.sleep(100);
            }
            System.out.println("Testing");
            /*for(;;){ // stage 2


            }*/
            //line = in.readLine();
            //System.out.println(line);
            //stopConnection();

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
                out.println("TCP message " + i + " from sender 2");
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
//setSoTimeout