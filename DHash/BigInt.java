package DHash;

/**
 *
 * @author John Gasparis
 *  @version 0.0.0.1 Testing
 */
public interface BigInt
{

	public BigInt addition(byte[] secNumber);

	public BigInt powerOfTwo(int k);

	public BigInt addpowerOfTwo(int k);

	public String toHexString();

	public byte[] getBytes();

	public boolean equals(BigInt b);

	public boolean equals(byte[] b);

	public byte[] modM(int m);

	public BigInt subtraction(byte[] secNumber);

	public int compareTo(BigInt big);

	public BigInt shiftLeft(int shiftValue);

	public BigInt shiftRight(int shiftValue);

	public String toBinaryString();

	public BigInt binaryToBigInt(String s);

	public String toBinaryStringWithoutZeros();

	public String toHexStringWithoutZeros();

}

