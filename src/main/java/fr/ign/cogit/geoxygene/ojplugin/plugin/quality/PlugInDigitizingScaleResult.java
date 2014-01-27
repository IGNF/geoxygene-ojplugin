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
 *  Copyright (C) 2013 Institut Géographique National
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
package fr.ign.cogit.geoxygene.ojplugin.plugin.quality;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInfoPlugIn;

/**
 * A plugin to show the result of digitizing scale (computed with granularity
 * exploration)
 * @author JFGirres
 * 
 */
public class PlugInDigitizingScaleResult extends FeatureInfoPlugIn {

  public static MultiInputDialog inputDialog;

  public static Double digitizingScale;

  /**
   * initialisation du menu parametrage
   * */
  public void initialize(PlugInContext context) throws Exception {

  }

  public boolean execute(PlugInContext context) {
    inputDialog = createDialog(context);
    return true;
  }

  public MultiInputDialog createDialog(final PlugInContext context) {

    DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
    df.applyPattern("0.0#");

    Integer scale = digitizingScale.intValue();
    String txtDigitizingScale = scale.toString();

    // input
    inputDialog = new MultiInputDialog(context.getWorkbenchFrame(),
        "Scale Estimation", false);

    inputDialog.addLabel("-----------------------------------");

    inputDialog.addLabel("Capture scale = 1/" + txtDigitizingScale);

    inputDialog.addLabel("-----------------------------------");

    // Centre et rend visible la fenêtre
    GUIUtil.centreOnWindow(inputDialog);
    inputDialog.setVisible(true);
    return inputDialog;
  }

  public void setScale(double scale) {
    digitizingScale = scale;
  }

}
