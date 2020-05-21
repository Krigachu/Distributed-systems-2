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
            int portNumber = Integer.parseInt(args[0]);
            Random RNG = new Random();
            Socket socket = new Socket(InetAddress.getLocalHost(), 4323);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ArrayList<ParticipantReceiver> participantReceivers = new ArrayList<>();
            ArrayList<ParticipantSender> participantSenders = new ArrayList<>();
            ArrayList<String> otherParticipants = new ArrayList<>();
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

            //adds other participant ports
            for (int a = 1; a < line.split(" ").length ; a++){
                otherParticipants.add(line.split(" ")[a]);
            }

            //testing printing other participants
            /*for (String voteOptionsTesting : otherParticipants){
                System.out.println(voteOptionsTesting);
            }*/

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


            //---------------------------------PARTICIPANT COMMS---------------------------------------------------
            try{
                //setting up own socket
                ServerSocket ss = new ServerSocket(portNumber);
                for(int b = 0 ; b < otherParticipants.size(); b++) {
                    try {

                        //setting up the sockets to other ports
                        if (!(portNumber == Integer.parseInt(otherParticipants.get(b)))){
                             participantSenders.add(new ParticipantSender(String.valueOf(portNumber),Integer.parseInt(otherParticipants.get(b))));
                        }

                        if (b == otherParticipants.size()-1) {
                            //starting the senders
                            for(ParticipantSender pS : participantSenders){
                                pS.start();
                            }

                            //getting the listeners ready
                            for (int c = 0; c < otherParticipants.size(); c++) {
                                if (!(portNumber == Integer.parseInt(otherParticipants.get(b)))) {
                                    Socket client = ss.accept();
                                    participantReceivers.add(new ParticipantReceiver(client,String.valueOf(portNumber)));
                                }
                            }

                            //starting the listeners
                            for (ParticipantReceiver pR : participantReceivers){
                                pR.start();
                            }

                        }
                    } catch (Exception e) {

                    }
                }

                /*
                for(int b = 0 ; b < otherParticipants.size(); b++){
                    try{
                        if (portNumber < Integer.parseInt(otherParticipants.get(b)) ){
                            Socket socketForOthers = new Socket(InetAddress.getLocalHost(),Integer.parseInt(otherParticipants.get(b)));
                        }

                        //System.out.println("WORKING");
                        //particpantThreadArray.add(new ParticipantCommunication(ss.accept(),String.valueOf(portNumber)));

                        if (b == otherParticipants.size()-1){
                            for (int c = 0 ; c < otherParticipants.size(); c++){
                                if(portNumber > Integer.parseInt(otherParticipants.get(c))) {
                                    participantReceivers.add(new ParticipantReceiver(ss.accept(), String.valueOf(portNumber)));
                                }
                            }
                            System.out.println("YOU GOT IN THE LOOP");

                            //this is where the sheet begins
                            for (ParticipantCommunication pT : participantThreadArray){
                                pT.start();
                            }



                    }catch(Exception e){System.out.println("error "+e);}
                }*/


                System.out.println("Out of the for loop");
            }catch(Exception e){System.out.println("error "+e);}

            //------------------------------------------------------------------------------------

            /*for (int i = 0; i < 5; i++) { // stage 1
                //out.println("TCP message " + i + " from sender " + socket.getLocalPort());
                out.println("TCP message " + i + " from sender 1");
                out.flush();                                               //required to send messages to receiver, flush = push stream into socket
                System.out.println("TCP message " + i + " sent");
                //Thread.sleep(1000);
                line = in.readLine();
                System.out.println(line + " received");
                //Thread.sleep(100);
            }*/
            System.out.println("Testing");
            /*for(;;){ // stage 2

            }*/
        }catch(Exception e){
            System.out.println("error"+e);
        }
    }

    public static int getLargestPortNumber(ArrayList<String> otherParticipants){
        int a = 0;
        for(String temp : otherParticipants){
            if (a < Integer.parseInt(temp)){
                a = Integer.parseInt(temp);
            }
        }
        return a;
    }

    public static int getSmallestPortNumber(ArrayList<String> otherParticipants){
        int a = Integer.MAX_VALUE;
        for(String temp : otherParticipants){
            if (a > Integer.parseInt(temp)){
                a = Integer.parseInt(temp);
            }
        }
        return a;
    }

    static class ParticipantReceiver extends Thread {
        Socket client;
        String selfPort;
        Random RNG = new Random();

        //PrintWriter out;
        //BufferedReader in;
        ParticipantReceiver(Socket c, String port) {
            client = c;
            this.selfPort = port;
        }

        //participant to participant comms
        //use synchro stuff, idiot
        public void run() {
            try {
                System.out.println("IS THIS THREAD RUNNING? -> receiver");
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;

                Thread.sleep(12000);

                while((line = in.readLine()) != null)
                    System.out.println(line+ " received");

                /*
                System.out.println("IS THIS THREAD RUNNING? -> receiver");
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;

                System.out.println("HELLO 1");

                //testing comms
                Thread.sleep(RNG.nextInt(1500) + 2000);
                out.println("COMMUNICATING TO " + client.getPort() + " FROM " + selfPort);
                Thread.sleep(5000);
                out.flush();
                System.out.println("HELLO 1.5");



                //line = in.readLine();
                if (!in.ready()) {
                    System.out.println("ITS EMPTY");
                } else {
                    System.out.println("ITS NOT EMPTY");
                }

                System.out.println("HELLO 2.5");
                System.out.println(in.readLine());
                System.out.println("HELLO 3");


                //while((line = in.readLine()) != null)
                //System.out.println(line+" received");
                */
                client.close();
            } catch (Exception e) {

            }
        }

    }

    static class ParticipantSender extends Thread{
            Socket client;
            String selfPort;
            int otherPort;
            Random RNG = new Random();
            Boolean zerothCheck = true;
            Boolean firstCheck = true;
            //PrintWriter out;
            //BufferedReader in;

            ParticipantSender(String myPort, int otherPort){
                //client=c;
                this.selfPort = myPort;
                this.otherPort = otherPort;
            }

            //participant to participant comms
            //use synchro stuff, idiot
            public void run(){
                try {
                    Socket client = new Socket(InetAddress.getLocalHost(),otherPort);
                    setClient(client);
                    setZerothCheck();

                    System.out.println("IS THIS THREAD RUNNING? -> sender");
                    PrintWriter out = new PrintWriter(client.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String line;

                    System.out.println("HELLO 1");

                    //testing comms
                    Thread.sleep(RNG.nextInt(1500)+2000);
                    out.println("COMMUNICATING TO " + client.getPort() + " FROM " + selfPort);
                    Thread.sleep(5000);
                    out.flush();
                    System.out.println("HELLO 1.5");


                    /*
                    //line = in.readLine();
                    if (!in.ready()) {
                        System.out.println("ITS EMPTY");
                    } else {
                        System.out.println("ITS NOT EMPTY");
                    }
                    System.out.println("HELLO 2.5");
                    System.out.println(in.readLine());
                    System.out.println("HELLO 3");
                    */


                    //while((line = in.readLine()) != null)
                    //System.out.println(line+" received");

                    client.close();
                }catch(Exception e){

                }
            }

            public void setClient(Socket client){
                this.client = client;
            }

            public Socket getClient(){
                return this.client;
            }

             public void setZerothCheck() {
                this.zerothCheck = false;
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
/*
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

}*/
