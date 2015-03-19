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
        CRC32 crc = new CRC32();
      
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
            long senderChecksum1 = bWrapper.getLong();
            //ByteBuffer seqWrapper = ByteBuffer.wrap(pkt.getData(), 8, 1); 
            //byte sendSeq = seqWrapper.get();
            String fileString = new String(pkt.getData(), 8, 992);
            String trimmedFile = fileString.trim();
            //System.out.println(fileString);
            crc.update(trimmedFile.getBytes());
            long chkSum = crc.getValue(); 

            if(chkSum == senderChecksum1){

            }
            int senderPort = pkt.getPort();
            //System.out.println(senderPort);

            //String received = new String(pkt.getData(), 0, pkt.getLength());
            fos = new FileOutputStream(trimmedFile); 
            InetAddress address = InetAddress.getByName("localhost");


            byte[] sendback = new byte[9];
            byte[] ackSeq = new byte[1];
            byte recSeq = (byte)1;
            while(true){
                buffer = new byte[1000];
                pkt = new DatagramPacket(buffer, buffer.length);
                socket.receive(pkt);


                ByteBuffer wrapper = ByteBuffer.wrap(pkt.getData(), 0, 8);
                long senderChecksum = wrapper.getLong();
                byte[] restWrapper = pkt.getData();
                
                ByteBuffer wrapper2 = ByteBuffer.wrap(pkt.getData(), 8, 4);
                int size = wrapper2.getInt();
                

                byte[] rest = new byte[992];
                System.arraycopy(restWrapper, 8, rest, 0, 992);
                byte senderSeq = rest[0];

                Checksum chkSum1 = new CRC32();
                chkSum1.update(rest, 0, rest.length);
                long ck1 = chkSum1.getValue();

                //System.out.println(ck1 + " + " + senderChecksum);
                if(senderSeq == recSeq){
                    ackSeq[0] = recSeq;
                    recSeq = (byte)(1-recSeq);
                    byte[] pktArr =  pkt.getData();
                    fos.write(pktArr, 9, pkt.getLength()-9);
                    //cut off extra
                } else{
                    ackSeq[0] = (byte)(1-recSeq);
                    
                }
                System.out.println(recSeq);

                Checksum ackChk = new CRC32();
                ackChk.update(ackSeq, 0, ackSeq.length);
                long ackCS = ackChk.getValue();
                ackChk.reset();
                byte[] ackChecksumArr = ByteBuffer.allocate(8).putLong(ackCS).array();
                System.arraycopy(ackSeq, 0, sendback, 0, 1);
                System.arraycopy(ackChecksumArr, 0, sendback, 1, 8);

                recPkt = new DatagramPacket(sendback, 9, address, senderPort);
                socket.send(recPkt);
                
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
