import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class CoordinatorTest {

    public void startCoordinator(int port) throws IOException {
        try{
            ArrayList<String> listOfParticipantPorts = new ArrayList<>();
            ServerSocket ss = new ServerSocket(port);
            for(;;){
                try{
                    final Socket client = ss.accept();
                    new Thread(new Runnable(){
                        public void run(){
                            try{
                                String test="";
                                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                String line;
                                //System.out.println("Adding from "+ client.getInetAddress());
                                PrintWriter out = new PrintWriter(client.getOutputStream());
                                line = in.readLine();
                                listOfParticipantPorts.add(line.split(" ")[1]);

                                /*for(String printPort : listOfParticipantPorts){
                                    System.out.println(printPort);
                                }*/

                                System.out.println(line);
                                while((line = in.readLine()) != null) {
                                    System.out.println(line + " received");
                                    //System.out.println(line);
                                    System.out.println("Sending ack");
                                    out.println("THIS IS AN ACK");
                                    out.flush();
                                    Thread.sleep(3000);
                                }
                                client.close();
                            }catch(Exception e){}
                        }
                    }).start();
                }catch(Exception e){System.out.println("error "+e);}
            }
        }catch(Exception e){System.out.println("error "+e);}
    }




    public static void main(String [] args) { //system arguments
        try {
            //int portSelected = Integer.parseInt(args[0]);                         //port to listen on
            //int portLogger = Integer.parseInt(args[1]);                         //logger port
            //int numberOfParticipants = Integer.parseInt(args[2]);         //number of participants
            //int timeoutValue = Integer.parseInt(args[3]);                 //time out value
            //voting options = the rest of the input arguments

            CoordinatorTest coordinator = new CoordinatorTest();
            coordinator.startCoordinator(4323);
            //Participant2 testingPart = new Participant2();
            //testingPart.run();

        } catch (Exception e){

        }
    }
}

