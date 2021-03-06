import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

class Coordinator {
    private ServerSocket ss;
    ArrayList<String> listOfParticipantPorts = new ArrayList<>();

    public static void main(String[] args) {
        try {
            int portSelected = Integer.parseInt(args[0]);                         //port to listen on
            int portLogger = Integer.parseInt(args[1]);                         //logger port
            int numberOfParticipants = Integer.parseInt(args[2]);         //number of participants
            int timeoutValue = Integer.parseInt(args[3]);                 //time out value
            ArrayList<Character> votingOptions = new ArrayList<>();
            CoordinatorLogger.initLogger(portLogger,portSelected,timeoutValue);
            CoordinatorLogger cLogger = CoordinatorLogger.getLogger();


            for (int a = 4 ;a < args.length ; a++){
                votingOptions.add(args[a].charAt(0));
                //System.out.println(Character.getNumericValue(args[a].charAt(0)));
            }

            Coordinator coordinator = new Coordinator();
            //coordinator.startParticipant("Kri", 4323);
            /*for (int a = 0; a < 3; a++){
                //coordinator.startParticipant(hostname,portSelected);
                coordinator.startParticipant("Kri", 4323);
            }*/
            //coordinator.startCoordinator(4323, 10000);
            coordinator.startCoordinator(portSelected,numberOfParticipants,timeoutValue,votingOptions, portLogger,cLogger);
            //coordinator.startCoordinator(4323, timeoutValue);
            //coordinator.sendDetailsParticipants();


            //numberOfParticipants
        } catch (Exception e) {


        }
    }

    /* from old tcp threaded class main(string[] args)
        try{
            ServerSocket ss = new ServerSocket(4323);
            for(;;){
                try{Socket client = ss.accept();
                    new Thread(new ServiceThread(client)).start();
                }catch(Exception e){System.out.println("error "+e);}
            }
        }catch(Exception e){System.out.println("error "+e);}
     */

    public void startCoordinator(int port,int numParticipants,int timeoutValue, ArrayList<Character> votingOptions, int portLogger,CoordinatorLogger cLogger) throws Exception {
        ArrayList<ServiceThread> participantArray = new ArrayList<>();
        int loggingPortUsed = portLogger;
        Object lock = new Object();


        System.out.println(numParticipants + " are expected to join");

        //this.votingOptions = votingOptions; // parameter
        ss = new ServerSocket(port);

        /*while (true) {
            new ServiceThread(ss.accept()).start();
        }*/

        int a = 0;
        while (a < numParticipants) {   //should be max number of participants
            //new ServiceThread(ss.accept()).start();

            //Logging started listening
            cLogger.startedListening(port);

            Socket client = ss.accept();
            participantArray.add(new ServiceThread(client,cLogger,lock));

            //Logging connection accepted
            cLogger.connectionAccepted(client.getPort());

            if (a == (numParticipants-1)) {
                ss.setSoTimeout(timeoutValue);


                System.out.println( numParticipants+ " clients have joined");

                //running the clients
                for (ServiceThread sT : participantArray) {
                    sT.start();
                }


                //getting ports of each participant
                Thread.sleep(500);
                for (ServiceThread sT : participantArray) {
                    //System.out.println("WOOOHUUUU " + sT.getPort());
                    listOfParticipantPorts.add(sT.getPort());
                    sT.setListOfParticipantPorts(listOfParticipantPorts);
                    //testing = sT.getListOfParticipantPorts();
                }

                //passing the numOfParticipants
                for (ServiceThread sT : participantArray) {
                    sT.setNum(numParticipants);
                }



                //setting first check
                Thread.sleep(500);
                for (ServiceThread sT : participantArray) {
                    sT.setFirstCheck();
                    System.out.println("Unlocking first gate");
                }

                //providing voting options for participants
                Thread.sleep(500);
                for (ServiceThread sT : participantArray){
                    sT.setVotingOptions(votingOptions);
                    System.out.println("Setting voting options");
                }

                //setting second check
                Thread.sleep(500);
                for (ServiceThread sT : participantArray) {
                    sT.setSecondCheck();
                    System.out.println("Unlocking second gate");
                }



                /*//testing what participants have been added
                Thread.sleep(3000);
                System.out.println("b4 port list");
                for (String test : testing) {
                    System.out.println("WOOT " +test);
                }*/

            }

            a++;
        }

    }

    //should send details i.e what participants are currently in consensus excluding yourself.
    public void sendDetailsParticipants() {
        for (String portName : listOfParticipantPorts) {
            System.out.println(portName);
        }

    }


    static class ServiceThread extends Thread {
        String port;
        Socket client;
        PrintWriter out;
        BufferedReader in;
        int numOfParticipants;
        String detailsMsg = "";
        String votingOptionsMsg = "";
        Boolean firstCheck = true;
        Boolean secondCheck = true;
        Boolean thirdCheck = true;
        ArrayList<String> listOfParticipantPorts = new ArrayList<>();
        ArrayList<Character> votingOptions = new ArrayList<>();
        CoordinatorLogger cLogger;
        Object lock;
        Boolean processFailed = false;


        public ServiceThread(Socket c, CoordinatorLogger cLogger,Object lock) {
            client = c;
            this.cLogger = cLogger;
            this.lock = lock;
        }

