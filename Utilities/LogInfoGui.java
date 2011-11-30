/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Application.Node;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author gasparosoft
 */
public class LogInfoGui extends JFrame
{

	private Node node;

	public LogInfoGui(Node n)
	{

		setTitle("ChordFPG - Log Info");

		this.node = n;

		JPanel panel = new JPanel();
		JLabel label = new JLabel();
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Utilities/resources/logo2.gif")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		String[] log;
		try
		{
			log = node.getWholeLog();


			String[] info = log[0].split("\n");
			String[] tables = log[1].split("\n");
			String[] warnings = log[2].split("\n");
			String[] errors = log[3].split("\n");
			int i;
			String text = "<html>"
			              + "<h3> Information </h3>"
			              + "<ul>";

			for (i = 0; i < info.length; i++)
			{
				text += "<li>" + info[i] + "</li>";
			}
			text += "</ul>"
			        + "<h3>Tables</h3>"
			        + "<ul>";
			for (i = 0; i < tables.length; i++)
			{
				text += "<li>" + tables[i] + "</li>";
			}
			text += "</ul>"
			        + "<h3>Warnings</h3>"
			        + "<ul>";
			for (i = 0; i < warnings.length; i++)
			{
				text += "<li>" + warnings[i] + "</li>";
			}
			text += "</ul>"
			        + "<h3>Errors</h3>"
			        + "<ul>";
			for (i = 0; i < errors.length; i++)
			{
				text += "<li>" + errors[i] + "</li>";
			}
			text += "</ul>"
			        + "</html>";


			label.setText(text);
			panel.setSize(500, 700);
			panel.add(label);


			JScrollPane scrollPane = new JScrollPane(panel);
			this.getContentPane().add(scrollPane, BorderLayout.CENTER);
			setSize(550, 750);
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screen.width / 2) - (this.getWidth() / 2);
			int y = (screen.height / 2) - (this.getHeight() / 2);
			this.setLocation(x,y);
			setVisible(true);
		}
		catch (RemoteException ex)
		{
			JOptionPane.showMessageDialog(this, "A problem occurred while collecting info for the node. Please try again.", "ChordFPG said:", JOptionPane.ERROR_MESSAGE);
		}
	}
}
