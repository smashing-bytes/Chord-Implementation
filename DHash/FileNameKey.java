package DHash;

import java.io.Serializable;

/**
 *
 * @author John Gasparis
 *  @version 0.0.0.1
 */
public class FileNameKey extends Key implements Serializable
{

	private final String fileName;
	private final BigInt hashKey;
	private final String hashKeyHexString;

	/*public FileNameKey(byte[] key, String fileName)
	{
	this.hashKey = Hashing.hash(key);
	this.fileName = fileName;
	this.hashKeyHexString = this.hashKey.toHexString();
	}*/
	public FileNameKey(String fileName)
	{
		this(Hashing.hash(fileName.getBytes()), fileName);
	}

	public FileNameKey(BigInt key, String fileName)
	{
		this.hashKey = key;
		this.fileName = fileName;
		this.hashKeyHexString = this.hashKey.toHexString();
	}




	@Override
	public byte[] getByteKey()
	{
		return hashKey.getBytes();
	}

	public BigInt getBigInt()
	{
		return this.hashKey;
	}

	public String getFileName()
	{
		return fileName;
	}

	@Override
	public boolean equals(Key k)
	{
		return hashKey.equals(k.getByteKey());
	}

	public String hashKeytoHexString()
	{
		return this.hashKeyHexString;
	}

	public BigInt getHashKey()
	{
		return this.hashKey;
	}
}
