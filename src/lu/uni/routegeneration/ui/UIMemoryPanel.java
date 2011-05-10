/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file UIMemoryPanel.java
 * @date Nov 9, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * 
 */
public class UIMemoryPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public int[] data = {};
	public int dataPointer = 0;
	public int tailleData = 100;
	public int dimensionX = 120;
	public int dimensionY = 70;

	public int maxMemory;
	// public JLabel lmax;
	// public JLabel lmin;

	public int initialMinMemory;
	public int initialMaxMemory;

	public int tailleRect;
	public int nbRect;

	public UIMemoryPanel(){
		init();
	}

	public void init(){
		setOpaque(true);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));
		setBackground(Color.white);
		setForeground(Color.blue);
		setPreferredSize(new Dimension(dimensionX, dimensionY + 65));
		setDoubleBuffered(true);
		setFont(new Font("Vernada", Font.PLAIN, 9));

		// lmax= new JLabel("");
		// lmax.setFont(
		data = new int[tailleData];
		for (int i = 0; i < tailleData; i++) {
			data[i] = 0;
		}
		initialMinMemory = Integer.MAX_VALUE;
		initialMaxMemory = 0;

		maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// System.out.println("maxMemory=" + maxMemory);

		tailleRect = 8;
		nbRect = (int) ((dimensionX - 4) / tailleRect);
		tailleRect = (int) Math.round((dimensionX - 4) / (double) nbRect);
		// System.out.println("nbRect = "+nbRect+" tailleRect = "+tailleRect);
	}
	public void paintComponent(Graphics g) {
		
		
update();
		
		
		// Graphics mg = getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		super.paintComponent(g);
		int minMem = initialMinMemory;
		int maxMem = initialMaxMemory;
		// recherhce des bornes hautes et basses du graphe.
		for (int i = 0; i < tailleData; i++) {
			// System.out.print(data[i]+",");
			if (minMem > data[i]) {
				minMem = data[i];
			}
			if (maxMem < data[i]) {
				maxMem = data[i];
			}
		}

		g2.setColor(new Color(150, 70, 90));
		// System.out.println(" ->Min:"+minMem+" Max: "+maxMem);
		int i;
		for (int k = 0; k < tailleData - 1; k++) {
			i = (dataPointer + k) % tailleData;
			int x1 = (int) (dimensionX * (k) / tailleData);
			int y1 = dimensionY
					+ 10
					- (int) ((double) ((double) dimensionY / (double) (maxMem - minMem)) * (double) (data[i] - minMem));
			int x2 = (int) (dimensionX * (k + 1) / tailleData);
			// System.out.println("x2="+x2);
			int y2 = dimensionY
					+ 10
					- (int) ((double) ((double) dimensionY / (double) (maxMem - minMem)) * (double) (data[(i + 1)
							% tailleData] - minMem));
			// System.out.print("("+x1+" "+y1+" "+x2+" "+y2+") ");
			g2.drawLine(x1, y1, x2, y2);
		}
		g2.setColor(Color.black);
		g2.drawString(String.format(new Locale("US"), "%d", maxMem), 5, 10);
		g2.drawString(Integer.toString(minMem), 5, dimensionY + 20);

		// une ligne horizontale
		g2.drawLine(0, dimensionY + 25, dimensionX, dimensionY + 25);

		// 0 Mo --- 63.34 Mo
		g2.drawString("0 Mo", 5, dimensionY + 40);
		String mo64 = String.format(getLocale(), "%d Mo", (maxMemory / 1024));
		g2.drawString(mo64, dimensionX - 30, dimensionY + 40);

		// rectangles
		// System.out.println("nbRect="+nbRect);
		// System.out.println("truc="+data[(dataPointer-1+tailleData)%tailleData]
		// /(double)maxMemory);
		int nbRectToDraw = (int) Math
				.ceil(nbRect
						* ((double) (data[(dataPointer - 1 + tailleData)
								% tailleData] / (double) maxMemory)));
		// System.out.println("nbRectToDraw="+nbRectToDraw);
		g2.setColor(new Color(150, 170, 190));
		for (i = 0; i < nbRectToDraw; i++) {
			g2.fillRoundRect(3 + (i * tailleRect), dimensionY + 46,
					tailleRect - 3, tailleRect + 2, 4, 4);
			g2.setColor(new Color(50, 70, 90));
			g2.drawRoundRect(3 + (i * tailleRect), dimensionY + 46,
					tailleRect - 3, tailleRect + 2, 4, 4);
			g2.setColor(new Color(150, 170, 190));
			
		}
		g2.setColor(new Color(50, 70, 90));
		for (; i < nbRect; i++) {
			g2.drawRoundRect(3 + (i * tailleRect), dimensionY + 46,
					tailleRect - 3, tailleRect + 2, 4, 4);
		}
		// System.out.println();
	}

	/**
	 * 
	 */
	private void update() {
		long mem= (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
			.freeMemory()) / 1024);
	//memoire.setText(	mem+ " kB");
	data[dataPointer]=(int) mem;
	dataPointer = (dataPointer +1)%tailleData;
		
	}
}
