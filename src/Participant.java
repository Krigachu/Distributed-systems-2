import java.io.*;
import java.net.*;
import java.util.*;

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
            HashMap<String,String> participantVotingInRounds = new HashMap<>();  //selfport number, with vote
            String voteChosen;
            int numOfParticipants;
            Boolean failure = true;
            //System.out.println("THIS IS MY LOCAL PORT " + socket.getLocalPort());

            //sends join msg
            String line;
            //out.println("JOIN " + socket.getLocalPort());
            out.println("JOIN " + portNumber);
            out.flush();
            //Thread.sleep(2000);

            //numOfParticipants
            line=in.readLine();
            System.out.println(line);
            numOfParticipants = Integer.parseInt(line.split(" ")[0]);


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

            //participant is now choosing their vote.
            voteChosen = votingOptions.get(RNG.nextInt(votingOptions.size()));
            System.out.println("I have chosen " +voteChosen);
            participantVotingInRounds.put(String.valueOf(portNumber),voteChosen);


            //---------------------------------PARTICIPANT COMMS---------------------------------------------------
            try{
                //setting up own socket
                ServerSocket ss = new ServerSocket(portNumber);
                for(int b = 0 ; b < otherParticipants.size(); b++) {
                    try {
                        //num of msgs to initiate next round?
                        //failed participants vote is still given
                        //setting up the sockets to other ports
                        if (!(portNumber == Integer.parseInt(otherParticipants.get(b)))){
                             participantSenders.add(new ParticipantSender(String.valueOf(portNumber),Integer.parseInt(otherParticipants.get(b)),voteChosen,numOfParticipants));
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
                                    //ss.setSoTimeout(1000);
                                    participantReceivers.add(new ParticipantReceiver(client,String.valueOf(portNumber),numOfParticipants));
                                }
                            }

                            //starting the listeners
                            for (ParticipantReceiver pR : participantReceivers){
                                pR.start();
                            }

                            //putting info into hashmap
                            Thread.sleep(2000);
                            for (ParticipantReceiver pR : participantReceivers) {
                                participantVotingInRounds.put(pR.getVotingParticipantPort(), pR.getOtherParticipantVote());
                            }

                            //passing the new updated hashmap
                            Thread.sleep(2000);
                            for (ParticipantSender pS : participantSenders) {
                                pS.setParticipantVotingInRounds(participantVotingInRounds);
                            }

                            //passing the new updated hashmap
                            Thread.sleep(2000);
                            for (ParticipantReceiver pR : participantReceivers) {
                                pR.setParticipantVotingInRounds(participantVotingInRounds);
                            }

                            //unlock first gate of the senders (2nd round of voting)
                            Thread.sleep(2000);
                            for (ParticipantSender pS : participantSenders) {
                                pS.setFirstCheck();
                            }

                            //unlock first gate of the receivers (2nd round of voting)
                            Thread.sleep(2000);
                            for (ParticipantReceiver pR : participantReceivers) {
                                pR.setFirstCheck();
                            }

                            //unlock second gate of the receivers (2nd round of voting)
                            Thread.sleep(2000);
                            for (ParticipantReceiver pR : participantReceivers) {
                                pR.setSecondCheck();
                            }

                            //unlock second gate of the receivers (2nd round of voting)
                            Thread.sleep(2000);
                            for (ParticipantSender pS : participantSenders) {
                                pS.setSecondCheck();
                            }


                            /*
                            //if there is an error
                            if(!(numOfParticipants == participantVotingInRounds.size())) {
                                int d = 0;
                                while (failure) {
                                    if (d != 0) {
                                        //locks while loop gate
                                        Thread.sleep(2000);
                                        for (ParticipantSender pS : participantSenders) {
                                            pS.setThirdCheck(false);
                                        }

                                        //locks while loop gate
                                        Thread.sleep(2000);
                                        for (ParticipantReceiver pR : participantReceivers) {
                                            pR.setThirdCheck(false);
                                        }
                                    }
                                    //updating hashmap
                                    Thread.sleep(2000);
                                    for (ParticipantReceiver pR : participantReceivers) {
                                        participantVotingInRounds = pR.getParticipantVotingInRounds();
                                    }

                                    //passing the new updated hashmap
                                    Thread.sleep(2000);
                                    for (ParticipantSender pS : participantSenders) {
                                        pS.setParticipantVotingInRounds(participantVotingInRounds);
                                    }

                                    if (d != 0) {
                                        //unlocks while loop gate
                                        Thread.sleep(2000);
                                        for (ParticipantSender pS : participantSenders) {
                                            pS.setThirdCheck(true);
                                        }

                                        //unlocks while loop gate
                                        Thread.sleep(2000);
                                        for (ParticipantReceiver pR : participantReceivers) {
                                            pR.setThirdCheck(true);
                                        }
                                        //breaks loop if all votes are accounted for
                                        if (numOfParticipants == participantVotingInRounds.size()) {
                                            failure = true;
                                        }

                                    }
                                    d++;

                                }
                            }
*/

                            Thread.sleep(3000);
                            for (ParticipantReceiver pR : participantReceivers){
                                System.out.println("WE OUTTIE");
                            }



                        }
                    } catch (Exception e) {

                    }
                }


                //System.out.println("Out of the for loop");
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
            //System.out.println("Testing");
            int f = 0;
            for(;;){ // stage 2
                if(participantVotingInRounds.size() == numOfParticipants){
                    for (String vote : votingOptions){
                        if (getVoteFrequency(vote,participantVotingInRounds) > a){

                        }
                    }
                }
            }
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

    public static int getVoteFrequency(String vote, HashMap<String, String> votingRounds){
        int a = 0;
        for (String port : votingRounds.keySet()){
            if(vote.equals(votingRounds.get(port))){
                a++;
            }
        }
        return a;
    }

    static class ParticipantReceiver extends Thread {
        Socket client;
        String selfPort;
        Random RNG = new Random();
        String otherParticipantVote;
        String votingParticipantPort;
        Boolean firstCheck = true;
        Boolean secondCheck = true;
        Boolean thirdCheck = true;
        Boolean failure = true;
        int numOfParticipants;
        HashMap<String,String> participantVotingInRounds = new HashMap<>();  //selfport number, with vote

        //PrintWriter out;
        //BufferedReader in;
        ParticipantReceiver(Socket c, String port,int numOfParticipants) {
            client = c;
            this.selfPort = port;
            this.numOfParticipants = numOfParticipants;
        }

        //participant to participant comms
        //use synchro stuff, idiot
        public void run() {
            try {
                System.out.println("IS THIS THREAD RUNNING? -> receiver");
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;


                //testing comms between participant
                line = in.readLine();
                System.out.println(line);

                //first round of voting
                line = in.readLine();
                System.out.println(line); // find out what others voted for
                otherParticipantVote = line.split(" ")[2];
                votingParticipantPort = line.split(" ")[1];
                setOtherParticipantVote(otherParticipantVote);
                setVotingParticipantPort(votingParticipantPort);



                //first lock
                System.out.println("Hit first lock - receiver");
                while (firstCheck) {
                    Thread.sleep(2000);
                    System.out.println("In first lock - receiver");
                }
                System.out.println("Exited first lock - receiver");

                //reading second round
                line = in.readLine();
                System.out.println(line);

                //second lock
                System.out.println("Hit second lock - receiver");
                while (secondCheck) {
                    Thread.sleep(2000);
                    System.out.println("In second lock - receiver");
                }
                System.out.println("Exited second lock - receiver");

                /*
                //recursing voting -> making sure all votes are accounted for
                    if (numOfParticipants != getParticipantVotingInRounds().size()) {
                        while ((line = in.readLine()) != null && failure) {
                            System.out.println(line);
                            if (numOfParticipants != getParticipantVotingInRounds().size()) {
                                for (int a = 1; a < line.split(" ").length; a++) {
                                    for (String port : getParticipantVotingInRounds().keySet()) {
                                        if (!(line.split(" ")[a].equals(port))) {
                                            getParticipantVotingInRounds().put(port, line.split(" ")[a + 1]);
                                        }
                                    }
                                }
                            } else {
                                failure = false;
                            }

                            //third lock
                            System.out.println("Hit third lock - receiver");
                            while (thirdCheck) {
                                Thread.sleep(2000);
                                System.out.println("In third lock - receiver");
                            }

                            System.out.println("Exited third lock - receiver");

                        }
                    }
                    */

                System.out.println("YAAS, IT WORKS - receiver");





                /*
                while((line = in.readLine()) != null)
                    System.out.println(line);
                */

                /*
                for (int a = 1; a < line.split(" ").length ; a++){
                votingOptions.add(line.split(" ")[a]);
            }
                 */



                client.close();
                System.out.println("CLOSED - receiver");
            } catch (Exception e) {

            }
        }
        public void setFirstCheck() {
            this.firstCheck = false;
        }

        public void setSecondCheck() {
            this.secondCheck = false;
        }

        public Boolean getSecondCheck(){
            return this.secondCheck;
        }

        public void setThirdCheck(Boolean var) {
            this.thirdCheck = var;
        }

        public void setOtherParticipantVote(String vote){
            this.otherParticipantVote = vote;
        }

        public String getOtherParticipantVote(){
            return this.otherParticipantVote;
        }

        public void setVotingParticipantPort(String port){
            this.votingParticipantPort = port;
        }

        public String getVotingParticipantPort(){
            return this.votingParticipantPort;
        }

        public String getSelfPort(){
            return this.selfPort;
        }

        public void setParticipantVotingInRounds(HashMap<String,String> votes){
            this.participantVotingInRounds = votes;
        }

        public HashMap<String,String> getParticipantVotingInRounds(){
            return this.participantVotingInRounds;
        }


    }



    static class ParticipantSender extends Thread{
            Socket client;
            String selfPort;
            int otherPort;
            Random RNG = new Random();
            Boolean zerothCheck = true;
            Boolean firstCheck = true;
            Boolean secondCheck = true;
            Boolean thirdCheck = true;
            Boolean failure = true;
            String voteChosen;
            int numOfParticipants;
            HashMap<String,String> participantVotingInRounds = new HashMap<>();  //selfport number, with vote

        //PrintWriter out;
            //BufferedReader in;

            ParticipantSender(String myPort, int otherPort, String voteChosen, int numOfParticipants){
                //client=c;
                setSelfPort(myPort);
                this.otherPort = otherPort;
                this.voteChosen = voteChosen;
                this.numOfParticipants = numOfParticipants;
            }

            //participant to participant comms
            //use synchro stuff, idiot
            public void run(){
                try {
                    Socket client = new Socket(InetAddress.getLocalHost(),otherPort);
                    //setClient(client);
                    //setZerothCheck();

                    System.out.println("IS THIS THREAD RUNNING? -> sender");
                    PrintWriter out = new PrintWriter(client.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String line;
                    String votePortMsg = "" ;


                    //testing comms
                    //Thread.sleep(RNG.nextInt(1500)+2000);
                    out.println("COMMUNICATING TO " + client.getPort() + " FROM " + selfPort);
                    //Thread.sleep(5000);
                    out.flush();

                    //first round
                    out.println("VOTE " +selfPort + " " +voteChosen);
                    out.flush();

                    //first lock
                    System.out.println("Hit first lock - sender");
                    while (firstCheck) {
                        Thread.sleep(2000);
                        System.out.println("In first lock - sender");
                    }
                    System.out.println("Exited first lock - sender");

                    //second round
                    for (String portWithVote : getParticipantVotingInRounds().keySet()) {
                        if (!(portWithVote.equals(selfPort))) {
                            votePortMsg = votePortMsg + " " + portWithVote + " " + getParticipantVotingInRounds().get(portWithVote);
                        }
                    }

                    //sending the vote round 2
                    out.println("VOTE" + votePortMsg);
                    out.flush();

                    //second lock
                    System.out.println("Hit second lock - sender");
                    while (secondCheck) {
                        Thread.sleep(2000);
                        System.out.println("In second lock - sender");
                    }
                    System.out.println("Exited second lock - sender");
                    votePortMsg = "";

                    /*
                    //future rounds
                        int e = 0;
                        while (failure) {
                            if (numOfParticipants != getParticipantVotingInRounds().size() || e == 0) {
                                for (String portWithVote : getParticipantVotingInRounds().keySet()) {
                                    if (!(portWithVote.equals(selfPort))) {
                                        votePortMsg = votePortMsg + " " + portWithVote + " " + getParticipantVotingInRounds().get(portWithVote);
                                    }
                                }

                                //sending the vote round 2
                                out.println("VOTE" + votePortMsg);
                                out.flush();
                            } else {
                                failure = false;
                                out.println("VOTE" + votePortMsg); //hmmm
                                out.flush();
                            }
                            e++;

                            //third lock
                            System.out.println("Hit third lock - sender");
                            while (thirdCheck) {
                                Thread.sleep(2000);
                                System.out.println("In third lock - sender");
                            }
                            System.out.println("Exited third lock - sender");

                        }*/

                    System.out.println("YAAS, IT WORKS - sender");
                    //now here comes rounds past 1

                    // -> got to make this recursive so that when no changes occur, it ends.
                    //while (something) {
                    // interpret (line) -> make sure nothing is wrong
                    // if there are differences -> record this in hashmap
                    // recheck with another round
                    // if all is cool, then stop looping.


                    //while((line = in.readLine()) != null)


                    client.close();
                    System.out.println("CLOSED - SENDER");
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

            public void setParticipantVotingInRounds(HashMap<String,String> votes){
                this.participantVotingInRounds = votes;
            }

            public HashMap<String,String> getParticipantVotingInRounds(){
                return this.participantVotingInRounds;
            }

            public void setSelfPort(String selfPort){
                this.selfPort = selfPort;
            }

            public String getSelfPort(){
                return this.selfPort;
            }

            public void setFirstCheck() {
                this.firstCheck = false;
            }

            public void setSecondCheck() {
                this.secondCheck = false;
            }

            public Boolean getSecondCheck() {
                return this.secondCheck;
            }
            public void setThirdCheck(Boolean var) {
            this.thirdCheck = var;
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
//exchanging of messages until things no longer change.
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
