import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

class Participant{

    //int portSelected = Integer.parseInt(args[0]);                         //port to listen on
    //int portLogger = Integer.parseInt(args[1]);                         //logger port
    //int numberOfParticipants = Integer.parseInt(args[2]);         //number of participants
    //int timeoutValue = Integer.parseInt(args[3]);                 //time out value
    //voting options = the rest of the input arguments

    public static void main(String [] args) throws IOException {
        try {
            //int portNumber = Integer.parseInt(args[2]);         //port number
            //int portNumber = 1234;
            int portNumber = Integer.parseInt(args[0]);
            Random RNG = new Random();
            Socket socket = new Socket("Kri", 4323);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ArrayList<String> votingOptions = new ArrayList<>();
            String voteChosen;
            //System.out.println("THIS IS MY LOCAL PORT " + socket.getLocalPort());

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
            System.out.println("Testing");
            /*for(;;){ // stage 2

            }*/
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

class TCPReceiverThreadedClass{

    public static void main(String [] args){
        try{
            ServerSocket ss = new ServerSocket(4322);
            for(;;){
                try{Socket client = ss.accept();
                    new Thread(new ServiceThread(client)).start();
                }catch(Exception e){System.out.println("error "+e);}
            }
        }catch(Exception e){System.out.println("error "+e);}
    }

    static class ServiceThread implements Runnable{
        Socket client;
        ServiceThread(Socket c){client=c;}
        public void run(){try{
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            String line;
            while((line = in.readLine()) != null)
                System.out.println(line+" received");
            client.close(); }catch(Exception e){}
        }
    }

}
