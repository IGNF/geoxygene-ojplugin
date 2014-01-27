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
 * Results of the estimation by the EstIM model for each impact.
 * 
 * @author JFGirres
 * 
 */
public class PlugInEstimResults extends FeatureInfoPlugIn {

  public static MultiInputDialog inputDialog;

  public static double projectionImpact;
  public static double terrainImpact;
  public static double curveImpact;
  public static double digitizingImpact;
  public static double digitizingImprecision;
  public static double generalisationImpact;
  public static double initialMeasure;
  public static String datasetName;
  public static String measureType;

  /**
   * initialisation du menu parametrage
   * */
  public void initialize(PlugInContext context) throws Exception {

  }

  public boolean execute(PlugInContext context) {
    // le plugin commence
    inputDialog = createDialog(context);
    return true;
  }

  public MultiInputDialog createDialog(final PlugInContext context) {

    DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
    df.applyPattern("0.0#");

    String txtMeasureType = measureType;
    String txtInitialMeasure = df.format(initialMeasure);
    String txtProjectionImpact = df.format(projectionImpact);
    String txtTerrainImpact = df.format(terrainImpact);
    String txtCurveImpact = df.format(curveImpact);
    String txtDigitizingImpact = df.format(digitizingImpact);
    String txtDigitizingImprecision = df.format(digitizingImprecision);
    String txtGeneralisationImpact = df.format(generalisationImpact);

    // input
    inputDialog = new MultiInputDialog(context.getWorkbenchFrame(),
        "EstIM Results", false);

    inputDialog.addLabel("Evaluated dataset: " + datasetName);

    inputDialog
        .addLabel("-------------------------------------------------------------");

    String unit = null;
    if (txtMeasureType.equals("Length")) {
      unit = "m.";
      inputDialog.addLabel("Initial length: " + txtInitialMeasure + unit);
    }
    if (txtMeasureType.equals("Area")) {
      unit = "m².";
      inputDialog.addLabel("Initial area: " + txtInitialMeasure + unit);
    }

    inputDialog
        .addLabel("-------------------------------------------------------------");

    if (projectionImpact == 99999) {
      inputDialog.addLabel("Projection Impact : Not selected");
    } else if (projectionImpact == 66666) {
      inputDialog.addLabel("Projection Impact : Not computable");
    } else {
      inputDialog.addLabel("Projection Impact = " + txtProjectionImpact + unit);
    }

    if (terrainImpact == 99999) {
      inputDialog.addLabel("Terrain Impact : Not selected");
    } else if (terrainImpact == 66666) {
      inputDialog.addLabel("Terrain Impact : Not computable");
    } else {
      inputDialog.addLabel("Terrain Impact = " + txtTerrainImpact + unit);
    }

    if (curveImpact == 99999) {
      inputDialog.addLabel("Polygonal Approximation Impact : Not selected");
    } else if (curveImpact == 66666) {
      inputDialog.addLabel("Polygonal Approximation Impact : Not computable");
    } else {
      inputDialog.addLabel("Polygonal Approximation Impact = " + txtCurveImpact
          + unit);
    }

    if (digitizingImpact == 99999) {
      inputDialog.addLabel("Digitizing Error Impact : Not selected");
    } else if (digitizingImpact == 66666) {
      inputDialog.addLabel("Digitizing Error Impact : Not computable");
    } else {
      inputDialog.addLabel("Digitizing Error Impact = " + txtDigitizingImpact
          + unit + " (+/- " + txtDigitizingImprecision + unit + ")");
    }

    if (generalisationImpact == 99999) {
      inputDialog.addLabel("Generalisation Impact : Not selected");
    } else if (generalisationImpact == 66666) {
      inputDialog.addLabel("Generalisation Impact : Not computable");
    } else {
      inputDialog.addLabel("Generalisation Impact = " + txtGeneralisationImpact
          + unit);
    }

    inputDialog
        .addLabel("-------------------------------------------------------------");

    // Centre et rend visible la fenêtre
    GUIUtil.centreOnWindow(inputDialog);
    inputDialog.setVisible(true);
    return inputDialog;
  }

  public void setImpacts(String type, String name, double measure,
      double projectionValue, double terrainValue, double curveValue,
      double digitizingValue, double digitizingSigmaValue,
      double generalisationValue) {

    datasetName = name;
    measureType = type;
    initialMeasure = measure;
    projectionImpact = projectionValue;
    terrainImpact = terrainValue;
    curveImpact = curveValue;
    digitizingImpact = digitizingValue;
    digitizingImprecision = 3 * digitizingSigmaValue;
    generalisationImpact = generalisationValue;

  }

}
