package Application;

import DHash.BigInt;
import DHash.BigIntImpl;
import DHash.FileNameKey;
import DHash.Hashing;
import DHash.IdKey;
import DHash.Key;
import java.io.Serializable;

/**
 *
 * @author gasparosoft
 * @version 0.0.0.1
 * TESTING
 */
public class FIDEntry implements Serializable, Comparable
{

	private final Key fKey;
	private final Key idKey;
	private final String ip;
	private final int FSPort;

	public FIDEntry(String fileName, String ip, int pid, int FSPort)
	{
		fKey = new FileNameKey(fileName);
		idKey = new IdKey(Hashing.hash((ip + "|" + pid).getBytes()), pid, ip);
		this.ip = ip;
		this.FSPort = FSPort;

	}

	public Key getfKey()
	{
		return fKey;
	}

	public int getFSPort()
	{
		return FSPort;
	}

	public String getIp()
	{
		return ip;
	}


	public Key getIdKey()
	{
		return idKey;
	}

	public BigInt getHashKey()
	{
		return fKey.getHashKey();
	}

	public BigInt getHashedIDKey()
	{
		return new BigIntImpl(idKey.getByteKey());
	}

	public boolean equals(Key k)
	{
		return this.fKey.equals(k);
	}

	public byte[] getByteKey()
	{
		BigInt hashKey = new BigIntImpl(fKey.getByteKey());
		return hashKey.getBytes();
	}

	public int compareTo(Object o)
	{
		if (o instanceof FIDEntry)
		{
			FIDEntry entry = (FIDEntry) o;
			return this.fKey.getHashKey().compareTo(entry.getHashKey());
		}
		else
			return this.compareTo(o);
	}
}