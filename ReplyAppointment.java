import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;

public class ReplyAppointment extends Thread{
    String portNum = "";
    public ReplyAppointment(String portNum){
        this.portNum = portNum;
    }
    @Override
    public void run (){
        try{
            DatagramSocket socket = new DatagramSocket(Integer.parseInt(portNum));
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket;
            while(true){
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                InetAddress address = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String appointmentType = new String(receivePacket.getData(), 0, receivePacket.getLength());
                Map<String, Integer> appointment;
                if (receivePacket.getData() != null){
                    // TODO: May not QuebecServer
                    appointment = QuebecServer.getAppointment().get(appointmentType);
                    String reply = "";
                    if(appointment == null){
                        reply = "Not available";
                    }else{
                        reply = appointment.toString();
                    }
                    DatagramPacket replyPacket = new DatagramPacket(reply.getBytes(), reply.length(), address, port);
                    socket.send(replyPacket);
                }
            }
        }catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
