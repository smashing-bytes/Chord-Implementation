/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Application.Node;
import DHash.IdKey;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 *
 * @author gasparosoft
 */
public class NodeViewer extends JFrame
{

	private Node node;
	private VisualizationViewer<String, String> visual;
	private Graph<String, String> graph;
	//private final DefaultModalGraphMouse<String, String> gm;
	private  GraphMouse<String,String> gm;
	private Layout<String, String> layout;

	public NodeViewer(Node node) throws RemoteException
	{

		super("Node Viewer - " + node.getLocalID().toString());

		this.node = node;

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Utilities/resources/logo2.gif")));

		setSize(new Dimension(600, 600));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		JMenuItem saveAsItem = new JMenuItem("Save As...");
		saveAsItem.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);


				int option = fileChooser.showSaveDialog(NodeViewer.this.rootPane);

				if (option == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();

					NodeViewer.this.writeImage(new File(file.getAbsolutePath() + ".jpg"));

				}
			}
		});

		fileMenu.add(saveAsItem);
		fileMenu.addSeparator();

		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				NodeViewer.this.dispose();
			}
		});

		fileMenu.add(closeItem);

		menuBar.add(fileMenu);

		// gm = new DefaultModalGraphMouse<String, String>();
		gm = new GraphMouse<String,String>();
		createGraph();

		Container content = getContentPane();
		GraphZoomScrollPane zoom = new GraphZoomScrollPane(visual);
		content.add(zoom);


		JComboBox modeBox = gm.getModeComboBox();
		modeBox.addItemListener(gm.getModeListener());
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);

		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				scaler.scale(visual, 1.1f, visual.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				scaler.scale(visual, 1 / 1.1f, visual.getCenter());
			}
		});

		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				try
				{
					update();
				}
				catch (RemoteException ex)
				{
					Logger.getLogger(NodeViewer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		JPanel controls = new JPanel();
		JPanel zoomControls = new JPanel(new GridLayout(2, 1));
		JPanel buttonControl = new JPanel(new GridLayout(1, 1));

		buttonControl.setBorder(BorderFactory.createTitledBorder("Update Task"));
		buttonControl.add(updateButton);

		zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
		zoomControls.add(plus);
		zoomControls.add(minus);

		controls.add(zoomControls);
		controls.add(modeBox);
		controls.add(buttonControl);

		content.add(controls, BorderLayout.SOUTH);

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

	private void createGraph() throws RemoteException
	{
		if (layout != null)
		{
			layout.reset();
		}
		if (visual != null)
		{
			visual.removeAll();
		}

		graph = new DirectedOrderedSparseMultigraph<String, String>();

		layout = new FRLayout<String, String>(graph);

		Dimension preferredSize = new Dimension(400, 400);

		final VisualizationModel<String, String> visualizationModel = new DefaultVisualizationModel<String, String>(layout, preferredSize);
		visual = new VisualizationViewer<String, String>(visualizationModel, preferredSize);



		// customize the render context
		visual.getRenderContext().setVertexLabelTransformer(MapTransformer.<String, String>getInstance(
		            LazyMap.<String, String>decorate(new HashMap<String, String>(), new ToStringLabeller<String>())));

		visual.getRenderContext().setEdgeLabelTransformer(MapTransformer.<String, String>getInstance(
		            LazyMap.<String, String>decorate(new HashMap<String, String>(), new ToStringLabeller<String>())));

		visual.setBackground(Color.white);

		// add a listener for ToolTips
		visual.setVertexToolTipTransformer(new ToStringLabeller<String>());



		visual.setGraphMouse(gm);

		visual.addKeyListener(gm.getModeKeyListener());

		visual.setVertexToolTipTransformer(visual.getRenderContext().getVertexLabelTransformer());

		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>()
		{

			public Paint transform(String str)
			{
				try
				{
					if (str.equals(node.getLocalID().toString()))
					{
						return Color.BLUE;
					}
				}
				catch (RemoteException ex)
				{
					Log.addMessage(ex.getMessage(), Log.ERROR);
				}
				return Color.YELLOW;
			}
		};

		visual.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		createNodes(false);
	}

	private void update() throws RemoteException
	{


		createNodes(true);
	}

	private void createNodes(boolean remove) throws RemoteException
	{

		//-----------------------------------------------------------------------
		Map<String, String> map = new HashMap<String, String>();
		Transformer<String, String> trans;
		IdKey[] fingers = node.getFingers();
		IdKey pred = node.getPredecessor();
		IdKey suc = node.getImmediateSuccessor();

		List<String> listVertices = new ArrayList<String>();
		List<String> listFingers = new ArrayList<String>();
		listVertices.add(node.getLocalID().toString());

		map.put(node.getLocalID().toString(), node.getLocalID().hashKeytoHexString());

		visual.getRenderContext().getPickedVertexState().clear();
		visual.getRenderContext().getPickedEdgeState().clear();



		if (suc == null && pred == null)
		{
			return;
		}

		if (suc != null)
		{
			listVertices.add(suc.toString());
			map.put(suc.toString(), suc.hashKeytoHexString());
		}
		if (fingers != null)
		{
			for (int i = 0; i < fingers.length; i++)
			{

				if (fingers[i] != null)
				{
					listFingers.add(fingers[i].toString());
					map.put(fingers[i].toString(), fingers[i].hashKeytoHexString());
				}
			}
		}

		if (pred != null)
		{
			listVertices.add(pred.toString());
			map.put(pred.toString(), pred.hashKeytoHexString());
		}

		if (remove)
		{
			graph.removeEdge("Suc");
			graph.removeEdge("Pred");

			if (fingers != null)
			{
				for (int i = 0; i < fingers.length; i++)
				{

					if (fingers[i] != null)
					{
						graph.removeEdge("Fin[" + i + "]");
					}
				}
			}
			for (int i = 0; i < listVertices.size(); i++)
			{
				String n = listVertices.get(i);
				graph.removeVertex(n);
			}
		}

		for (int i = 0; i < listVertices.size(); i++)
		{
			String n = listVertices.get(i);
			graph.addVertex(n);
			visual.getRenderContext().getPickedVertexState().pick(n, true);
		}

		if (suc != null)
		{

			graph.addEdge("Suc", listVertices.get(0), listVertices.get(1), EdgeType.DIRECTED);
			visual.getRenderContext().getPickedEdgeState().pick("Suc", true);

			if (fingers != null)
			{
				for (int i = 0; i < fingers.length; i++)
				{
					if (fingers[i] != null)
					{
						visual.getRenderContext().getPickedEdgeState().pick("Fin[" + i + "]", true);
						graph.addEdge("Fin[" + i + "]", listVertices.get(0), listFingers.get(i), EdgeType.DIRECTED);
					}
				}
			}
		}

		if (pred != null)
		{
			visual.getRenderContext().getPickedEdgeState().pick("Pred", true);
			graph.addEdge("Pred", listVertices.get(0), pred.toString(), EdgeType.DIRECTED);

		}

		trans = TransformerUtils.mapTransformer(map);
		visual.setVertexToolTipTransformer(trans);

		visual.repaint();

	}
}

