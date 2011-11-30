package FileServices;

import java.net.Socket;
import java.io.IOException;
import java.io.PrintStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;


/**
 *
 * @author haris
 */
public class Connection implements Runnable
{

	static Map<Byte, String> errorCodes;
	Socket tcpSocket;
	Share share;
	DataOutputStream out;

	protected Connection(Socket tcpSocket, Share share)
	{
		this.tcpSocket = tcpSocket;
		this.share = share;
		fillErrorCodes();
	}

	private static void fillErrorCodes()
	{
		errorCodes = new TreeMap<Byte, String>();
		errorCodes.put(new Byte((byte) -1), "Requested file was not found.");
		errorCodes.put(new Byte((byte) 0), "Send Success");
		errorCodes.put(new Byte((byte) 1), "Error 500: Malformed request");
		errorCodes.put(new Byte((byte) 2), "Send has failed, due to connection problems.");
	}

	public void run()
	{

		String request;
		byte execCode;
		try
		{
			/* Get client request input*/
			BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
			out = new DataOutputStream(tcpSocket.getOutputStream());

			request = in.readLine();
			execCode = parseRequest(request);

			/*Error handling in request*/
			System.out.println(errorCodes.get((Byte)execCode));

			tcpSocket.close();


		}
		catch (IOException ioe)
		{
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}

	private byte parseRequest(String request)
	{
		/*Valid request*/
		if (request.startsWith("GET"))
		{
			String filename = request.substring(3).trim();
			System.out.println("Host " + tcpSocket.getInetAddress().toString() +
			                   " is requesting the file " + filename);
			if (share.filenames.containsKey(filename))
			{

				System.out.println("File found and sending");
				if (sendFile(share.filenames.get(filename)))
				{
					return 0; //Send success
				}
				else
				{
					return 2; //Send EPIC fail
				}
			}
			else
				return -1; //File does not exist
		}
		else
		{
			/*Return 500 error - syntax error*/
			return 1;
		}


	}

	private boolean sendFile(String filename)
	{
		long bytesSent = 0;
		byte []buffer = new byte[4096];

		try
		{
			PrintStream outRespond = new PrintStream(tcpSocket.getOutputStream());

			/*Read the file into the byte array*/
			File transferFile = new File(filename);
			Long fileSize = new Long(transferFile.length());



			byte []fileSizeHeader = longToByteArray(fileSize.longValue());


			/*Send an 8 byte header with the file size*/
			out.write(fileSizeHeader);



			BufferedInputStream byteReader = new BufferedInputStream(new FileInputStream(transferFile));
			DataOutputStream out1 = new DataOutputStream(tcpSocket.getOutputStream());
			int len = 0, i = 0;

			while((len = byteReader.read(buffer)) > 0)
			{
				i++;
				out1.write(buffer, 0, len);

				bytesSent += len;
				if(i == 100)
				{
					System.out.println("Total bytes sent: " + bytesSent);
					i = 0;

				}

			}


			out1.flush();
			return true;
		}
		catch (IOException ioe)
		{
			System.out.println(ioe.getMessage());
			return false;
		}
	}


	private byte[] longToByteArray(long l)
	{
		byte[] byteArray = new byte[8];
		ByteBuffer bBuffer = ByteBuffer.wrap(byteArray);
		LongBuffer lBuffer = bBuffer.asLongBuffer();
		lBuffer.put(0, l);
		return byteArray;

	}
}
