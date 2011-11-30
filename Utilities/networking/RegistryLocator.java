/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities.networking;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.io.IOException;
import Utilities.Log;

/**
 *
 * @author haris
 */
public class RegistryLocator
{

    private static java.rmi.registry.Registry locateRMIRegistry() throws IOException, InterruptedException
    {

        /*Multicast a request to specific group (RMI Server) compliant*/
        MulticastSocket querySocket = null;
        DatagramSocket listeningSocket = null;
        InetAddress group = null;

        /*Request string*/
        final String request = "RQ";
        byte buff[] = new byte[32];
        buff = request.getBytes();

        /*Outcoming and incoming packet declaration*/
        DatagramPacket answer = null;
        DatagramPacket packet_out = null;
        try
        {
            /*Bind the sockets*/
            listeningSocket = new DatagramSocket(4440);
            querySocket = new MulticastSocket(4446);
            group = InetAddress.getByName("230.0.0.1");


            answer = new DatagramPacket(buff, buff.length);
            packet_out = new DatagramPacket(buff, buff.length, group, 4446);

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
        Log.addMessage("Sending multicast from client", Log.INFORMATION);

        querySocket.send(packet_out);

        /*Set timeout for server to act accordingly*/
        listeningSocket.setSoTimeout(2000);

        try
        {
            listeningSocket.receive(answer);
            Log.addMessage("Found an RMI Registry Server at: " + answer.getAddress().toString().substring(1), Log.INFORMATION);
            listeningSocket.close();
            querySocket.close();

            return java.rmi.registry.LocateRegistry.getRegistry(answer.getAddress().toString().substring(1));
        } catch (java.net.SocketTimeoutException ste)
        {
            /*An RMI Registry service was not found*/
            Log.addMessage("An RMI Registry service was not found.", Log.WARNING);
            listeningSocket.close();
            querySocket.close();
            return null;
        }
    }
}
