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

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_Feature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;
import fr.ign.cogit.geoxygene.ojplugin.plugin.I18NPlug;

/** Création d'une carte topologique pour cartographiée le degré des noeuds : plugin OpenJump
 * 
 *  @author grosso - IGN / Laboratoire COGIT
 */
public class CarteTopoNoeudValuePlugIn extends AbstractPlugIn implements ThreadedPlugIn{

	private static MultiInputDialog dialog;

	@Override
	public void initialize(PlugInContext context) throws Exception {
		FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
		featureInstaller.addMainMenuPlugin(
				this,
				new String[] {MenuNames.PLUGINS, "GeOxygene",I18NPlug.getString("cartetopologique")},
				I18NPlug.getString("cartetopo.CarteTopoNoeudValuePlugIn.cartodegrenoeuds"),
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
		return true;
	}

	private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
		dialog.setSideBarDescription(
				I18NPlug.getString("cartetopo.CarteTopoNoeudValuePlugIn.cartodegrenoeuds"));
		dialog.addLayerComboBox(I18NPlug.getString("selectioncouche"),
				context.getLayerNamePanel().getLayerManager().getLayer(0),
				context.getLayerNamePanel().getLayerManager());
	}

	@Override
	public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
		System.gc();

		Layer layer = CarteTopoNoeudValuePlugIn.dialog
			.getLayer(I18NPlug.getString("selectioncouche"));

		//conversion en collection geoxygene
		FT_FeatureCollection<DefaultFeature> collection = new FT_FeatureCollection<DefaultFeature>();		

		try {
			collection = UtilJump.getObjectsFromLayer(layer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Création de la carte topologique
		CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(collection);

		//Affichage
		UtilJump.afficheCollection(
				context,
				carteTopo.getPopNoeuds(),
				StandardCategoryNames.RESULT,
				layer.getName() + " " + I18NPlug
					.getString("cartetopo.CarteTopoNoeudValuePlugIn.noeudsvalues"),
				Color.red);
	}

}