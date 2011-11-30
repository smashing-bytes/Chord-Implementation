/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities.networking;

import Application.NodeProperties;
import java.io.IOException;
import java.io.Serializable;
import java.io.DataInputStream;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import Utilities.Log;

/**
 *
 * @author haris
 */
public class LocateBootstrapNode
{

    public static NodeProperties locate() throws IOException, InterruptedException
    {

        /*Stream declaration*/
        DataInputStream socketStream;

        /*Multicast a request to specific group (Nodes)*/
        MulticastSocket querySocket = null;
        DatagramSocket listeningSocket = null;
        InetAddress group = null;

        /*Dummy Request*/
        byte buff[] = new byte[562];


        /*Outcoming and incoming packet declaration*/
        DatagramPacket answer = null;
        DatagramPacket dummyPacket = null;
        try
        {

            /*Bind the sockets*/
            listeningSocket = new DatagramSocket(6000);
            listeningSocket.setReuseAddress(true);

            querySocket = new MulticastSocket(4400);
            querySocket.setLoopbackMode(false);
            
            group = InetAddress.getByName("230.0.1.5");

            querySocket.joinGroup(group);
            answer = new DatagramPacket(buff, buff.length);
            dummyPacket = new DatagramPacket(buff, buff.length, group, 4400);

            

        } catch (Exception ioe)
        {
            if (ioe instanceof IOException)
            {
                System.out.println("Error joining Multicast group.");
            } else if (ioe instanceof java.net.UnknownHostException)
            {
                System.out.println("Uknown host.");
            } else
            {
                System.out.println(ioe.getMessage());
            }
        }

        querySocket.setLoopbackMode(false);

        /*Send a packet to multicast group members*/
        Log.addMessage("Sending multicast from client for bootstraping", Log.INFORMATION);
        
        querySocket.send(dummyPacket);

        /*Set timeout for server to act accordingly*/
        listeningSocket.setSoTimeout(2000);

        try
        {
            listeningSocket.receive(answer);
            
         
            /*Get the bytes of the udp packet and deserialize it by custom method*/
            NodeProperties properties = (NodeProperties)toObject(answer.getData());
            Log.addMessage("Found a bootstrap node: " + properties.getPid(), Log.INFORMATION);
            listeningSocket.close();
            querySocket.close();

            return properties;
        } catch (java.net.SocketTimeoutException ste)
        {
            
            System.out.println("A bootstrap node was not found.");
            listeningSocket.close();
            querySocket.close();
            return null;
        }
        catch(Exception general)
        {
            System.out.println("Some exc:" + general.getMessage());
        }



        return null;

    }

    private static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException
    {
       java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        java.io.Serializable obj = (Serializable)ois.readObject();
        return obj;
    }
}
