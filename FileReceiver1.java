// Author: Andrew Pang

import java.net.*;
import java.io.*;
import java.util.zip.*;
import java.nio.*;

class FileReceiver {
    
    public DatagramSocket socket; 
    public DatagramPacket pkt;
    public DatagramPacket recPkt;
    
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
            ByteBuffer bWrapper = ByteBuffer.wrap(pkt.getData(), 0, 8);
            long senderChecksum = bWrapper.getLong();
            String fileString = new String(pkt.getData(), 8, 992);

            //String filename = new String(pkt.getData(), 8, pkt.getLength()); 
            //System.out.println(filename);
            //crc.update(filename.getBytes());
            //long chkSum = crc.getValue(); 

            //int senderPort = pkt.getPort();
            //System.out.println(senderPort);

            //String received = new String(pkt.getData(), 0, pkt.getLength());
            fos = new FileOutputStream(fileString); 
            InetAddress address = InetAddress.getByName("localhost");
            int seq = 1;

            while(true){
                //buffer = new byte[1000];
                //pkt = new DatagramPacket(buffer, buffer.length);
                socket.receive(pkt);

                //received = new String(pkt.getData(), 0, pkt.getLength());
                //System.out.println(pktLen);
                ByteBuffer wrapper = ByteBuffer.wrap(pkt.getData(), 0, 8);
                senderChecksum = wrapper.getLong();

                ByteBuffer restWrapper = ByteBuffer.wrap(pkt.getData(), 8, 992);
                //byte[] comp = new byte[992];
                //System.arraycopy(comp, 0, restWrapper, 0, restWrapper.length);
                CRC32 crc = new CRC32();
                crc.update(restWrapper);
                long checksum = crc.getValue();

                buffer = new byte[9];
                byte[] ackSeq = new byte[1];
                // if(checksum == senderChecksum){
                //     ackSeq[0] = seq;
                // }
                // else{
                //     ackSeq[0] = 1 - seq;
                // }
                crc = new CRC32();
                crc.update(ackSeq);
                long ackChecksum = crc.getValue();
                byte[] ackChecksumArr = ByteBuffer.allocate(8).putLong(checksum).array();
                System.arraycopy(ackSeq, 0, buffer, 0, 1);
                System.arraycopy(ackChecksumArr, 0, buffer, 1, ackChecksumArr.length);

                //System.out.println(senderPort);
                //recPkt = new DatagramPacket(buffer, 9, address, senderPort);
                //socket.send(recPkt);
                
                //fos.write(pkt.getData(), 9, 991);
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
