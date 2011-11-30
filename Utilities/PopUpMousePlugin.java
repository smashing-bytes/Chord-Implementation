/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Application.Node;
import DHash.IdKey;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

/**
 *
 * @author gasparosoft
 */
public class PopUpMousePlugin<V, E> extends AbstractPopupGraphMousePlugin
{

	private JPopupMenu popup;

	public PopUpMousePlugin()
	{
		popup = new JPopupMenu();
	}

	@Override
	protected void handlePopup(MouseEvent me)
	{
		popup.removeAll();

		final VisualizationViewer<V, E> visual = (VisualizationViewer<V, E>) me.getSource();

		final Layout<V, E> layout = visual.getGraphLayout();

		final Graph<V, E> graph = layout.getGraph();

		final Point2D point = me.getPoint();

		final Point2D ppoint = point;

		GraphElementAccessor<V, E> pickSupport = visual.getPickSupport();

		if (pickSupport != null)
		{
			final V vertex = pickSupport.getVertex(layout, ppoint.getX(), ppoint.getY());

			final PickedState<V> pickedVertexState = visual.getPickedVertexState();

			JMenu showInfo = null;

			if (vertex != null)
			{
				Set<V> pickedSet = pickedVertexState.getPicked();

				if (pickedSet.size() > 0)
				{
					showInfo = new JMenu("View Info");

					popup.add(showInfo);
					JMenuItem[][] itemArray = new JMenuItem[pickedSet.size()][3];
					int index = 0;
					for (final V next : pickedSet)
					{
						itemArray[index][0] = new JMenuItem("General Info - " + next);
						itemArray[index][0].addActionListener(new ActionList(next));

						itemArray[index][1] = new JMenuItem("Statistics - " + next);
						itemArray[index][1].addActionListener(new ActionList(next));

						itemArray[index][2] = new JMenuItem("Log - " + next);
						itemArray[index][2].addActionListener(new ActionList(next));


						showInfo.add(itemArray[index][0]);
						showInfo.add(itemArray[index][1]);
						showInfo.add(itemArray[index][2]);

						if (pickedSet.size() > 0 && index < pickedSet.size() - 1)
						{
							showInfo.add(new JSeparator());
						}

						index++;
					}
				}
			}
			if (popup.getComponentCount() > 0)
			{
				popup.show(visual, me.getX(), me.getY());
			}

			pickedVertexState.clear();

		}


	}

	private class ActionList implements ActionListener
	{

		private V nodeStr;

		public ActionList(V s)
		{
			nodeStr = s;
		}

		public void actionPerformed(ActionEvent e)
		{
			String temp = (String) nodeStr;
			int index;
			int delim;
			String ip = "";
			String pid = "";

			try
			{
				index = Integer.parseInt(temp);
			}
			catch (NumberFormatException nExc)
			{
				index = -1;
			}

			if (index == -1)
			{

				delim = temp.indexOf('|');
				ip = temp.substring(0, delim);
				pid = temp.substring(delim + 1);
			}
			else
			{
				IdKey key = ChordViewer.getNodeKey(index);
				ip = key.getIP();
				pid = Integer.toString(key.getPID());
			}



			if (e.getActionCommand().equals("General Info - " + nodeStr))
			{
				SwingUtilities.invokeLater(new ShowGui(0, ip, pid));
			}
			else if (e.getActionCommand().equals("Statistics - " + nodeStr))
			{
				SwingUtilities.invokeLater(new ShowGui(1, ip, pid));
			}
			else
			{
				SwingUtilities.invokeLater(new ShowGui(2, ip, pid));
			}
		}
	}

	private class ShowGui implements Runnable
	{

		private int i;
		private String ip, pid;

		ShowGui(int i, String ip, String pid)
		{
			this.i = i;
			this.ip = ip;
			this.pid = pid;
		}

		public void run()
		{
			Node node = null;
			try
			{
				node = (Node) Naming.lookup("/" + ip + ":1099/" + pid);
			}
			catch (NotBoundException ex)
			{
				Log.addMessage("PopUpMousePlugin - NBexc: Node is not bound or was unbound.", Log.ERROR);
			}
			catch (MalformedURLException ex)
			{
				Log.addMessage("PopUpMousePlugin - MURLexc: A problem occurred. Please try again.", Log.ERROR);
			}
			catch (RemoteException ex)
			{
				Log.addMessage("PopUpMousePlugin - Rexc: A problem occurred. Please try again.", Log.ERROR);
			}

			if (node == null)
				return;

			if (i == 0)
			{
				GeneralInfoGui gGui = new GeneralInfoGui(node);

			}
			else if (i == 1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Statistics", "ChordFPG said:", JOptionPane.INFORMATION_MESSAGE);

			}
			else
			{
				LogInfoGui lGui = new LogInfoGui(node);
			}

		}
	}
}
