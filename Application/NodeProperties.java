/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Application;
import java.net.InetAddress;
import java.io.Serializable;
/**
 *
 * @author haris
 */
public class NodeProperties implements Serializable
{
	private InetAddress nodeIP;
	private int FSPort;
	private int pid;
	private String bindName;


	public NodeProperties(InetAddress nodeIP, int FSPort, int pid, String bindName)
	{
		this.nodeIP = nodeIP;
		this.FSPort = FSPort;
		this.pid = pid;
		this.bindName = String.valueOf(pid);

	}

	public String getBindName()
	{
		return bindName;
	}

	public void setBindName(String bindName)
	{
		this.bindName = bindName;
	}


	public int getFSPort()
	{
		return FSPort;
	}

	public void setFSPort(int FSPort)
	{
		this.FSPort = FSPort;
	}

	public InetAddress getNodeIP()
	{
		return nodeIP;
	}

	public void setNodeIP(InetAddress nodeIP)
	{
		this.nodeIP = nodeIP;
	}

	public int getPid()
	{
		return pid;
	}

	public void setPid(int pid)
	{
		this.pid = pid;
	}

}