/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;

/**
 *
 * @author gasparosoft
 */
public class GraphMouse<V, E> extends DefaultModalGraphMouse
{

	private PopUpMousePlugin<V, E> popupMousePlugin;


	public GraphMouse()
	{
		this(1.1f, 1 / 1.1f);
	}

	public GraphMouse(float in, float out)
	{
		super(in, out);
		loadPlugins();
	}

	@Override
	protected void loadPlugins()
	{

		pickingPlugin = new PickingGraphMousePlugin<V,E>();
		animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V,E>();
		translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
		rotatingPlugin = new RotatingGraphMousePlugin();
		shearingPlugin = new ShearingGraphMousePlugin();

		popupMousePlugin = new PopUpMousePlugin<V, E>();
	}

	@Override
	public void setMode(Mode mode)
	{
		if (this.mode != mode)
		{
			fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
			                                   this.mode, ItemEvent.DESELECTED));
			this.mode = mode;

			if (mode == Mode.PICKING)
			{
				setPickingMode();
			}
			else if (mode == Mode.TRANSFORMING)
			{
				setTransformingMode();

			}
			if (modeBox != null)
			{
				modeBox.setSelectedItem(mode);
			}
			fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED));
		}
	}

	@Override
	public void setPickingMode()
	{
		remove(translatingPlugin);
		remove(rotatingPlugin);
		remove(shearingPlugin);
		add(pickingPlugin);
		add(animatedPickingPlugin);
		add(popupMousePlugin);
	}

	@Override
	public void setTransformingMode()
	{
		remove(pickingPlugin);
		remove(animatedPickingPlugin);
		add(translatingPlugin);
		add(rotatingPlugin);
		add(shearingPlugin);
		add(popupMousePlugin);
	}

	@Override
	public JComboBox getModeComboBox()
	{
		if (modeBox == null)
		{
			modeBox = new JComboBox(new Mode[] {Mode.TRANSFORMING, Mode.PICKING});
			modeBox.addItemListener(getModeListener());
		}
		modeBox.setSelectedItem(mode);
		return modeBox;
	}


	public static class ModeKeyAdapter extends KeyAdapter
	{
		private char t = 't';
		private char p = 'p';
		protected ModalGraphMouse graphMouse;

		public ModeKeyAdapter(ModalGraphMouse graphMouse)
		{
			this.graphMouse = graphMouse;
		}

		public ModeKeyAdapter(char t, char p, ModalGraphMouse graphMouse)
		{
			this.t = t;
			this.p = p;
			this.graphMouse = graphMouse;
		}

		@Override
		public void keyTyped(KeyEvent event)
		{
			char keyChar = event.getKeyChar();
			if(keyChar == t)
			{
				((Component)event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				graphMouse.setMode(Mode.TRANSFORMING);
			}
			else if(keyChar == p)
			{
				((Component)event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				graphMouse.setMode(Mode.PICKING);
			}
		}
	}


	/**
	 * @return the popupEditingPlugin
	 */
	public PopUpMousePlugin<V, E> getPopupEditingPlugin()
	{
		return popupMousePlugin;
	}
}
