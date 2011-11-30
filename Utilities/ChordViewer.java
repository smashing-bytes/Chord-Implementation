package Utilities;

import Application.Node;
import DHash.BigInt;
import DHash.IdKey;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 *
 * @author gasparosoft
 */
public class ChordViewer extends JFrame
{

	private Node node;
	private VisualizationViewer<String, String> visual;

	private static List<IdKey> nodesKey = new ArrayList<IdKey>();

	public ChordViewer(Node node) throws RemoteException
	{


		super("Chord Ring Viewer - " + node.getLocalID().toString());

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Utilities/resources/logo2.gif")));
		setSize(new Dimension(600, 600));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		JMenuItem saveAsItem = new JMenuItem("Save As...");
		saveAsItem.addActionListener( new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);


				int option = fileChooser.showSaveDialog(ChordViewer.this.rootPane);

				if (option == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();

					ChordViewer.this.writeImage(new File(file.getAbsolutePath()+".jpg"));

				}
			}
		});

		fileMenu.add(saveAsItem);
		fileMenu.addSeparator();

		JMenuItem closeItem  = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				ChordViewer.this.dispose();
			}
		});

		fileMenu.add(closeItem);

		menuBar.add(fileMenu);


		this.node = node;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width / 2) - (this.getWidth() / 2);
		int y = (screen.height / 2) - (this.getHeight() / 2);
		this.setLocation(x,y);
		this.setVisible(true);

	}

	private void writeImage(File file)
	{
		int width = visual.getWidth();
		int height = visual.getHeight();

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D graphics2D = bufferedImage.createGraphics();
		visual.paint(graphics2D);
		graphics2D.dispose();
		String s = file.getName();

		try
		{
			ImageIO.write(bufferedImage, "jpeg", file);
		}
		catch (Exception e)
		{

		}
	}

	public void create()
	{
		Graph<String, String> graph = new DirectedSparseGraph<String, String>();
		List<String> list;
		Map<String, Integer> map = new TreeMap<String, Integer>();
		IdKey key;
		Node nd;
		nd = node;
		CircleLayout layout;
		Transformer<String, String> tran;


		int i = 0;
		try
		{

			BigInt bg = nd.getLocalID().getHashKey().powerOfTwo(0);
			IdKey id = new IdKey(bg, 0, "low");

			id = nd.find_successor_ID(id);
			try
			{
				// node = (Node) nd.getRMIHandle().lookup(id.toString());
				node = (Node) Naming.lookup("/" + id.getIP() + ":1099/" + String.valueOf(id.getPID()));
			}
			catch (MalformedURLException ex)
			{
				JOptionPane.showMessageDialog(this,"A problem occurred while creating the graph. Please try again later." , "ChordFPG said:" , JOptionPane.ERROR_MESSAGE);
				this.dispose();
			}
			key = id;

			while (true)
			{
				nodesKey.add(key);
				map.put(key.hashKeytoHexString(), i);
				graph.addVertex(Integer.toString(i));
				i++;

				key = node.getImmediateSuccessor();
				try
				{
					// node = (Node) nd.getRMIHandle().lookup(id.toString());
					node = (Node) Naming.lookup("/" + key.getIP() + ":1099/" + String.valueOf(key.getPID()));
				}
				catch (MalformedURLException ex)
				{
					JOptionPane.showMessageDialog(this,"A problem occurred while creating the graph. Please try again later." , "ChordFPG said:" , JOptionPane.ERROR_MESSAGE);
					this.dispose();
				}

				if (key.equals(id))
				{
					break;
				}
			}
		}
		catch (RemoteException re)
		{
			JOptionPane.showMessageDialog(this,"A problem occurred while creating the graph. Please try again later." , "ChordFPG said:" , JOptionPane.ERROR_MESSAGE);
			this.dispose();
		}
		catch (NotBoundException nb)
		{
			JOptionPane.showMessageDialog(this,"A problem occurred while creating the graph. Please try again later." , "ChordFPG said:" , JOptionPane.ERROR_MESSAGE);
			this.dispose();
		}

		list = new ArrayList<String>();
		for (int j = 0; j < i; j++)
		{
			list.add(Integer.toString(j));
		}

		for (int j = 0; j < list.size() - 1; j++)
		{
			graph.addEdge("Successor[" + j + "]", Integer.toString(j), Integer.toString(j + 1), EdgeType.DIRECTED);
		}


		graph.addEdge("Successor[" + (list.size() - 1) + "]", Integer.toString(list.size() - 1), Integer.toString(0), EdgeType.DIRECTED);


		layout = new CircleLayout(graph);
		layout.setVertexOrder(list);
		layout.setSize(new Dimension(550, 550));

		Map<String, String> nMap = new HashMap<String, String>();
		Set<String> set = map.keySet();
		Object[] array = set.toArray();

		for (i = 0; i < map.size(); i++)
		{
			nMap.put(Integer.toString(i), (String) array[i]);
		}

		tran = TransformerUtils.mapTransformer(nMap);

		visual = new VisualizationViewer<String, String>(layout);
		visual.setBackground(Color.white);

		layout.setVertexOrder(list);

		visual.setSize(new Dimension(400, 400));
		visual.getRenderContext().setVertexLabelTransformer(MapTransformer.<String, String>getInstance(
		            LazyMap.<String, String>decorate(new HashMap<String, String>(), new ToStringLabeller<String>())));

		visual.getRenderContext().setEdgeLabelTransformer(MapTransformer.<String, String>getInstance(
		            LazyMap.<String, String>decorate(new HashMap<String, String>(), new ToStringLabeller<String>())));

		visual.setVertexToolTipTransformer(tran);

		//DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		GraphMouse<String,String> gm = new GraphMouse<String,String>();
		gm.setMode(ModalGraphMouse.Mode.PICKING);

		visual.setGraphMouse(gm);

		this.getContentPane().add(visual);

		visual.repaint();

	}

	public static IdKey getNodeKey(int index)
	{
		return nodesKey.get(index);
	}
}