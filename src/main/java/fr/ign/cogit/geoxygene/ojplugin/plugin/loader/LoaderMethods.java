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


import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.datatools.Geodatabase;
import fr.ign.cogit.geoxygene.datatools.ojb.GeodatabaseOjbFactory;
import fr.ign.cogit.geoxygene.feature.DataSet;
import fr.ign.cogit.geoxygene.feature.Population;

/** Classe Créée afin de définir les Méthodes utilisées pour
 *  la création d'une interface de chargement
 *  @author Grosso - IGN / Laboratoire COGIT
 *  Créé le 8 nov. 2004
 */
public class LoaderMethods {

	/**constructeur
	 */
	public LoaderMethods(){
	}

	/** Méthode qui renvoie les deux premiers niveaux
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> hierarchyDataAndIdDataSet(){
		// connexion à la base de données
		DataSet.db = GeodatabaseOjbFactory.newInstance();
		// hierarchie dataset: il s'agit d'une liste de listes (listes contenant en position 0 le dataset parent
		// et ensuite les datasets enfants s'y rattachant)
		List hierarchyData = new ArrayList();
		//liste contenant les COGITID des datasets parents
		List idDataSet = new ArrayList();
		//liste de tous les datasets présents dans la table dataset du SGBD
		List dataSets = new ArrayList();
		//CHARGEMENT DATASET
		dataSets = DataSet.db.loadAll(DataSet.class);
		//duplication de la liste de tous les datasets
		List dataSetsClone = new ArrayList(dataSets);

		DataSet ds,dsClone;
		List structureDS, listeRetour = new ArrayList();
		Iterator itDS = dataSetsClone.iterator(),itDSClone;
		int cogitIDTest;

		while(itDS.hasNext()) {
			ds = (DataSet)itDS.next();
			itDSClone = dataSetsClone.iterator();
			structureDS = new ArrayList();
			//le dataset est il parent? (ds.getAppartientA()==null si dataset de plus haut niveau)
			if (ds.getAppartientA() == null ) {
				structureDS.add(ds);
				//ajout du COGITID dans la liste des ids
				idDataSet.add(new Integer(ds.getId()));
				//on regarde quels sont les enfants du dataset parent
				while(itDSClone.hasNext()){
					dsClone = (DataSet)itDSClone.next();
					if (dsClone.getAppartientA() != null){
						cogitIDTest = dsClone.getAppartientA().getId();
						if (!dsClone.equals(ds) && cogitIDTest==ds.getId()){
							structureDS.add(dsClone);
						}
					}
				}
				//ajout d'une liste avec le dataset parent en position 0 et les datasets
				// enfants ensuite
				hierarchyData.add(structureDS);
			}
		}

		listeRetour.add(hierarchyData);
		listeRetour.add(idDataSet);
		return listeRetour;
	}

	/**Construit un JTree à partir du dataset oracle à partir de ce que renvoie
	 * la Méthode static Matching.hierarchyData().
	 * La liste renvoyée contient le JTree Créé, une liste des noms des datasets
	 * présents dans la base
	 * @return List
	 * @throws ClassNotFoundException
	 */
	public static List<Object> creationJTreeDataSet() throws ClassNotFoundException {
		List<Object> hierarchyDataAndIdDataSet = LoaderMethods.hierarchyDataAndIdDataSet();
		List<?> hierarchy = (List<?>)hierarchyDataAndIdDataSet.get(0);
		List<String> nomsDS = new ArrayList<String>();
		List<IPopulation<? extends IFeature>> populations = new ArrayList<IPopulation<? extends IFeature>>();
		List<String> tablesDataSet = new ArrayList<String>(), tablesGeomTable = new ArrayList<String>();
		List<String> tablesAConserver = new ArrayList<String>();
		DataSet dataset,dataset2;
		IPopulation<? extends IFeature> pop;
		String ntmDataSet,theme,query,sqlTableName;
		Iterator<?> itHierarchy, itHierarchies, itTablesGeom, itTablesAConserver;
		Iterator<IPopulation<? extends IFeature>> itPop;
		int test;
		Class<?> theClass = null;

		//noeud abstrait (méta-parent)
		DefaultMutableTreeNode noeudBD = new DefaultMutableTreeNode("Jeux de données");
		//initialisation de l'arbre
		DefaultTreeModel modele = new DefaultTreeModel(noeudBD);
		JTree arbreBD = new JTree(modele);
		arbreBD.setBackground(new Color(232,180,200));
		arbreBD.setForeground(new Color(232,180,200));
		arbreBD.setVisible(true);
		//initialisation des noeuds: level1: datasets parents(bdcarto/bdtopo), level2: datasets enfants (admin,routier...)
		//level3: populations (troncon routier...)
		DefaultMutableTreeNode noeudBDDSlevel1, noeudBDDSlevel2, noeudBDDSlevel3;

		//concerne la partie relative aux populations qui sont contenues dans les tables dataset et population
		itHierarchy = hierarchy.iterator();
		while (itHierarchy.hasNext()){
			List<?> hierarchies = (List<?>)itHierarchy.next();
			dataset = (DataSet)hierarchies.get(0);
			//nom, type et modèle du dataset (au regard des renseignements passés pendant la création du dataset)
			ntmDataSet = dataset.getNom().concat(" (").concat(dataset.getTypeBD()).concat(" ").concat(dataset.getModele()).concat(")");
			//ajout du noeud de niveau 1
			noeudBDDSlevel1 = new DefaultMutableTreeNode(ntmDataSet);
			noeudBD.add(noeudBDDSlevel1);
			itHierarchies = hierarchies.iterator();
			itHierarchies.next();
			//création des noeuds pour les datasets enfants (admin, routier)
			while(itHierarchies.hasNext()){
				dataset2 = (DataSet)itHierarchies.next();
				theme = dataset2.getNom();
				noeudBDDSlevel2 = new DefaultMutableTreeNode(theme);
				noeudBDDSlevel1.add(noeudBDDSlevel2);

				populations = dataset2.getPopulations();

				//remplissage des populations
				itPop = populations.iterator();
				//crée des noeuds pour chaque population d'un dataset fils niveau 2 donné
				while(itPop.hasNext()){
					pop = itPop.next();
					//Code qui récupère le nom de la table d'une population donnée
					//((Metadata)DataSet.db.getMetadata(Class.forName(pop.getNomClasse()))).getClass();
					//tablesDataSet.add(((Metadata)DataSet.db.getMetadata(Class.forName(pop.getNomClasse()))).getTableName());
					test = 0;
					//on regarde si la classe existe réellement; si non, alors test =1
					try {
						theClass = Class.forName(pop.getNomClasse());
					}catch (Exception e) {
						test = 1;
					}
					if (test == 0){
						noeudBDDSlevel3 = new DefaultMutableTreeNode(pop.getNom());
						noeudBDDSlevel2.add(noeudBDDSlevel3);
						tablesDataSet.add((DataSet.db.getMetadata(theClass)).getTableName());
					}
				}
				//test si le noeud a des enfants; si non, on l'ôte de l'arbre
				if (noeudBDDSlevel2.getChildCount()==0)noeudBDDSlevel1.remove(noeudBDDSlevel2);
			}
			//test si le noeud a des enfants; si non, on l'ôte de l'arbre
			if (noeudBDDSlevel1.getChildCount()==0)noeudBD.remove(noeudBDDSlevel1);
			//ajout à la liste des noms des datasets parents
			else nomsDS.add(ntmDataSet);
		}

		//Calcul du différentiel entre les populations insérées dans l'arbre
		//et celles qui sont contenues dans la table des géométries au niveau
		//du SGBD
		if (DataSet.db.getDBMS() == Geodatabase.ORACLE) query = "SELECT TABLE_NAME FROM USER_SDO_GEOM_METADATA";
		else if (DataSet.db.getDBMS() == Geodatabase.POSTGIS) query = "SELECT F_TABLE_NAME FROM GEOMETRY_COLUMNS";
		else query = "";
		try {
			Connection conn = DataSet.db.getConnection();
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			while (rs.next()) {
				sqlTableName = rs.getString(1);
				tablesGeomTable.add(sqlTableName);
			}

			//on ne conserve que les tables qui ont au moins un enregistrement
			//et qui ne sont pas correspondent pas à un enregistrement dans la
			//table population
			tablesGeomTable.removeAll(tablesDataSet);

			itTablesGeom = tablesGeomTable.iterator();
			while (itTablesGeom.hasNext()){
				sqlTableName = (String)itTablesGeom.next();
				query = "SELECT COUNT(*) FROM ".concat(sqlTableName);
				rs = stm.executeQuery(query);
				rs.next();
				if (new Double(rs.getString(1)).doubleValue()!=0){
					tablesAConserver.add(sqlTableName);
				}
			}
			stm.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//ajout des populations qui ne dans l'arbre
		//noeud "Autres"
		String nomTable;

		//les tables conservées sont celles qui contiennent des enregistrements (voir
		//test précédent) et celles qui ont un fichier de mapping
		if(tablesAConserver.size()!=0){
			noeudBDDSlevel1 = new DefaultMutableTreeNode("Autres");
			noeudBD.add(noeudBDDSlevel1);
			itTablesAConserver = tablesAConserver.iterator();
			while(itTablesAConserver.hasNext()){
				nomTable=(String)itTablesAConserver.next();
				test = 0;
				try {
					theClass = (DataSet.db.getMetadata(nomTable)).getJavaClass();
				} catch (Exception e) {
					test = 1;
				}
				//test pour savoir si la table est en correspondance dans un fichier de mapping
				if (test==0){
					noeudBDDSlevel2 = new DefaultMutableTreeNode(nomTable);
					noeudBDDSlevel1.add(noeudBDDSlevel2);
				}
			}
			//test si le noeud a des enfants; si non, on l'ôte de l'arbre
			if (noeudBDDSlevel1.getChildCount()==0)noeudBD.remove(noeudBDDSlevel1);
		}

		List<Object> all = new ArrayList<Object>();
		//ajout de l'arbre
		all.add(arbreBD);
		//nom des datasets parents
		all.add(nomsDS);
		//COGITIDs des datasets parents
		all.add(hierarchyDataAndIdDataSet.get(1));
		return all;
	}

	/** Chargement Jump depuis l'interface java
	 */
	@SuppressWarnings("unchecked")
	public static void chargeDataSetJump(List<?> infosData,int cogitIDDS,PlugInContext context){

		Color couleur;
		IFeatureCollection<? extends IFeature> ftfc;

		Class<?> theClass;
		DataSet ds = DataSet.db.load(DataSet.class, new Integer(cogitIDDS));
		String dsName = (ds.getTypeBD()).concat(" ").concat(ds.getModele());
		String themeName = (String)infosData.get(2), popName;

		if (infosData.size()==4){
			popName = (String)infosData.get(3);
			theClass = ds.getComposant(themeName).getPopulation(popName).getClasse();
			ftfc = DataSet.db.loadAllFeatures(theClass);
			couleur = JColorChooser.showDialog(new ColorChooser(),"Choix de couleur pour ".concat(popName),Color.blue);
			if (couleur == null) couleur = Color.blue;
			UtilJump.afficheCollection(context,ftfc,dsName+"/"+themeName,popName,couleur);
		}
		else{
			List<IPopulation<? extends IFeature>> populations = ds.getComposant(themeName).getPopulations();
			Iterator<IPopulation<? extends IFeature>> itPop = populations.iterator();
			while(itPop.hasNext()){
				popName = ((Population)itPop.next()).getNom();
				theClass = ds.getComposant(themeName).getPopulation(popName).getClasse();
				ftfc = DataSet.db.loadAllFeatures(theClass);
				couleur = JColorChooser.showDialog(new ColorChooser(),"Choix de couleur pour ".concat(popName),Color.blue);
				if (couleur == null) couleur = Color.blue;
				UtilJump.afficheCollection(context,ftfc,dsName+"/"+themeName,popName,couleur);
			}
		}
	}

	/** Affichage des attributs et des Méthodes d'une population donnée
	 *   si cette population est contenue dans la table population
	 */
	public static void afficheAttributsMethodes(List<?> infosData, int cogitIDDS) {
		//initialisation des variables
		Field[] attribs;
		Method[] meths;
		int i = 0;

		DataSet ds;
		Class<?> classeAbstraite;
		String nomPop;

		if (cogitIDDS==-2){
			nomPop=(String)infosData.get(2);
			classeAbstraite = (DataSet.db.getMetadata(nomPop)).getJavaClass().getSuperclass();
		}
		else{
			//dataset concerné
			ds = DataSet.db.load(DataSet.class, new Integer(cogitIDDS));
			//en fonction du thème et de la population
			nomPop=(String)infosData.get(3);
			classeAbstraite = ds.getComposant((String)infosData.get(2)).getPopulation(nomPop).getClasse().getSuperclass();
		}

		//Attributs
		attribs = classeAbstraite.getDeclaredFields();
		//Méthodes
		meths = classeAbstraite.getDeclaredMethods();

		//information qui sera contenue dans les JTables
		AttributesTable tAtt;
		String [][] dataNomAttributs = new String[attribs.length][1], dataTypeAttributs = new String[attribs.length][1];
		String [][] dataNomMethodes = new String[meths.length][1], dataTypeMethodes = new String[meths.length][1];

		while(i!=attribs.length){
			Field attrib = attribs[i];
			dataNomAttributs[i][0]=attrib.getName();
			dataTypeAttributs[i][0]=typeVar(attrib.getType().getName());
			i++;
		}

		i = 0;
		while(i!=meths.length){
			Method meth = meths[i];
			dataNomMethodes[i][0]=meth.getName();
			dataTypeMethodes[i][0]=typeVar(meth.getReturnType().getName());
			i++;
		}

		//affichage de la table dans une fenêtre
		tAtt = new AttributesTable(dataNomAttributs,dataTypeAttributs,dataNomMethodes,dataTypeMethodes,nomPop);
		tAtt.setVisible(true);
	}

	/**Méthode qui renvoit en langage naturel le type de l'attribut passé en paramètre
	 * @param typeVariable
	 * @return chaine de caractère représentant le type de l'attribut
	 */
	public static String typeVar(String typeVariable){
		String typeVar = typeVariable;
		//ordre à conserver sinon conflit entre GM_LineString et String
		if (typeVariable.endsWith("GM_LineString"))return "ligne";
		if (typeVariable.endsWith("String"))return "string";
		if (typeVariable.endsWith("boolean"))return "boolean";
		if (typeVariable.endsWith("double"))return "double";
		if (typeVariable.endsWith("GM_Point"))return "point";
		if (typeVariable.endsWith("int"))return "entier";
		if (typeVariable.endsWith("GM_Polygon"))return "surface";
		if (typeVariable.endsWith("List")) return "liste";
		return typeVar;
	}
}
