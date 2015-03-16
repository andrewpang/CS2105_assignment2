// Author: Andrew Pang

import java.net.*;
import java.io.*;
import java.util.zip.*;
import java.nio.file.*;


class FileSender {
    
    public DatagramSocket socket; 
    public DatagramPacket pkt;
    
    public static void main(String[] args) throws InterruptedException{
        
        // check if the number of command line argument is 4
        if (args.length != 3) {
            System.out.println("Usage: java FileSender <path/filename> "
                                   + "<unreliNetPort> <rcvFileName>");
            System.exit(1);
        }
        
        new FileSender(args[0], args[1], args[2]);
    }
    
    public FileSender(String fileToOpen, String port, String rcvFileName)  throws InterruptedException{
 
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try{
            fis = new FileInputStream(fileToOpen);
            bis = new BufferedInputStream(fis);

            // Create socket
            int intPort = Integer.parseInt(port);

            if (socket == null) {
                socket = new DatagramSocket(null);
                //socket.setReuseAddress(true);
                //socket.setBroadcast(true);
                //socket.bind(new InetSocketAddress(intPort));
            } 

            // Checksum
            byte[] bytes = Files.readAllBytes(Paths.get(fileToOpen)); 
            CRC32 crc = new CRC32();
            crc.update(bytes);
            long checksum = crc.getValue();

            // Create packet
            byte[] buffer = new byte[1000];
            InetAddress address = InetAddress.getByName("localhost");
            int numBytes;
            //Send filename
            buffer = rcvFileName.getBytes();
            pkt = new DatagramPacket(buffer, buffer.length, address, intPort);
            socket.send(pkt);
            buffer = new byte[1000];

            while (true) {
                numBytes = bis.read(buffer);
                if(numBytes <= 0){
                    break;
                }
                pkt = new DatagramPacket(buffer, numBytes, address, intPort);
                socket.send(pkt);
                //Thread.sleep(10);
            }
        } catch(IOException e) {
            System.out.println("Error:" + e);
        }finally {
            try{
                bis.close();
            } catch(IOException e){
                System.out.println("Error:" + e);
            }
        } 
    }
}