        // locks are good
        public void run() {
            try {
                //ArrayList<String> listOfParticipantPorts = new ArrayList<>();
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;

                //receives join msg
                line = in.readLine();
                setPort(line.split(" ")[1]);
                System.out.println(line);


                //Logging join msg
                cLogger.messageReceived(Integer.parseInt(getPort()),line);
                cLogger.joinReceived(Integer.parseInt(getPort()));


                //first lock
                System.out.println("Hit first lock");
                while (firstCheck) {
                    Thread.sleep(500);
                    //System.out.println("In first lock");
                }
                System.out.println("Exited first lock");


                //sending num of participants
                out.println(numOfParticipants+" IS HOW MANY PARTICIPANTS THERE SHOULD BE");
                out.flush();

                //sending details block
                for (String portName : getListOfParticipantPorts()) {
                    if (!(portName == getPort())) {
                        detailsMsg = detailsMsg + " " +portName;
                    }
                }

                out.println("DETAILS" + detailsMsg);
                out.flush();

                //converts strings to integers for logger
                ArrayList<Integer> participantList = new ArrayList<>();
                for (String ports : getListOfParticipantPorts()){
                    if(!(getPort().equals(ports))){
                        participantList.add(Integer.parseInt(ports));
                    }
                }

                //Logging details sent
                cLogger.messageSent(Integer.parseInt(getPort()),"DETAILS" + detailsMsg);
                cLogger.detailsSent(Integer.parseInt(getPort()),participantList);

                //second lock
                System.out.println("Hit second lock");
                while (secondCheck) {
                    Thread.sleep(500);
                    //System.out.println("In second lock");
                }
                System.out.println("Exited second lock");

                //sending vote options
                for (Character voteOption : getVotingOptions()) {
                    votingOptionsMsg = votingOptionsMsg + " " + voteOption;
                }

                out.println("VOTING_OPTIONS" + votingOptionsMsg);
                out.flush();

                //converts characters to strings for logger
                ArrayList<String> votingOptionsList = new ArrayList<>();
                for (Character votes : getVotingOptions()){
                    votingOptionsList.add(String.valueOf(votes));
                }

                //Logging voting options sent
                cLogger.messageSent(Integer.parseInt(port),"VOTING_OPTIONS" + votingOptionsMsg);
                cLogger.voteOptionsSent(Integer.parseInt(getPort()), votingOptionsList);


                //third lock
                /*System.out.println("Hit third lock");
                while (thirdCheck) {
                    Thread.sleep(2000);
                    System.out.println("In third lock");
                }
                System.out.println("Exited third lock");*/


                //Reads outcome msg
                //Thread.sleep(25000);
                line = in.readLine();
                System.out.println(line);

                //Logging outcome msg received
                cLogger.messageReceived(Integer.parseInt(getPort()),line);
                cLogger.outcomeReceived(Integer.parseInt(getPort()),line.split(" ")[1]);

                /*
                while ((line = in.readLine()) != null) {
                    System.out.println(line);

                }*/


                /*for(int a = 0; a<5 ;a++){
                    System.out.println(line + " received");
                    //System.out.println(line);
                    System.out.println("Sending ack");
                    out.println("THIS IS AN ACK");
                    out.flush();
                    Thread.sleep(3000);
                }*/

                    System.out.println("I HAVE FINISHED");


                    client.close();
                }catch(Exception e){
                    System.out.println( getPort() + " has crashed");
                    cLogger.participantCrashed(Integer.parseInt(getPort()));
                    processFailed = true;
                }
            }

            //should send details i.e what participants are currently in consensus excluding yourself.
        /*private void sendingDetailsParticipants() {
            for(String portName: listOfParticipantPorts){
                System.out.println(portName);
            }*/



            public void setPort (String port){
                this.port = port;
            }

            public String getPort() {
                return this.port;
            }

            public Boolean getProcessFailed() {
                return this.processFailed;
            }

            public void setListOfParticipantPorts (ArrayList < String > listOfParticipantPorts) {
                this.listOfParticipantPorts = listOfParticipantPorts;
            }

            public ArrayList<String> getListOfParticipantPorts () {
                return this.listOfParticipantPorts;
            }

            public void setFirstCheck() {
                this.firstCheck = false;
            }
            public void setSecondCheck() {
            this.secondCheck = false;
        }
            public void setThirdCheck() {
            this.thirdCheck = false;
        }

            public void setVotingOptions(ArrayList<Character> votingOptions){
                this.votingOptions = votingOptions;
            }

            public ArrayList<Character> getVotingOptions(){
                return this.votingOptions;
            }
            public void setNum(int numOfParticipants){
                this.numOfParticipants = numOfParticipants;
            }

        }


        public void setListOfParticipantPorts(ArrayList<String> listOfParticipantPorts) {
            this.listOfParticipantPorts = listOfParticipantPorts;
        }

        public ArrayList<String> getListOfParticipantPorts() {
            return this.listOfParticipantPorts;
        }


}
// vote is selected only once,
// first vote is simple, send what u voted to others
// subsequent votes are u discovering what everybody else voted for. -> send the information u do know to others.
