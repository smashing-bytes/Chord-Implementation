package Exceptions;

/**
 * thrown by create() of Chord, when the node we try to connect is
 * already connected
 */
public class AlreadyConnectedException extends Exception
{

	public AlreadyConnectedException()
	{
		super();
	}

	public AlreadyConnectedException(String message)
	{
		super(message);
	}
}
