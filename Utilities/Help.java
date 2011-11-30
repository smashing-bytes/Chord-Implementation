/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author mariuska
 */
public class Help extends JFrame
{

	/** Creates new form HelpContents */
	public Help()
	{

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("ChordFPG 1.0 - Help");
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Utilities/resources/logo2.gif")));


		String text;

		text = "<html>"
		       + "<h3> Stats(Tab)</h3>"
		       + "<ul>"
		       + "<li>Information about the node of ChordFPG(successor, predecessor, IdKey)</li>"
		       + "<li>Warnings</li><li>Information about fingers and successor lists of the node</li>"
		       + "<li>Errors that occur while ChordFPG is running</li>"
		       + "</ul>"
		       + "<h3> CLI(Tab)</h3>"
		       + "<ul>"
		       + "<li>Console that the user can use in order to interact with ChordFPG</li>"
		       + "<ul>"
		       + "<li> To be reviewed </li>"
		       + "</ul>"
		       + "</ul>"
		       + "<h3> SaveAs</h3>"
		       + "<ul>"
		       + "<li>If in the Stats tab, this command saves the information projected.</li>"
		       + "<li>If in CLI tab, this command saves the terminal.</li>"
		       + "<li>(Both are saved in .txt files)</li>"
		       + "</ul>"
		       + "<h3> NodeViewer</h3>"
		       + "<ul>"
		       + "<li>Creates a new window with a graphical design of the nodes.</li>"
		       + "<li>The current node is associated with(successor, predecessor, fingers)</li>"
		       + "</ul>"
		       + "<h3> ChordViewer </h3>"
		       + "<ul>"
		       + "<li> Creates a new window with a graphical design of the chord ring at that time</li>"
		       + "</ul>"
		       + "<h3> Exit </h3>"
		       + "<ul>"
		       + "<li>Disconnects the node from the chord ring and then terminates ChordFPG</li>"
		       + "</ul>"
		       + "<h3> Help</h3>"
		       + "<ul>"
		       + "<li>Creates this window.</li>"
		       + "</ul>"
		       + "<h3> About </h3>"
		       + "<ul>"
		       + "<li>Creates a new window with the data of the developers and the purpose of ChordFPG</li>"
		       + "</ul>"
		       + "</html>";


		JPanel panel = new JPanel();
		JLabel label = new JLabel(text);

		panel.setSize(600, 550);
		panel.add(label);
		JScrollPane scrollPane = new JScrollPane(panel);
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		setSize(620, 600);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width / 2) - (this.getWidth() / 2);
		int y = (screen.height / 2) - (this.getHeight() / 2);
		this.setLocation(x, y);
		this.setVisible(true);
	}
}
