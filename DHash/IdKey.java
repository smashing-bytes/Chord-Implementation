package DHash;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

/**
 *
 * @author gasparosoft
 * @version 0.0.0.1
 */
public class IdKey extends Key implements Serializable
{

	private final BigInt hashKey;
	private final int pid;
	// private final int fsPort;
	private final String ip;
	private String hashKeytoString;
	private String[] macInter = { "" , "" };

	/*need to add fileserver port??? - FoX*/
	public IdKey(int pid, String ip)
	{
		this(Hashing.hash((ip +"|"+pid).getBytes()), pid, ip);
	}

	public IdKey(BigInt hashKey, int pid, String ip/*, int fsPort*/)
	{
		this.pid = pid;
		this.ip = ip;
		//  this.fsPort = fsPort;
		this.hashKey = hashKey;
		this.hashKeytoString = hashKey.toHexString();
		setMacAndInterface();
	}

	public int getPID()
	{
		return pid;
	}

	public String getIP()
	{
		return ip;
	}


	@Override
	public byte[] getByteKey()
	{
		return hashKey.getBytes();
	}

	@Override
	public boolean equals(Key k)
	{
		return hashKey.equals(k.getByteKey());
	}

	public BigInt getHashKey()
	{
		return this.hashKey;
	}

	@Override
	public String toString()
	{
		return getIP() + "|" + getPID();
	}

	public String hashKeytoHexString()
	{
		return this.hashKeytoString;
	}
	private void setMacAndInterface()
	{
		try
		{
			String ipA = getIP().substring(1);
			InetAddress inet = InetAddress.getByName(ipA);
			NetworkInterface nInterface = NetworkInterface.getByInetAddress(inet);

			if (nInterface != null)
			{
				byte[] macAddress = nInterface.getHardwareAddress();
				macInter[1] = nInterface.getDisplayName();

				if (macAddress != null)
				{
					for (byte b : macAddress)
					{
						String temp = Integer.toHexString(b & 0xFF).toUpperCase();

						if (temp.length() == 1)
						{
							temp = "0" + temp;
						}
						macInter[0] += temp +":";
					}
					macInter[0] = macInter[0].substring(0, macInter[0].length() - 1);
				}
				else
				{
					macInter[0] = "Unknown";
				}

			}
			else
			{
				macInter[0] = "Unknown";
				macInter[1] = "Unknown";
			}

			// return macInter;

		}
		catch (java.net.UnknownHostException ex)
		{
			macInter[0] = "Unknown";
			macInter[1] = "Unknown";
		}
		catch (SocketException ex)
		{
			macInter[0] = "Unknown";
			macInter[1] = "Unknown";
		}

	}

	public String[] getMacAndInterface()
	{

		return macInter;
	}

}
