package Utilities;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author John Gasparis
 * @version 0.0.0.1
 */
public final class Log
{

	public static final byte INFORMATION = 0;
	public static final byte WARNING = 1;
	public static final byte ERROR = 2;
	public static final byte TABLES = 3;


	private String message;
	private byte type;
	private static BlockingQueue<Log> logQueue = new LinkedBlockingQueue<Log>();

	public Log(String message, byte type)
	{
		this.message = message;
		this.type = type;
	}

	public static void addMessage(String message, byte type)
	{
		try
		{
			Log.logQueue.put(new Log(message, type));
		}
		catch (InterruptedException ex)
		{
		}
	}

	public static Log getLogger()
	{
		try
		{
			return Log.logQueue.take();
		}
		catch (InterruptedException ex)
		{

			return null;
		}
	}

	public String getMessage()
	{
		return this.message;
	}

	public int getType()
	{
		return this.type;
	}
}

