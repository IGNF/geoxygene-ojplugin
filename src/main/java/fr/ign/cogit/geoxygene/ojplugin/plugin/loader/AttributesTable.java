/*
 * This file is part of the GeOxygene-plugin project for OpenJump.
 * 
 *  GeOxygene aims at providing an open framework which implements
 *  OGC/ISO specifications for the development and deployment of
 *  geographic(GIS) applications. It is a open source contribution
 *  of the COGIT laboratory at the Institut Géographique National
 *  (the French National Mapping Agency).
 * 
 *  See: http://oxygene-project.sourceforge.net
 *
 *  Copyright (C) 2009 Institut Géographique National
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 *  For more information, contact:
 *
 *  IGN
 *  Laboratoire COGIT - GeOxygene
 *  73 avenue de Paris
 *  94165 SAINT-MANDE Cedex
 *  France
 */
package fr.ign.cogit.geoxygene.ojplugin.plugin.loader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/** 
 * @author grosso - IGN / Laboratoire COGIT
 * 1 juin 2005
 */

public class AttributesTable extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1886357441643122417L;
	public Object [][] dataNomsAttrib;
	public Object [][] dataTypesAttrib;

	private JPanel jPanelNomAtt = null;
	private JPanel jPanelTypeAtt = null;
	private JTable jTableNomAtt = null;
	private JTable jTableTypeAtt = null;
	private JTextField jtfAtt = null;

	public Object [][] dataNomsMethod;
	public Object [][] dataTypesMethod;
	private JPanel jPanelNomMethod = null;
	private JPanel jPanelTypeMethod = null;
	private JTable jTableNomMethod = null;
	private JTable jTableTypeMethod = null;
	private JTextField jtfMethod = null;

	private JButton jButtonOK;

	private String nomPop;

	/**
	 * This is the default constructor
	 */
	public AttributesTable(String [][] dataNomsAttributs,String [][] dataTypesAttributs,
			String [][] dataNomsMethodes,String [][] dataTypesMethodes, String nomPop) {
		super();
		this.dataNomsAttrib = dataNomsAttributs;
		this.dataTypesAttrib = dataTypesAttributs;
		this.dataNomsMethod = dataNomsMethodes;
		this.dataTypesMethod = dataTypesMethodes;
		this.nomPop = nomPop;
		initialize();
	}

	/** This method initializes this
	 */
	protected void initialize(){
		this.setSize(new Dimension(800,400));

		//ATTRIBUTS
		//nom de la table
		jtfAtt = new JTextField("Attributs");
		jtfAtt.setFont(new Font("True Type",Font.BOLD,16));
		jtfAtt.setEditable(false);
		jtfAtt.setBackground(Color.white);
		jtfAtt.setForeground(new Color(107,101,201));
		jtfAtt.setMinimumSize(new Dimension(100,40));
		//entête des noms des attributs
		String [] columnsNomAtt = {"Nom"};
		jTableNomAtt = new JTable(dataNomsAttrib,columnsNomAtt);
		jTableNomAtt.setBackground(new Color(250,250,255));
		jTableNomAtt.getColumnModel().getColumn(0).setPreferredWidth(200);
		jTableNomAtt.setEnabled(false);

		String [] columnsTypeAtt = {"Type"};
		jTableTypeAtt = new JTable(dataTypesAttrib,columnsTypeAtt);
		jTableTypeAtt.setBackground(new Color(250,250,255));
		jTableTypeAtt.getColumnModel().getColumn(0).setPreferredWidth(500);
		jTableTypeAtt.setEnabled(false);

		jPanelNomAtt = new JPanel(new BorderLayout());
		// ajoute les entetes au nord du conteneur
		jPanelNomAtt.add(jTableNomAtt.getTableHeader(),BorderLayout.NORTH);
		jPanelNomAtt.add(jTableNomAtt, BorderLayout.CENTER);
		jPanelNomAtt.setBackground(new Color(250,250,255));

		jPanelTypeAtt = new JPanel(new BorderLayout());
		// ajoute les entetes au nord du conteneur
		jPanelTypeAtt.add(jTableTypeAtt.getTableHeader(),BorderLayout.NORTH);
		jPanelTypeAtt.add(jTableTypeAtt, BorderLayout.CENTER);
		jPanelTypeAtt.setBackground(new Color(250,250,255));

		//METHODES
		//nom de la table
		jtfMethod = new JTextField("Méthodes");
		jtfMethod.setFont(new Font("True Type",Font.BOLD,16));
		jtfMethod.setEditable(false);
		jtfMethod.setBackground(Color.white);
		jtfMethod.setForeground(new Color(107,101,201));
		jtfMethod.setMinimumSize(new Dimension(100,40));
		//entête des noms des attributs
		String [] columnsNomMethod = {"Nom"};
		jTableNomMethod = new JTable(dataNomsMethod,columnsNomMethod);
		jTableNomMethod.setBackground(new Color(250,250,255));
		jTableNomMethod.getColumnModel().getColumn(0).setPreferredWidth(200);
		jTableNomMethod.setEnabled(false);

		String [] columnsTypeMethod = {"Type"};
		jTableTypeMethod = new JTable(dataTypesMethod,columnsTypeMethod);
		jTableTypeMethod.setBackground(new Color(250,250,255));
		jTableTypeMethod.getColumnModel().getColumn(0).setPreferredWidth(500);
		jTableTypeMethod.setEnabled(false);

		jPanelNomMethod = new JPanel(new BorderLayout());
		// ajoute les entetes au nord du conteneur
		jPanelNomMethod.add(jTableNomMethod.getTableHeader (),BorderLayout.NORTH);
		jPanelNomMethod.add(jTableNomMethod, BorderLayout.CENTER);
		jPanelNomMethod.setBackground(new Color(250,250,255));

		jPanelTypeMethod = new JPanel(new BorderLayout());
		// ajoute les entetes au nord du conteneur
		jPanelTypeMethod.add(jTableTypeMethod.getTableHeader (),BorderLayout.NORTH);
		jPanelTypeMethod.add(jTableTypeMethod, BorderLayout.CENTER);
		jPanelTypeMethod.setBackground(new Color(250,250,255));

		Box boxAtt = Box.createHorizontalBox();
		boxAtt.add(jPanelNomAtt);
		boxAtt.add(jPanelTypeAtt);
		Box boxMethod = Box.createHorizontalBox();
		boxMethod.add(jPanelNomMethod);
		boxMethod.add(jPanelTypeMethod);

		Box box = Box.createVerticalBox();
		box.add(jtfAtt);
		box.add(boxAtt);
		box.add(Box.createHorizontalStrut(10));
		box.add(jtfMethod);
		box.add(boxMethod);
		box.add(getJButtonOK());

		this.setContentPane(box);
		this.setBackground(Color.white);
		this.setTitle("Attributs et Méthodes de ".concat(nomPop));
		this.pack();

		//taille de l'écran et centrage
		Dimension tailleEcran = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((tailleEcran.width-this.getWidth())/2,
				(tailleEcran.height-this.getHeight())/2);

	}

	public void refresh() {
		for (int i = 0; i<dataTypesAttrib.length;i++) {
			jTableNomAtt.setValueAt(dataNomsAttrib[i][0],i,0);
			jTableTypeAtt.setValueAt(dataTypesAttrib[i][0],i,0);
		}
		for (int i = 0; i<dataTypesMethod.length;i++) {
			jTableNomMethod.setValueAt(dataNomsMethod[i][0],i,0);
			jTableTypeMethod.setValueAt(dataTypesMethod[i][0],i,0);
		}
	}

	public void kill(){
		this.dispose();
		//this.finalize();
	}

	/** initialisation du bouton OK
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButtonOK() {
		jButtonOK = new JButton();
		jButtonOK.setPreferredSize(new Dimension(80,30));
		jButtonOK.setText("OK");

		jButtonOK.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		return jButtonOK;
	}

}