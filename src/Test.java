public class Test {
    public static void main(String[] args) {
        System.out.println("Ho");
    }
}
/*
import java.io.*;
import java.net.*;

class TCPReceiver{
    public static void main(String [] args){
        try{
            ServerSocket ss = new ServerSocket(4323);
            for(;;){ //while true loop
                try{
                    Socket client = ss.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String line;
                    while((line = in.readLine()) != null) { //reads input stream while the input stream is not null
                        System.out.println(line + " received");
                    }
                    client.close();
                }catch(Exception e){
                    System.out.println("error "+e);
                }
            }
        }catch(Exception e){
            System.out.println("error "+e);
        }
    }
}

 */