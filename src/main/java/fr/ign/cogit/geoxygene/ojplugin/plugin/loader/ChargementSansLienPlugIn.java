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
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInfoPlugIn;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.datatools.Geodatabase;
import fr.ign.cogit.geoxygene.datatools.ojb.GeodatabaseOjbFactory;
import fr.ign.cogit.geoxygene.util.loader.MetadataReader;

import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;

/**
 * 
 * Cette classe contient le code permettant de charger une couche de données
 * GeOxygene (= une table correspondant à un fichier java). La fonction est
 * accessible par un item du menu GeOxygene, et par un menu contextuel dans le
 * panneau de gauche de JUMP.
 * 
 * English : GeOxygene data loader (JUMP plugin)
 * @author bonin
 */
//@TODO 
//Ce plugin dépend du module GeOxygene
//- geoxygene-database
//et des librairie Apache rassemblées dans
//- ojb-1.0.rc6-COGIT.jar
//De plus la classe suivante ne peut pas s'initialiser
//org.apache.ojb.broker.PersistenceBrokerFactory.defaultPersistenceBroker
//De plus il ne devrait sans doute pas étendre FeatureInfoPlugIn
//avec lequel il n'a rien à voir et qui nécessite d'avoir un objet sélectionné
public class ChargementSansLienPlugIn extends FeatureInfoPlugIn {

	@Override
	public void initialize(PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuPlugin(this,
				new String[]{MenuNames.PLUGINS,"GeOxygene","Chargement des données"},
					"Chargement d'une table sans lien", false, null, null);

		context.getFeatureInstaller().addPopupMenuItem(context.getWorkbenchFrame().getCategoryPopupMenu(),
				this,"Chargement de données GeOxygene",false,GUIUtil.toSmallIcon(FeatureInfoTool.ICON),
				null);
	}

	@Override
	public boolean execute(PlugInContext context) throws Exception {
		List<String> allTables = new ArrayList<String>();
		Geodatabase data = GeodatabaseOjbFactory.newInstance();

		MetadataReader theMetadataReader = new MetadataReader(data);
		allTables = theMetadataReader.getSelectedTables();
		for (int i=0; i<allTables.size(); i++) {
			String tableName = allTables.get(i);
			System.out.println(tableName);
			IFeatureCollection<IFeature> coll = data.loadAllFeatures(data.getMetadata(tableName).getJavaClass());
			// do NOT export Links
			// on n'exporte pas les liens
			UtilJump.exportLinks = false;
			UtilJump.afficheCollection(context,coll,"Working",tableName,Color.blue);
		}
		return true;
	}

}
