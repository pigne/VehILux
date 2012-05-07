/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file AreasEditor.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.Timer;

import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Zone;

import org.miv.mbox.MBoxListener;
import org.miv.mbox.MBoxStandalone;

/**
 * 
 */
public class AreasEditor implements ActionListener, MBoxListener {

	RouteGeneration rg;

	double min_x_boundary = Double.MAX_VALUE;
	double min_y_boundary = Double.MAX_VALUE;
	double max_x_boundary = Double.MIN_VALUE;
	double max_y_boundary = Double.MIN_VALUE;

	Vector<Point2D.Double> destinations;
	UIMemoryPanel uim;
	Timer timer;
	JFrame window;
	public EditorPanel editorPanel;
	int count=0;

	public MBoxStandalone mbox;

	/**
	 * @param zones
	 * @param areasFile
	 */
	public AreasEditor(RouteGeneration rg) {
		this.rg = rg;
		destinations = new Vector<Point2D.Double>();
		mbox = new MBoxStandalone(this);
	}

	/**
	 * 
	 */
	public void run() {

		for (Zone z : rg.getZones().values()) {
			if (z.min_x_boundary < min_x_boundary)
				min_x_boundary = z.min_x_boundary;
			if (z.max_x_boundary > max_x_boundary)
				max_x_boundary = z.max_x_boundary;
			if (z.min_y_boundary < min_y_boundary)
				min_y_boundary = z.min_y_boundary;
			if (z.max_y_boundary > max_y_boundary)
				max_y_boundary = z.max_y_boundary;
		}

		editorPanel = new EditorPanel(this);
		editorPanel.setBackground(Color.white);
		window = new JFrame("Areas Editor");
		window.setPreferredSize(new Dimension(2475, 3300));
		window.setBackground(Color.white);
		window.setLayout(new BorderLayout(5, 5));

		window.getContentPane().add(editorPanel, BorderLayout.CENTER);
		uim = new UIMemoryPanel();
		window.getContentPane().add(uim, BorderLayout.EAST);
		
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		timer = new Timer(300, this);
		timer.setCoalesce(true);
		timer.setDelay(300);
		timer.setRepeats(true);
		timer.start();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (count++%10==0){
			mbox.processMessages();
			window.repaint();
		}
		uim.repaint();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.miv.mbox.MBoxListener#processMessage(java.lang.String,
	 * java.lang.Object[])
	 */
	public void processMessage(String from, Object[] data) {
		// TODO Auto-generated method stub
		destinations.add((Point2D.Double) data[0]);
	}
}
