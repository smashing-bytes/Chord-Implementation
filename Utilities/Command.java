package Utilities;

import Application.Node;
import Application.NodeImpl;
import DHash.IdKey;
import Utilities.networking.IPTools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Random;
import java.util.Vector;
import java.rmi.*;
import java.rmi.server.*;

/**
 *
 * @author gasparosoft
 */
public class Command
{

	public final static String CREATE_NODE = "create node";
	//public static String FIND_FILE = "find FILENAME";
	public final static String INSERT_FILES = "insert FOLDERNAME";
	public final static String DOWNLOAD_FILE = "download FILENAME";
	public final static String CLEAR = "clear";
	public final static String MAN_PAGES = "man COMMAND";
	public final static String HELP = "help";
	public final static String DISCONNECT_NODE = "disconnect";
	public final static String QUIT_APPLICATION = "exit";

	private NodeImpl localNode;

	/**
	 * Creates a local Node
	 * @return An instance of Node
	 */
	public Node createNode() throws RemoteException
	{
		int pid = -1;
		String ip = null;

		try
		{

			// Finds Application's PID
			/*
			Vector<String> com = new Vector<String>();
			if ( System.getProperty("os.name").equals("Linux") ) {
			    com.add("/bin/bash");
			    com.add("-c");
			    com.add("echo $$");
			}

			ProcessBuilder builder = new ProcessBuilder(com);

			Process proc = builder.start();
			proc.waitFor();

			if (proc.exitValue() == 0) {
			    BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			    pid = Integer.parseInt(stdout.readLine().trim());
			}
			*/

			String pids = ManagementFactory.getRuntimeMXBean().getName();
			String[]Ids = pids.split("@");
			pid = Integer.valueOf(Ids[0]);

			//System.out.println(osProcessId + "\t" + pid);

			// Finds the Lan IP
			InetAddress ipInet = IPTools.getIP();
			ip = ipInet.getHostAddress();

		} /*catch (InterruptedException ie) {
            System.out.println(ie.toString());
            //logger
        } */
		catch (IOException ex)
		{
			System.out.println(ex.toString());
			return null;
			//logger
		}

		if (pid == -1)
		{
			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			pid = r.nextInt(30000);
		}

		IdKey idKey = new IdKey(pid, ip);

		// message for Logger
		// threads etc
		localNode = new NodeImpl(idKey);
		return localNode ;
	}


	private String findFile(String fileName)
	{
		// weird code
		return "192.168.1.1";
	}

	public String getManPage(String command)
	{
		return "Boring";
	}

	public String getHelp()
	{
		return "Usage: \n" +
		       "* create node\n" +
		       "* insert [DIR]\n" +
		       "* download [FILENAME]\n" +
		       "* clear\n" +
		       "* man [COMMAND]\n" +
		       "* help\n" +
		       "* disconnect\n" +
		       "* exit\n";
	}

	public boolean downloadFile(String fileName)
	{
		String ip = findFile(fileName);
		// weird code
		return false;
	}

	public void disconnect()
	{
		if (localNode == null)
			quit();
		else
		{
			//localNode.disconnect();
			quit();
		}
	}

	private void quit()
	{
		System.exit(0);
	}
}
