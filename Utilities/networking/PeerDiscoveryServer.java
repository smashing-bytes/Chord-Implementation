package Utilities.networking;

import java.io.IOException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import Application.NodeProperties;
import java.util.logging.Level;
import java.util.logging.Logger;
import Utilities.Log;

/**
 *
 * @author haris
 */
public class PeerDiscoveryServer implements Runnable
{

    NodeProperties properties;
    DatagramPacket bootstrapMSG;

    public PeerDiscoveryServer(NodeProperties properties)
    {
        this.properties = properties;

    }

    public static byte[] getBytes(Object obj) throws java.io.IOException
    {
        Serializable mySerializableObj = (Serializable) obj;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mySerializableObj);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }

    public void run()
    {
        MulticastSocket listeningSocket = null;
        DatagramSocket responseSocket = null;
        InetAddress group;
        DatagramPacket bootstrapMsg, dummyPacket = null;
        byte[] buff = null;

        try
        {
            
            listeningSocket = new MulticastSocket(4400);
            listeningSocket.setReuseAddress(true);

            responseSocket = new DatagramSocket();

            group = InetAddress.getByName("230.0.1.5");

            listeningSocket.joinGroup(group);


            buff = new byte[30];
            bootstrapMsg = new DatagramPacket(buff, buff.length);
            dummyPacket = new DatagramPacket(buff, buff.length);
      
        } catch (Exception ioe)
        {

            ioe.printStackTrace();
        }


        while (!Thread.interrupted())
        {
            try
            {
                listeningSocket.receive(dummyPacket);
                System.out.print("oops");
                /*Generate the answer*/
                buff = getBytes(properties);
                Log.addMessage("Request came from " + dummyPacket.getAddress().toString(), Log.WARNING);
                bootstrapMsg = new DatagramPacket(buff, buff.length, dummyPacket.getAddress(), 6000);
                responseSocket.send(bootstrapMsg);
            } catch (IOException ex)
            {
                
                Logger.getLogger(PeerDiscoveryServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
