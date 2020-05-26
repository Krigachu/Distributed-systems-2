import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

class Participant{

    //int portSelected = Integer.parseInt(args[0]);                         //port to listen on
    //int portLogger = Integer.parseInt(args[1]);                         //logger port
    //int numberOfParticipants = Integer.parseInt(args[2]);         //number of participants
    //int timeoutValue = Integer.parseInt(args[3]);                 //time out value
    //voting options = the rest of the input arguments

    public static void main(String [] args) throws IOException {
        try {
            int coordPortNumber = Integer.parseInt(args[0]);                         //port to listen on
            int portLogger = Integer.parseInt(args[1]);                         //logger port
            int participantPortNumber = Integer.parseInt(args[2]);         //number of participants
            int timeoutValue = Integer.parseInt(args[3]);                 //time out value

            ParticipantLogger.initLogger(portLogger, participantPortNumber, timeoutValue);
            ParticipantLogger pLogger = ParticipantLogger.getLogger();

            Participant participant = new Participant();
            participant.startParticipant(coordPortNumber,portLogger,participantPortNumber,timeoutValue,pLogger);
        } catch (Exception e) {

        }
    }

    public void startParticipant(int coordPort, int lPort, int participantPort, int timeout, ParticipantLogger pLog) throws Exception{
        int coordPortNumber = coordPort;                        //port to listen on
        int portLogger = lPort;                         //logger port
        int participantPortNumber = participantPort;         //number of participants
        int timeoutValue = timeout;                 //time out value
        ParticipantLogger pLogger = pLog;
        try{
            Random RNG = new Random();
            Socket socket = new Socket(InetAddress.getLocalHost(), coordPortNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ArrayList<ParticipantReceiver> participantReceivers = new ArrayList<>();
            ArrayList<ParticipantSender> participantSenders = new ArrayList<>();
            ArrayList<String> otherParticipants = new ArrayList<>();
            ArrayList<String> votingOptions = new ArrayList<>();
            HashMap<String,String> participantVotingInRounds = new HashMap<>();  //selfport number, with vote
            HashMap<String,Integer> voteTally = new HashMap<>();
            String voteChosen;
            int numOfParticipants;
            Boolean failure = true;
            Object lock = new Object();
            //System.out.println("THIS IS MY LOCAL PORT " + socket.getLocalPort());

            //sends join msg
            String line;
            //out.println("JOIN " + socket.getLocalPort());
            out.println("JOIN " + participantPortNumber);
            out.flush();
            //Logging join msg
            pLogger.messageSent(coordPortNumber,"JOIN " + participantPortNumber);
            pLogger.joinSent(coordPortNumber);


            //numOfParticipants
            line=in.readLine();
            System.out.println(line);
            numOfParticipants = Integer.parseInt(line.split(" ")[0]);


            //reads other participants (details msg)
            line = in.readLine();
            System.out.println(line);

            //adds other participant ports
            for (int a = 1; a < line.split(" ").length ; a++){
                otherParticipants.add(line.split(" ")[a]);
            }

            //converting strings to integers for log message
            ArrayList<Integer> logParticipantIDs = new ArrayList<>();
            for (String otherPorts : otherParticipants){
                logParticipantIDs.add(Integer.parseInt(otherPorts));
            }
            //Logging details received msg
            pLogger.messageReceived(coordPortNumber,line);
            pLogger.detailsReceived(logParticipantIDs);

            //reads voting options
            line = in.readLine();
            System.out.println(line);

            //adds the options for voting to participant
            for (int a = 1; a < line.split(" ").length ; a++){
                votingOptions.add(line.split(" ")[a]);
            }
            //Logging voting options recieved msg
            pLogger.messageReceived(coordPortNumber,line);
            pLogger.voteOptionsReceived(votingOptions);

            //participant is now choosing their vote.
            voteChosen = votingOptions.get(RNG.nextInt(votingOptions.size()));
            System.out.println("I have chosen " + voteChosen);
            participantVotingInRounds.put(String.valueOf(participantPortNumber),voteChosen);

            //add voting options to hashmap which is used for consensus
            for (String vote : votingOptions){
                voteTally.put(vote,0);
            }


            //---------------------------------PARTICIPANT COMMS---------------------------------------------------
            try{
                //setting up own server socket for participant to participant comms
                ServerSocket ss = new ServerSocket(participantPortNumber);

                //Logging startedListening msg
                pLogger.startedListening();

                for(int b = 0 ; b < otherParticipants.size(); b++) {
                    try {
                        //num of msgs to initiate next round?
                        //setting up the sockets to other ports
                        if (!(participantPortNumber == Integer.parseInt(otherParticipants.get(b)))){
                            participantSenders.add(new ParticipantSender(String.valueOf(participantPortNumber),Integer.parseInt(otherParticipants.get(b)),voteChosen,numOfParticipants,pLogger));
                        }

                        if (b == otherParticipants.size()-1) {
                            //starting the senders

                            //Logging start of round 1
                            pLogger.beginRound(1);
                            for(ParticipantSender pS : participantSenders){
                                pS.start();
                            }

                            //getting the listeners ready
                            for (int c = 0; c < otherParticipants.size(); c++) {
                                try {
                                    if (!(participantPortNumber == Integer.parseInt(otherParticipants.get(c)))) { //was b instead of c
                                        ss.setSoTimeout(timeoutValue);
                                        Socket client = ss.accept();

                                        //Logging connection accepted
                                        pLogger.connectionAccepted(client.getPort());


                                        participantReceivers.add(new ParticipantReceiver(client, String.valueOf(participantPortNumber), numOfParticipants, pLogger));
                                    }
                                }catch (Exception e){
                                    //System.out.println("Participant "+otherParticipants.get(c)+ " has crashed");
                                    //pLogger.participantCrashed(Integer.parseInt(otherParticipants.get(c)));
                                    //e.printStackTrace();
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

                            //Logging end of round 1
                            pLogger.endRound(1);

                            //Logging start of round 2
                            pLogger.beginRound(2);

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

                            //Logging end of round 2
                            pLogger.endRound(2);

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

                            //removing participants who did not vote
                            for (ParticipantSender pS : participantSenders){
                                if (pS.getHasFailed()){
                                    otherParticipants.remove(String.valueOf(pS.getOtherPort()));
                                }
                            }


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                //System.out.println("Out of the for loop");
            }catch(Exception e){System.out.println("error "+e);}

            //------------------------------------------------------------------------------------

            //System.out.println("Testing");
            //int f = 0;

            //decide outcome here
            //Thread.sleep(20000);
            for(String vote : voteTally.keySet()){
                voteTally.replace(vote,getVoteFrequency(vote,participantVotingInRounds));
            }

            Thread.sleep(1000);
            //getMostPopularVote(voteTally);
            //System.out.println("MOST POPULAR CHOICE "+getMostPopularVote(voteTally));

            String outcomeMsg = "";
            for(String ports : otherParticipants){
                outcomeMsg = outcomeMsg + " " + ports;
            }

            out.println("OUTCOME " + getMostPopularVote(voteTally) + " "+ participantPortNumber + outcomeMsg);
            out.flush();

            //converting strings to integers for log message
            logParticipantIDs.add(participantPortNumber);

            //Logging outcome, and outcome msg
            pLogger.outcomeDecided(getMostPopularVote(voteTally),logParticipantIDs);
            pLogger.messageSent(coordPortNumber,"OUTCOME " + getMostPopularVote(voteTally) + " "+ participantPortNumber + outcomeMsg);
            pLogger.outcomeNotified(getMostPopularVote(voteTally),logParticipantIDs);

            System.out.println("The end");
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

    public static String getMostPopularVote(HashMap<String,Integer> voteTally){
        int a = -1;
        String highestCurrentVote = "Z";
        for(String vote : voteTally.keySet()){
            if (voteTally.get(vote) > a ){
                a = voteTally.get(vote);
                highestCurrentVote = vote;
            }else if(voteTally.get(vote) == a){
                if(Character.getNumericValue(highestCurrentVote.charAt(0)) > Character.getNumericValue(vote.charAt(0))){
                    highestCurrentVote = vote;
                }
            }
        }
        return  highestCurrentVote;
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
        ParticipantLogger pLogger;

        //PrintWriter out;
        //BufferedReader in;
        ParticipantReceiver(Socket c, String port,int numOfParticipants,ParticipantLogger pLogger) {
            client = c;
            this.selfPort = port;
            this.numOfParticipants = numOfParticipants;
            this.pLogger = pLogger;
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

                //creating a vote list for loggers
                ArrayList<Vote> voteList = new ArrayList<>();
                voteList.add(new Vote(Integer.parseInt(votingParticipantPort),otherParticipantVote));

                //Logging received votes
                pLogger.messageReceived(Integer.parseInt(votingParticipantPort),line);
                pLogger.votesReceived(Integer.parseInt(votingParticipantPort),voteList);
                voteList.remove(0);

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

                //updating a vote list for loggers
                for (String portWithVote : getParticipantVotingInRounds().keySet()){
                    //System.out.println(votingParticipantPort);
                    //System.out.println(client.getPort());

                    if(!(portWithVote.equals(votingParticipantPort))){
                        //System.out.println(portWithVote+ " IS NOT EQUAL TO " + votingParticipantPort);
                        voteList.add(new Vote(Integer.parseInt(portWithVote),getParticipantVotingInRounds().get(portWithVote)));
                    }
                }

                //Logging received votes
                pLogger.messageReceived(Integer.parseInt(votingParticipantPort),line);
                pLogger.votesReceived(Integer.parseInt(votingParticipantPort),voteList);


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

                client.close();
                System.out.println("CLOSED - receiver");
            } catch (Exception e) {
                //System.out.println(getVotingParticipantPort()+ " has crashed - receiver");
                //pLogger.participantCrashed(Integer.parseInt(votingParticipantPort));

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
            Boolean hasFailed = false;
            String voteChosen;
            int numOfParticipants;
            HashMap<String,String> participantVotingInRounds = new HashMap<>();  //selfport number, with vote
            ParticipantLogger pLogger;

        //PrintWriter out;
            //BufferedReader in;

            ParticipantSender(String myPort, int otherPort, String voteChosen, int numOfParticipants,ParticipantLogger pLogger){
                //client=c;
                setSelfPort(myPort);
                this.otherPort = otherPort;
                this.voteChosen = voteChosen;
                this.numOfParticipants = numOfParticipants;
                this.pLogger = pLogger;
            }

            //participant to participant comms
            //use synchro stuff, idiot
            public void run(){
                try {
                    Socket client = new Socket(InetAddress.getLocalHost(),otherPort);

                    //Logging connection established
                    pLogger.connectionEstablished(otherPort);

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

                    //creating a vote list for loggers
                    ArrayList<Vote> voteList = new ArrayList<>();
                    voteList.add(new Vote(Integer.parseInt(selfPort),voteChosen));

                    //Logging sent votes
                    pLogger.messageSent(otherPort,"VOTE " +selfPort + " " +voteChosen);
                    pLogger.votesSent(otherPort,voteList);
                    voteList.remove(0);

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

                    //updating a vote list for loggers
                    for (String portWithVote : getParticipantVotingInRounds().keySet()){
                        if(!(portWithVote.equals(selfPort))){
                            voteList.add(new Vote(Integer.parseInt(portWithVote),getParticipantVotingInRounds().get(portWithVote)));
                        }
                    }

                    //Logging sent votes
                    pLogger.messageSent(otherPort,"VOTE" + votePortMsg);
                    pLogger.votesSent(otherPort,voteList);


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


                    // -> got to make this recursive so that when no changes occur, it ends.
                    //while (something) {
                    // interpret (line) -> make sure nothing is wrong
                    // if there are differences -> record this in hashmap
                    // recheck with another round
                    // if all is cool, then stop looping.


                    client.close();
                    System.out.println("CLOSED - SENDER");
                }catch(Exception e){
                    System.out.println(otherPort+ " has crashed - sender");
                    pLogger.participantCrashed(otherPort);
                    hasFailed = true;

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

            public Boolean getHasFailed(){
                return this.hasFailed;
            }

            public int getOtherPort(){
                return this.otherPort;
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
