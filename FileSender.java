// Author: Andrew Pang

import java.net.*;
import java.io.*;
import java.util.zip.*;
import java.nio.*;
import java.util.Arrays;
import java.util.zip.Checksum;


class FileSender {
    
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

            // // Checksum
            // byte[] bytes = Files.readAllBytes(Paths.get(fileToOpen)); 
            // CRC32 crc = new CRC32();
            // crc.update(bytes);
            // long checksum = crc.getValue();

            // Create packet
            byte[] buffer = new byte[1000];
            byte[] data = new byte[1000];
            byte[] seqArr = new byte[1];
            byte seq = 1;
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
                buffer = new byte[1000];
                data = new byte[992];
                numBytes = bis.read(data, 1, 991);
                //System.out.println(data.length);
                if(numBytes <= 0){
                        break;
                }
                data[0] = seq;
                crc.update(data);
                Checksum checksum1 = new CRC32();
                checksum1.update(data, 0, data.length);
                long ck = checksum1.getValue();
                //System.out.println(ck);
                //
                //crc.reset();
                //crc.update(data);


                //System.out.println(chkSum + "    " + checksum);
                //
                byte[] checksumArr = ByteBuffer.allocate(8).putLong(ck).array();
                System.arraycopy(checksumArr, 0, buffer, 0, checksumArr.length);
                System.arraycopy(data, 0, buffer, checksumArr.length, data.length);
                pkt = new DatagramPacket(buffer, numBytes+9, address, intPort);
                socket.send(pkt);

                // ByteBuffer wrapper = ByteBuffer.wrap(pkt.getData(), 0, 8);
                // long senderChecksum = wrapper.getLong();
                // byte[] restWrapper = pkt.getData();
                // byte[] rest = new byte[992];
                // System.arraycopy(restWrapper, 8, rest, 0, 992);

                // Checksum chkSum = new CRC32();
                // chkSum.update(rest, 0, rest.length);
                // long ck1 = chkSum.getValue();
                // System.out.println(ck1);
                //boolean areEqual = Arrays.equals(data, rest);
                //System.out.println(areEqual);
                //System.out.println(data.length + " and " + rest.length);
                
                //+ " + " + senderChecksum);
                //socket.receive(recPkt);
                //ByteBuffer wrapper = ByteBuffer.wrap(recPkt.getData(), 0, 1);
                //byte recSeq = wrapper.get();
                //System.out.println(wrapper);

                //seq = 1 - seq;
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