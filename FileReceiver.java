// Author: Andrew Pang

import java.net.*;
import java.io.*;
import java.util.zip.*;
import java.nio.file.*;

class FileReceiver {
    
    public DatagramSocket socket; 
    public DatagramPacket pkt;
    
    public static void main(String[] args) {
        
        // check if the number of command line argument is 1
        if (args.length != 1) {
            System.out.println("Usage: java FileReceiver port");
            System.exit(1);
        }
        
        new FileReceiver(args[0]);
    }
    
    public FileReceiver(String localPort) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
      
        int intPort = Integer.parseInt(localPort);
        try{
            
            //socket = new DatagramSocket(intPort);
            if (socket == null) {
                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                socket.bind(new InetSocketAddress(intPort));
            } 
            byte[] buffer = new byte[1000];
            pkt = new DatagramPacket(buffer, buffer.length);
            socket.receive(pkt);
            String received = new String(pkt.getData(), 0, pkt.getLength());
            fos = new FileOutputStream(received); 

            while(true){
                //buffer = new byte[1000];
                //pkt = new DatagramPacket(buffer, buffer.length);
                socket.receive(pkt);
                //received = new String(pkt.getData(), 0, pkt.getLength());
                //System.out.println(pktLen);
                fos.write(pkt.getData(), 0, pkt.getLength());
                if(pkt.getLength() != 1000){
                    break;
                }
        }


        } catch(SocketException s){
            System.out.println("Error" + s);
        } catch(IOException e){
            System.out.println("Error" + e);
        }
    }
}
