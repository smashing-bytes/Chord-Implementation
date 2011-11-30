/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Application;

import DHash.IdKey;

/**
 *
 * @author gasparosoft
 */
public class FingerEntry implements Comparable<FingerEntry>
{

	private IdKey idKey;
	private int lastElement;
	private int firstElement;

	public FingerEntry(IdKey idKey , int firstElement , int lastElement)
	{
		this.idKey = idKey;
		this.lastElement = lastElement;
		this.firstElement = firstElement;
	}


	public IdKey getKey()
	{
		return idKey;
	}

	public int getLastElement()
	{
		return lastElement;
	}

	public void setLastElement(int element)
	{
		lastElement = element;
	}

	public int getFirstElement()
	{
		return this.firstElement;
	}

	public void setFirstElement(int element)
	{
		firstElement = element;
	}

	public boolean contains(IdKey idKey)
	{
		return this.idKey.equals(idKey);
	}


	public int compareTo(FingerEntry o)
	{
		FingerEntry obj = (FingerEntry)o;

		if (lastElement > obj.getLastElement())
			return 1;
		else if (lastElement < obj.getLastElement())
			return -1;
		else
		{
			return this.getKey().getHashKey().compareTo(o.getKey().getHashKey());
		}
	}
}