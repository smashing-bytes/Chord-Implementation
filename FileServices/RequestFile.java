/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileServices;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author haris
 */
public class RequestFile
{


	public static void getFile(InetAddress server, int port, String filename) throws IOException
	{
		long fileSize;
		byte []fileSizeArray = new byte[8];
		Socket clientSocket;
		try
		{
			clientSocket = new Socket(server, port);
		}
		catch(ConnectException ce)
		{
			System.out.println("File Server is down on remote machine");
			System.out.println("Could not retrieve file.");
			return;
		}


		System.out.println("Connecting...");

		PrintStream out = new PrintStream(clientSocket.getOutputStream());

		/*Not currently used*/
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());

		/*Send Request*/
		out.println("GET " + filename);
		try
		{
			Thread.sleep(45);
		}
		catch (InterruptedException ex)
		{
			Logger.getLogger(RequestFile.class.getName()).log(Level.SEVERE, null, ex);
		}

		/*Read header, and allocate approriate size for file*/
		dataIn.read(fileSizeArray, 0, 8);
		fileSize = byteArrayToLong(fileSizeArray);

		if(fileSize == 0)
		{
			System.out.println("File was not found on remote machine.");
			return;
		}
		System.out.println("Filesize is: " + fileSize + "B");

		/*Start Receiving*/

		InputStream is = clientSocket.getInputStream();
		BufferedOutputStream fileWrite =  new BufferedOutputStream(new FileOutputStream("new file-copy.txt"));
		byte []buffer = new byte [256];
		int len = 0;

		System.out.println("Receiving File");
		while((len = is.read(buffer)) > 0 )
		{

			fileWrite.write(buffer, 0, len);
		}

		fileWrite.close();
		is.close();

		clientSocket.close();

	}

	private static long byteArrayToLong(byte []byteArray)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
		long l = byteBuffer.getLong();
		return l;
	}
}