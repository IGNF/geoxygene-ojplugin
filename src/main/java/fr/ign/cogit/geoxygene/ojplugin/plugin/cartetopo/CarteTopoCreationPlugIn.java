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

package fr.ign.cogit.geoxygene.ojplugin.plugin.cartetopo;

import java.awt.Color;
import java.util.Iterator;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;


import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_Feature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;
import fr.ign.cogit.geoxygene.ojplugin.plugin.I18NPlug;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;


/** Création de la carte topologique (noeuds, arcs, faces) : plugin OpenJump
 * 
 *  @author Eric Grosso - IGN / Laboratoire COGIT
 */
public class CarteTopoCreationPlugIn extends AbstractPlugIn implements ThreadedPlugIn{

	private static MultiInputDialog dialog;
	
    private final static String NOEUDS = I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.affichagenoeuds");
    private final static String ARCS = I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.affichagearcs");
    private final static String FACES = I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.affichagefaces");
    private final static String TOLERANCE = I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.tolerance");
    private final static String TOLERANCETOOLTIP = I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.tolerancetooltip");
    private final static String PLANAIRE = I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.planaire");
    
    boolean bNoeuds = true;
    boolean bArcs = true;
    boolean bFaces = true;
    boolean bPlanaire = true;
    double tolerance = 1;
	
	@Override
	public void initialize(PlugInContext context) throws Exception {
		FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
		featureInstaller.addMainMenuPlugin(
				this,
				new String[] {MenuNames.PLUGINS, "GeOxygene",I18NPlug.getString("cartetopologique")},
				I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.creationcartetopologique"),
				false,
				null,
				createEnableCheck(context.getWorkbenchContext()));
	}

	public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
		EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

		return new MultiEnableCheck()
		.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
	}

	@Override
	public boolean execute(PlugInContext context) throws Exception {
		this.reportNothingToUndoYet(context);
		dialog = new MultiInputDialog(
				context.getWorkbenchFrame(), getName(), true);
		setDialogValues(dialog, context);
		GUIUtil.centreOnWindow(dialog);
		dialog.setVisible(true);
		if (! dialog.wasOKPressed()) { return false; }
		getDialogValues();
		return true;
	}
	
	private void getDialogValues() {
		this.bNoeuds = dialog.getBoolean(NOEUDS);
		this.bArcs = dialog.getBoolean(ARCS);
		this.bFaces = dialog.getBoolean(FACES);
		this.bPlanaire = dialog.getBoolean(PLANAIRE);
		this.tolerance = dialog.getDouble(TOLERANCE);
	}

	private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
		dialog.setSideBarDescription(
				I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.creationcartetopologique"));
		dialog.addLayerComboBox(I18NPlug.getString("selectioncouche"),
				context.getLayerNamePanel().getLayerManager().getLayer(0),
				context.getLayerNamePanel().getLayerManager());
		dialog.addCheckBox(NOEUDS, true, NOEUDS);
		dialog.addCheckBox(ARCS, true, ARCS);
		dialog.addCheckBox(FACES, true, FACES);
		dialog.addCheckBox(PLANAIRE, true, PLANAIRE);
		dialog.addDoubleField(TOLERANCE, 1, 4, TOLERANCETOOLTIP);
	}

	@Override
	public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
		
	    System.gc();

		Layer layer = CarteTopoCreationPlugIn.dialog
			.getLayer(I18NPlug.getString("selectioncouche"));
		
		//conversion en collection geoxygene
		FT_FeatureCollection<DefaultFeature> collection = new FT_FeatureCollection<DefaultFeature>();		
		
		try {
			collection = UtilJump.getObjectsFromLayer(layer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Création de la carte topologique
		CarteTopo carteTopo = creeCarteTopo(collection,
				this.bFaces,
				this.bPlanaire,
				this.tolerance);
			
		//Affichage
		if(this.bNoeuds) UtilJump.afficheCollection(
				context,
				carteTopo.getPopNoeuds(),
				StandardCategoryNames.RESULT,
				layer.getName() + " " + I18NPlug.getString("noeuds"),
				Color.blue);
		
		if(this.bArcs) UtilJump.afficheCollection(
				context,carteTopo.getPopArcs(),
				StandardCategoryNames.RESULT,
				layer.getName() + " " + I18NPlug.getString("arcs"),
				Color.magenta);
		
		if(this.bFaces) UtilJump.afficheCollection(
				context,
				carteTopo.getPopFaces(),
				StandardCategoryNames.RESULT,
				layer.getName() + " " + I18NPlug.getString("faces"),
				Color.green);
	}
	
	
	/**Création d'une CarteTopo à partir d'une FT_FeatureCollection
	 * 
	 * @param collection
	 * @return une carte topologique
	 */
	@SuppressWarnings("unchecked")
	private static CarteTopo creeCarteTopo(
			FT_FeatureCollection<? extends DefaultFeature> collection,
					boolean affichageFaces,
					boolean planaire,
					double tolerance) {
		
		//Initialisation d'une nouvelle CarteTopo
		CarteTopo carteTopo = new CarteTopo("Graphe");

		if(collection.isEmpty()) return carteTopo;
		
		Iterator<? extends DefaultFeature> it = collection.getElements().iterator();
		DefaultFeature feature;
		Arc arc;

		//Récupération des arcs de la carteTopo
		Population<Arc> arcs = (Population<Arc>) carteTopo.getPopArcs();
		
		//Import des arcs de la collection dans la carteTopo
		while (it.hasNext()) {
			feature = it.next();
			//création d'un nouvel élément
			arc = arcs.nouvelElement();
			//affectation de la géométrie de l'objet issu de la collection
			//à l'arc de la carteTopo
			arc.setGeometrie((GM_LineString) feature.getGeom());
			//instanciation de la relation entre l'arc créé et l'objet
			//issu de la collection
			arc.addCorrespondant(feature);
		}
		
		System.out.println(carteTopo.getPopArcs().size() + " " 
				+ I18NPlug.getString("cartetopo.CarteTopoCreationPlugIn.nbarcs"));
		
		//Création des noeuds manquants
		System.out.println(I18NPlug
				.getString("cartetopo.CarteTopoCreationPlugIn.creationnoeudsmanquants"));
		carteTopo.creeNoeudsManquants(tolerance);
		//Création de la topologie Arcs Noeuds
		System.out.println(I18NPlug
				.getString("cartetopo.CarteTopoCreationPlugIn.creationtopologiearcsnoeuds"));
		carteTopo.creeTopologieArcsNoeuds(tolerance);

		if(planaire) {
			//La carteTopo est rendue planaire
			System.out.println(I18NPlug
					.getString("cartetopo.CarteTopoCreationPlugIn.cartetopoplanaire"));
			carteTopo.rendPlanaire(tolerance);			
		}
		


		// Création des faces de la carteTopo
		if(affichageFaces)  {
			System.out.println(I18NPlug
					.getString("cartetopo.CarteTopoCreationPlugIn.creationfaces"));
			carteTopo.creeTopologieFaces();
			System.out.println(carteTopo.getPopFaces().size() + " " + I18NPlug
					.getString("cartetopo.CarteTopoCreationPlugIn.nbfaces"));
		}

		return carteTopo;
	}

}