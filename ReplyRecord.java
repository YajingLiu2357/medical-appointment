import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class ReplyRecord extends Thread{
    String portNum = "";
    public ReplyRecord (String portNum){
        this.portNum = portNum;
    }
    @Override
    public void run (){
        try{
            DatagramSocket socket = new DatagramSocket(Integer.parseInt(portNum));
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket;
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            InetAddress addressRecord = receivePacket.getAddress();
            int portRecord = receivePacket.getPort();
            List<String> recordList;
            if (receivePacket.getData() != null){
                // TODO: May not QuebecServer
                recordList = QuebecServer.getRecordList();
                String replyRecord = "";
                if(recordList.size() == 0){
                    replyRecord = "Not available";
                }else{
                    replyRecord = recordList.toString();
                }
                DatagramPacket replyPacketRecord = new DatagramPacket(replyRecord.getBytes(), replyRecord.length(), addressRecord, portRecord);
                socket.send(replyPacketRecord);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
