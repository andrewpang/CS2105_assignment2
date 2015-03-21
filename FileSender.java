// Author: Andrew Pang

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.nio.*;
import java.util.Arrays;
import java.util.zip.Checksum;

class sender extends TimerTask{
    DatagramSocket socket;
    DatagramPacket packet;
    public sender(DatagramSocket skt, DatagramPacket pkt){
        socket = skt;
        packet = pkt;
    }
    public void run(){
        try{
            socket.send(packet);
        } catch(IOException e) {
            System.out.println("Error:" + e);
        }
    }
}
class FileSender{
    
    public DatagramSocket socket; 
    public DatagramPacket pkt;
    public DatagramPacket recPkt;
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
            socket.setSoTimeout(300);
            // // Checksum
            // byte[] bytes = Files.readAllBytes(Paths.get(fileToOpen)); 
            // CRC32 crc = new CRC32();
            // crc.update(bytes);
            // long checksum = crc.getValue();

            // Create packet
            byte[] buffer = new byte[1000];
            byte[] data = new byte[1000];
            byte[] seqArr = new byte[1];
            byte seq = (byte)1;
            seqArr[0] = seq;

            InetAddress address = InetAddress.getByName("localhost");
            int numBytes;
            CRC32 crc = new CRC32();
            //Send filename
            byte[] filename = rcvFileName.getBytes();
            crc.update(filename);
            long checksum = crc.getValue();
            byte[] checksumByte = ByteBuffer.allocate(8).putLong(checksum).array();
            int totalSize = filename.length + checksumByte.length;

            System.arraycopy(checksumByte, 0, buffer, 0, checksumByte.length);
            //System.arraycopy(seqArr, 0, buffer, checksumByte.length, 1);
            System.arraycopy(filename, 0, buffer, checksumByte.length, filename.length);
           
            pkt = new DatagramPacket(buffer, totalSize, address, intPort);

            socket.send(pkt);

           

            while (true){
                
                data = new byte[992];
                byte[] recBuffer = new byte[1000];
                numBytes = bis.read(data, 1, 991);
                //System.out.println(numBytes);
                if(numBytes < 991 && numBytes > 0){
                    byte[] lastData = new byte[numBytes+4];
                    System.arraycopy(data, 1, lastData, 0, numBytes);
                    data = new byte[numBytes+1];
                    System.arraycopy(lastData, 0, data, 1, numBytes);
 
                }
                if(numBytes <= 0){
                        break;
                }

                data[0] = seq;
                crc.update(data);
                Checksum checksum1 = new CRC32();
                checksum1.update(data, 0, data.length);
                long ck = checksum1.getValue();

                buffer = new byte[data.length+8];
                
                    // System.out.println(buffer.length);
                byte[] checksumArr = ByteBuffer.allocate(8).putLong(ck).array();
                System.arraycopy(checksumArr, 0, buffer, 0, checksumArr.length);
                System.arraycopy(data, 0, buffer, checksumArr.length, data.length);
                pkt = new DatagramPacket(buffer, data.length+8, address, intPort);
                
                while(true){
                    try{
                        socket.send(pkt);
                        recPkt = new DatagramPacket(recBuffer, recBuffer.length);
                        socket.receive(recPkt);

                        byte[] rec = recPkt.getData();

                        ByteBuffer recWrapper = ByteBuffer.wrap(recPkt.getData(), 1, 8);
                        long recChecksum = recWrapper.getLong();
                        byte[] recSeq = new byte[1];
                        System.arraycopy(rec, 0, recSeq, 0, 1);

                        Checksum ackCheck = new CRC32();
                        ackCheck.update(recSeq, 0, recSeq.length);
                        long ackCheckVal = ackCheck.getValue();

                            //System.out.println(seq);
                        if((recSeq[0] == seq) && (recChecksum == ackCheckVal)){
                            seq = (byte)(1 - seq);
                            break;
                            //timer.cancel();
                        } 
                    } catch (SocketTimeoutException s) {
                        //System.out.println("this shit corrupted");
                        socket.send(pkt);
                        continue;
                    }
                }

                
                //else{
                    //keep sending packet
                    //socket.send(pkt);
                    //break;
                //}               
            }
            //while (true) {
              //  numBytes = bis.read(buffer, 9, 991);
                //if(numBytes <= 0){
                  //  break;
                //}
                //pkt = new DatagramPacket(buffer, 1000, address, intPort);
                //socket.send(pkt);
                //Thread.sleep(10);
            //}
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