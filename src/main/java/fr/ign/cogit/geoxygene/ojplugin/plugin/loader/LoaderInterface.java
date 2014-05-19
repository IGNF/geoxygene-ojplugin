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

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.DataSet;


/** 
 * @author Grosso - IGN / Laboratoire COGIT
 * Créé le 10 nov. 2004
 */
public class LoaderInterface extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8572059707216667403L;
	private JPanel jContentPane, jPanel;
	private JButton jButton;
	private JScrollPane jScroll;

	private PlugInContext contextApp = null;
	private List<?> hierarchy = null, infosData = null;
	private int cogitIDDS = -1;

	/** Constructeur par défaut
	 * @throws ClassNotFoundException
	 */
	public LoaderInterface() throws ClassNotFoundException {
		super();
		hierarchy = LoaderMethods.creationJTreeDataSet();
		initialize();
		System.gc();
	}

	/** Constructeur dans le cadre de Jump
	 */
	public LoaderInterface(PlugInContext context)throws ClassNotFoundException {
		super();
		contextApp = context;
		hierarchy = LoaderMethods.creationJTreeDataSet();
		initialize();
		System.gc();
	}

	/** This method initializes this
	 */
	private void initialize() {
		this.setSize(new Dimension(600,400));
		//taille de l'écran et centrage
		Dimension tailleEcran = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((tailleEcran.width-this.getWidth())/2,
				(tailleEcran.height-this.getHeight())/2);
		this.setContentPane(getJContentPane());
		this.setBackground(java.awt.Color.black);
		this.setTitle("Sélection de bases de données");
		this.setVisible(true);
		this.setForeground(java.awt.Color.darkGray);
	}

	/** This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		jContentPane.add(getJPanel(), BorderLayout.SOUTH);
		jContentPane.setBackground(new Color(250,250,255));
		return jContentPane;
	}

	/** initialisation de jScroll
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		jScroll = new JScrollPane(getJTree());
		JScrollBar jScrollBar = new JScrollBar(Adjustable.VERTICAL);
		jScrollBar.setBackground(new Color(250,250,255));
		jScrollBar.setForeground(new Color(107,101,201));
		jScroll.setVerticalScrollBar(jScrollBar);
		jScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return jScroll;
	}

	/** initialisation de jTree
	 * @return javax.swing.JTree
	 */
	private JTree getJTree() {
		final JTree arbreBD = (JTree)hierarchy.get(0);

		arbreBD.setBackground(new Color(250,250,255));
		JTreeRenderer jTreeRend = new JTreeRenderer();
		arbreBD.setCellRenderer(jTreeRend);
		arbreBD.setVisible(true);
		arbreBD.setToggleClickCount(1);
		arbreBD.scrollPathToVisible(arbreBD.getSelectionPath());

		arbreBD.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		arbreBD.addTreeSelectionListener(new TreeSelectionListener(){
			@Override
            public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)arbreBD.getLastSelectedPathComponent();
				if (node != null){
					infosData = nodeToString(node);
				}
				System.gc();
			}
		});
		return arbreBD;
	}

	/** retourne la liste pour accéder au noeud Sélectionné
	 * exemple: [Base de données, BDCarto, Administratif, Arrondissement]
	 * @param node noeud
	 * @return liste de chaînes de caractère représentant le noeud
	 */
	private List<String> nodeToString (DefaultMutableTreeNode node){
		List<String> listName = new ArrayList<String>();
		listName.add(node.toString());
		while (node.getParent() != null){
			node = (DefaultMutableTreeNode)node.getParent();
			listName.add(0,node.toString());
		}
		return listName;
	}

	/** initialisation de jPanel
	 * @return JPanel
	 */
	private JPanel getJPanel() {
		jPanel = new JPanel();
		jPanel.setLayout(new FlowLayout());
		jPanel.add(getJButtonLoad());
		jPanel.add(getJButtonAttribute());
		jPanel.add(getJButtonQuitter());
		jPanel.setBackground(new Color(250,250,255));
		return jPanel;
	}

	/** initialisation de jButtonLoad
	 * @return JButton
	 */
	private JButton getJButtonLoad() {
		jButton = new JButton();
		jButton.setPreferredSize(new Dimension(120,30));
		jButton.setText("Chargement");
		jButton.setAlignmentX(30);
		jButton.setAlignmentY(30);

		jButton.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
				int oKCancel;
				String message,dsUsed,nomDS;
				IFeatureCollection<IFeature> ftfc;
				Color couleur;
				//contient: cogitIDDS + nom thème (+nom population si demande de chargement de population)

				if (infosData.size() <= 2){
					message = "Veuillez Sélectionner un thème ou une population";
					JOptionPane.showMessageDialog(null,message,"Sélection erronée",JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					dsUsed = (String)infosData.get(1);
					//si la table est une table du DS "Autres" i.e appartenant à la table des géométries
					//mais pas des populations
					if (dsUsed.equals("Autres")){
						message = "Etes-vous sur de vouloir charger les données provenant" +
						" de la table ".concat((String)infosData.get(2)).concat(" ?");
						oKCancel = JOptionPane.showConfirmDialog(null,message,"Confirmation " +
								"du chargement",JOptionPane.OK_CANCEL_OPTION);
						if (oKCancel == 0){
							couleur = JColorChooser.showDialog(new ColorChooser(),"Choix de couleur",Color.blue);
							if (couleur == null)couleur = Color.blue;
							ftfc = DataSet.db.loadAllFeatures((DataSet.db.getMetadata((String)infosData.get(2))).getJavaClass());
							UtilJump.afficheCollection(contextApp,ftfc,"Autres",(String)infosData.get(2),couleur);
						}
					}
					else{
						for (int i=0;i<((List<?>)hierarchy.get(1)).size();i++){
							nomDS = (String)((List<?>)hierarchy.get(1)).get(i);
							if (dsUsed.equals(nomDS)){
								cogitIDDS = ((Integer)((List<?>)hierarchy.get(2)).get(i)).intValue();

								if (infosData.size()==3){
									message = "Etes-vous sur de vouloir charger le thème ".concat((String)infosData.get(2))
									.concat(" issu de la base de données ").concat((String)infosData.get(1)).concat(" ?");
								} else {
									message = "Etes-vous sur de vouloir charger la population ".concat((String)infosData.get(3))
									.concat(" issue de la base de données ").concat((String)infosData.get(1)).concat(" ?");
								}
								oKCancel = JOptionPane.showConfirmDialog(null,message,"Confirmation du chargement",JOptionPane.OK_CANCEL_OPTION);

								if (oKCancel == 0){
									LoaderMethods.chargeDataSetJump(infosData,cogitIDDS,contextApp);
								}
								break;
							}
						}
					}
				}
			}
		});
		return jButton;
	}

	/** initialisation de jButtonAttribute
	 * @return JButton
	 */
	private JButton getJButtonAttribute() {
		jButton = new JButton();
		jButton.setPreferredSize(new Dimension(120,30));
		jButton.setText("Attributs");

		jButton.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
				String dsUsed,nomDS,message;
				int infosSize = infosData.size();

				dsUsed = (String)infosData.get(1);

				if (infosSize==3 && dsUsed.equals("Autres")){
					LoaderMethods.afficheAttributsMethodes(infosData,-2);
				}
				else if (infosSize==4){
					for (int i=0;i<((List<?>)hierarchy.get(1)).size();i++){
						nomDS = (String)((List<?>)hierarchy.get(1)).get(i);
						if (dsUsed.equals(nomDS)){
							cogitIDDS = ((Integer)((List<?>)hierarchy.get(2)).get(i)).intValue();
							break;
						}
					}
					LoaderMethods.afficheAttributsMethodes(infosData,cogitIDDS);
				}
				else {
					message = "Veuillez Sélectionner une population";
					JOptionPane.showMessageDialog(null,message,"Sélection erronée",JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		return jButton;
	}

	/** initialisation de jButtonQuitter
	 * @return JButton
	 */
	private JButton getJButtonQuitter() {
		jButton = new JButton();
		jButton.setPreferredSize(new Dimension(100,30));
		jButton.setText("Quitter");

		jButton.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		return jButton;
	}
}
