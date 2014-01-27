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

package fr.ign.cogit.geoxygene.ojplugin.plugin;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import fr.ign.cogit.geoxygene.ojplugin.plugin.appariement.AppariementReseauxPlugIn;
import fr.ign.cogit.geoxygene.ojplugin.plugin.cartetopo.CarteTopoCreationPlugIn;
import fr.ign.cogit.geoxygene.ojplugin.plugin.cartetopo.CarteTopoFaceCirculairePlugIn;
import fr.ign.cogit.geoxygene.ojplugin.plugin.cartetopo.CarteTopoNoeudValuePlugIn;
import fr.ign.cogit.geoxygene.ojplugin.plugin.quality.PlugInComparison;
import fr.ign.cogit.geoxygene.ojplugin.plugin.quality.PlugInCompute2D5Shape;
import fr.ign.cogit.geoxygene.ojplugin.plugin.quality.PlugInEstim;
import fr.ign.cogit.geoxygene.ojplugin.plugin.quality.PlugInMountainGridDetection;
import fr.ign.cogit.geoxygene.ojplugin.plugin.quality.PlugInRepresentationScaleEstimation;
import fr.ign.cogit.geoxygene.ojplugin.plugin.quality.PlugInSimulation;
import fr.ign.cogit.geoxygene.ojplugin.plugin.quality.PlugInUrbanDetection;



/**
 * Classe d'initialisation des plugins GeOxygene pour OpenJump
 * 
 * @author Eric Grosso - IGN / Laboratoire COGIT
 */
public class GeOxygeneExtension extends Extension {
	
	/** Appel des différents plugins via initializse()
	 */
	public void configure(PlugInContext context) throws Exception {
		
		// Le plugin suivant ne fait plus rien (code mis en ccommentaire ?)
		new CarteTopoCreationPlugIn().initialize(context);
		
		// Le plugin suivant ne fait plus rien (code mis en ccommentaire ?)
		new CarteTopoFaceCirculairePlugIn().initialize(context);
		
		// Le plugin suivant ne fait plus rien (code mis en ccommentaire ?)
		new CarteTopoNoeudValuePlugIn().initialize(context);
		
		// AppariementReseauxPlugIn dépend des 4 modules suivants
		// - geoxygene-api
		// - geoxygene-contrib
		// - geoxygene-feature
		// - geoxygene-spatial
		new AppariementReseauxPlugIn().initialize(context);
		  
		// Quality 
		new PlugInComparison().initialize(context);
		new PlugInEstim().initialize(context);
		new PlugInRepresentationScaleEstimation().initialize(context);
		new PlugInCompute2D5Shape().initialize(context);
		new PlugInSimulation().initialize(context);
		new PlugInMountainGridDetection().initialize(context);
		new PlugInUrbanDetection().initialize(context);
		
	}
	
}