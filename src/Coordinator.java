import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Coordinator {
    private ServerSocket ss;
    ArrayList<String> listOfParticipantPorts = new ArrayList<>();

    public static void main(String[] args) {
        try {
            //int portSelected = Integer.parseInt(args[0]);                         //port to listen on
            //int portLogger = Integer.parseInt(args[1]);                         //logger port
            int numberOfParticipants = Integer.parseInt(args[0]);         //number of participants
            //int timeoutValue = Integer.parseInt(args[3]);                 //time out value
            //voting options = the rest of the input arguments

            Coordinator coordinator = new Coordinator();
            //coordinator.startParticipant("Kri", 4323);
            /*for (int a = 0; a < 3; a++){
                //coordinator.startParticipant(hostname,portSelected);
                coordinator.startParticipant("Kri", 4323);
            }*/
            //coordinator.startCoordinator(4323, 10000);
            coordinator.startCoordinator(4323,numberOfParticipants);
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

    public void startCoordinator(int port,int numParticipants) throws Exception {
        ArrayList<ServiceThread> participantArray = new ArrayList<>();
        ArrayList<String> testing = new ArrayList<>();
        ArrayList<String> votingOptions = new ArrayList<>();
        System.out.println(numParticipants + " are expected to join");

        //this.votingOptions = votingOptions; // parameter
        ss = new ServerSocket(port);

        /*while (true) {
            new ServiceThread(ss.accept()).start();
        }*/

        int a = 0;
        while (a < numParticipants) {   //should be max number of participants
            //new ServiceThread(ss.accept()).start();
            participantArray.add(new ServiceThread(ss.accept()));

            if (a == (numParticipants-1)) {
                ss.setSoTimeout(1000);
                votingOptions.add("A");
                votingOptions.add("B");
                votingOptions.add("C");

                System.out.println( numParticipants+ " clients have joined");

                //running the clients
                for (ServiceThread sT : participantArray) {
                    sT.start();
                }

                //getting ports of each participant
                Thread.sleep(2000);
                for (ServiceThread sT : participantArray) {
                    //System.out.println("WOOOHUUUU " + sT.getPort());
                    listOfParticipantPorts.add(sT.getPort());
                    sT.setListOfParticipantPorts(listOfParticipantPorts);
                    testing = sT.getListOfParticipantPorts();
                }

                //passing the numOfParticipants
                for (ServiceThread sT : participantArray) {
                    sT.setNum(numParticipants);
                }

                //testing whether setting something works
                /*Thread.sleep(3000);
                for (ServiceThread sT : participantArray) {
                    sT.setPort("69420");
                    System.out.println("has it changed port numbers");
                }*/

                //setting first check
                Thread.sleep(2000);
                for (ServiceThread sT : participantArray) {
                    sT.setFirstCheck();
                    System.out.println("Unlocking first gate");
                }

                //providing voting options for participants
                Thread.sleep(2000);
                for (ServiceThread sT : participantArray){
                    sT.setVotingOptions(votingOptions);
                    System.out.println("Setting voting options");
                }

                //setting second check
                Thread.sleep(2000);
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
/*
    public void startCoordinator(int port, int timeout) throws IOException {
        ss = new ServerSocket(port);
        ss.setSoTimeout(timeout);

        while (true) {
            new ServiceThread(ss.accept()).start();
        }
        /*int a = 0;
        while(a < 3) {   //should be max number of participants

            new ServiceThread(ss.accept()).start();
            //ServiceThread test = new ServiceThread(ss.accept());

            if (a == 2){
                System.out.println("3 clients have joined");

            }

            a++;
        }

    }
    */

    //participant crashing throws tcp and time out
//details contains list of ports -> partcipant

    public void startParticipant(String host, int portSelected) throws IOException {
        Participant2 participant = new Participant2();
        participant.start();
        //participant.startConnection(host,portSelected);

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
        private volatile ArrayList<String> listOfParticipantPorts = new ArrayList<>();
        private volatile ArrayList<String> votingOptions = new ArrayList<>();

        public ServiceThread(Socket c) {
            client = c;
        }

        // locks are good
        public void run() {
            try {
                //ArrayList<String> listOfParticipantPorts = new ArrayList<>();
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;

                line = in.readLine();
                setPort(line.split(" ")[1]);
                System.out.println(line);

                //Thread.sleep(10000);
                //System.out.println(getPort());

                //first lock
                System.out.println("Hit first lock");
                while (firstCheck) {
                    Thread.sleep(2000);
                    System.out.println("In first lock");
                }
                System.out.println("Exited first lock");

                //sending num of participants
                out.println(numOfParticipants+" OF PARTICIPANTS");
                out.flush();

                //sending details block
                for (String portName : getListOfParticipantPorts()) {
                    if (!(portName == getPort())) {
                        detailsMsg = detailsMsg + " " +portName;
                    }
                }

                out.println("DETAILS" + detailsMsg);
                out.flush();

                //second lock
                System.out.println("Hit second lock");
                while (secondCheck) {
                    Thread.sleep(2000);
                    System.out.println("In second lock");
                }
                System.out.println("Exited second lock");

                //sending vote options
                for (String voteOption : getVotingOptions()) {
                    votingOptionsMsg = votingOptionsMsg + " " + voteOption;
                }

                out.println("VOTING_OPTIONS" + votingOptionsMsg);
                out.flush();

                //third lock
                /*System.out.println("Hit third lock");
                while (thirdCheck) {
                    Thread.sleep(2000);
                    System.out.println("In third lock");
                }
                System.out.println("Exited third lock");*/



                while ((line = in.readLine()) != null) {
                    System.out.println(line + " received");
                    //System.out.println(line);
                    System.out.println("Sending ack");
                    out.println("THIS IS AN ACK");
                    out.flush();
                    Thread.sleep(3000);
                }


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

            public void setVotingOptions(ArrayList<String> votingOptions){
                this.votingOptions = votingOptions;
            }

            public ArrayList<String> getVotingOptions(){
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
