import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Coordinator{
    private ServerSocket ss;
    ArrayList<String> listOfParticipantPorts = new ArrayList<>();

    public static void main(String [] args){
        try {
            //int portSelected = Integer.parseInt(args[0]);                         //port to listen on
            //int portLogger = Integer.parseInt(args[1]);                         //logger port
            //int numberOfParticipants = Integer.parseInt(args[2]);         //number of participants
            //int timeoutValue = Integer.parseInt(args[3]);                 //time out value
            //voting options = the rest of the input arguments

            Coordinator coordinator = new Coordinator();
            //coordinator.startParticipant("Kri", 4323);
            /*for (int a = 0; a < 3; a++){
                //coordinator.startParticipant(hostname,portSelected);
                coordinator.startParticipant("Kri", 4323);
            }*/
            //coordinator.startCoordinator(4323, 10000);
            coordinator.startCoordinator(4323);
            //coordinator.startCoordinator(4323, timeoutValue);
            //coordinator.sendDetailsParticipants();


            //numberOfParticipants
        } catch (Exception e){

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

    public void startCoordinator(int port) throws Exception {
        ArrayList<ServiceThread> participantArray = new ArrayList<>();
        ss = new ServerSocket(port);
        /*while (true) {
            new ServiceThread(ss.accept()).start();
        }*/

        int a = 0;
        while(a < 3) {   //should be max number of participants

            //new ServiceThread(ss.accept()).start();
            participantArray.add(new ServiceThread(ss.accept()));

            if (a == 2){
                System.out.println("3 clients have joined");
                for (ServiceThread sT: participantArray){
                    sT.start();
                    Thread.sleep(10000);
                    System.out.println("WOOOHUUUU "+ sT.getPort());
                    listOfParticipantPorts.add(sT.getPort());
                    sT.setListOfParticipantPorts(listOfParticipantPorts);
                    //listOfParticipantPorts.add(sT.getPort());
                }
                //System.out.println("b4 port list");
                /*for (String test : listOfParticipantPorts){
                    System.out.println(test);
                }*/
            }

            a++;
        }

    }

    public void startCoordinator(int port,int timeout) throws IOException {
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
        }*/

    }
//participant crashing throws tcp and time out
//details contains list of ports -> partcipant

    public void startParticipant(String host, int portSelected) throws IOException {
        Participant2 participant = new Participant2();
        participant.start();
        //participant.startConnection(host,portSelected);

    }

    //should send details i.e what participants are currently in consensus excluding yourself.
    public void sendDetailsParticipants(){
        for(String portName: listOfParticipantPorts){
            System.out.println(portName);
        }

    }


    static class ServiceThread extends Thread{
        String port;
        Socket client;
        PrintWriter out;
        BufferedReader in;
        private volatile ArrayList<String> listOfParticipantPorts = new ArrayList<>();

        public ServiceThread(Socket c){
            client=c;
        }
// locks are good
        public void run(){
            try{
                //ArrayList<String> listOfParticipantPorts = new ArrayList<>();
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;

                line = in.readLine();
                //System.out.println(line);
                setPort(line.split(" ")[1]);
                //System.out.println(line.split(" ")[1]);
                //listOfParticipantPorts.add(line.split(" ")[1]);

                while((line = in.readLine()) != null) {
                    System.out.println(line + " received");
                    //System.out.println(line);
                    System.out.println("Sending ack");
                    out.println("THIS IS AN ACK");
                    out.flush();
                    Thread.sleep(3000);
                }

                System.out.println("I HAVE FINISHED");

                for(String portName: listOfParticipantPorts){
                    System.out.println(portName);
                }

                client.close();

            }catch(Exception e){

            }
        }

        //should send details i.e what participants are currently in consensus excluding yourself.
        /*private void sendingDetailsParticipants() {
            for(String portName: listOfParticipantPorts){
                System.out.println(portName);
            }

        }*/

        public void setPort(String port){
            this.port = port;
        }

        public String getPort(){
            return this.port;
        }

        public void setListOfParticipantPorts(ArrayList<String> listOfParticipantPorts){
            this.listOfParticipantPorts = listOfParticipantPorts;
        }

        public ArrayList<String> getListOfParticipantPorts(){
            return this.listOfParticipantPorts;
        }

    }







}
