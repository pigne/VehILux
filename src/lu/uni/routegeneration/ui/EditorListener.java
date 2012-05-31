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
import java.util.ArrayList;
import java.util.HashMap;
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
public class EditorListener implements ActionListener, MBoxListener {

	UIMemoryPanel uim;
	Timer timer;
	JFrame window;
	public EditorPanel editorPanel;
	int count=0;
	public MBoxStandalone mbox;
	Vector<Point2D.Double> destinations;
	
	/**
	 * @param zones
	 * @param areasFile
	 */
	public EditorListener(EditorPanel editorPanel) {
		this.editorPanel = editorPanel;
		mbox = new MBoxStandalone(this);
		window = new JFrame("Areas Editor");
		window.setPreferredSize(new Dimension(2475, 3300));
		window.setBackground(Color.white);
		window.setLayout(new BorderLayout(5, 5));
		window.getContentPane().add(editorPanel, BorderLayout.CENTER);
		uim = new UIMemoryPanel();
		window.getContentPane().add(uim, BorderLayout.EAST);
		window.pack();
		//window.setVisible(true);
		//window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * 
	 */
	public void run() {
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
