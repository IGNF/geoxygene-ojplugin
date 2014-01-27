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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInfoPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.quality.estim.scaledetection.RoadCoalescenceDetection;
import fr.ign.cogit.geoxygene.contrib.quality.estim.scaledetection.RoadSymbolOverlap;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;

/**
 * A plugin to facilitate the estimation of the representation scale on a
 * generalised road network
 * 
 * @author JFGirres
 * 
 */
public class PlugInRepresentationScaleEstimation extends FeatureInfoPlugIn {

  static Logger logger = Logger.getLogger(PlugInRepresentationScaleEstimation.class.getName());
  
  public static MultiInputDialog inputDialog;
  public static IFeatureCollection<IFeature> jddIn = new FT_FeatureCollection<IFeature>();
  public static IFeatureCollection<IFeature> jddThickeningZone = new FT_FeatureCollection<IFeature>();
  public static IFeatureCollection<IFeature> jddBuffer = new FT_FeatureCollection<IFeature>();

  /** initialisation du menu parametrage */
  public void initialize(PlugInContext context) throws Exception {
      FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
      featureInstaller.addMainMenuPlugin(this, new String[] { MenuNames.PLUGINS, "GeOxygene", "Spatial Data Quality",
              "Util" }, "Representation Scale Estimation", false, null, createEnableCheck(context.getWorkbenchContext()));
      
      /*EnableCheckFactory factory = new EnableCheckFactory(
        context.getWorkbenchContext());
    context.getFeatureInstaller().addMainMenuItem(
        this,
        new String[] { "GeOxygene", "Spatial Data Quality", "Util" },
        "Representation Scale Estimation",
        false,
        null,
        new MultiEnableCheck().add(factory
            .createAtLeastNLayersMustExistCheck(1)));*/
  }
  
  public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

    return new MultiEnableCheck()
    .add(checkFactory.createAtLeastNLayersMustExistCheck(1));
  }

  /**
   * Execute
   * @param context
   */
  public boolean execute(PlugInContext context) {
    // le plugin commence
    inputDialog = createDialog(context);
    return true;
  }

  /**
   * @param context
   * @return
   */
  public MultiInputDialog createDialog(final PlugInContext context) {
    // input
    inputDialog = new MultiInputDialog(context.getWorkbenchFrame(),
        "Spatial Data Quality", false);

    // Une combobox permettant de choisir le jdd à bruiter
    inputDialog.addLayerComboBox("LineString Dataset", // nom de la variable
        context.getLayerNamePanel().getLayerManager().getLayer(0),// un layer
                                                                  // initial
        context.getLayerNamePanel().getLayerManager()); // layer manager

    inputDialog
        .addLabel("-----------------------------------------------------------------------------------------------------------");
    List<String> itemsTheme = Arrays.asList("Roads", "Railways", "Rivers",
        "Other");
    final JComboBox themeComboBox = inputDialog.addComboBox("Layer Theme",
        "Layer Theme", itemsTheme, "Layer Theme");
    themeComboBox.setEnabled(false);

    inputDialog
        .addLabel("-----------------------------------------------------------------------------------------------------------");
    List<String> itemsMethod = Arrays.asList("Road Symbol Overlap",
        "Road Coalescence");
    final JComboBox methodComboBox = inputDialog.addComboBox(
        "Evaluation Method", "Evaluation Method", itemsMethod,
        "Evaluation Method");

    inputDialog
        .addLabel("---------------------------------------------------------------------------");

    final JTextField thresholdBox = inputDialog.addDoubleField(
        "Coalescence Threshold", 1.7, 5, "Coalescence Threshold");
    thresholdBox.setEnabled(false);

    // Bouton validation
    JButton validationButton = new JButton("OK");
    inputDialog.addRow("Scale estimation", new JLabel("Scale estimation"),
        validationButton, null, "Scale Detection");

    inputDialog
        .addLabel("---------------------------------------------------------------------------");

    methodComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String method = methodComboBox.getSelectedItem().toString();
        if (method.equals("Road Coalescence")) {
          thresholdBox.setEnabled(true);
        }
      }
    });

    // Bouton validation
    validationButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {

        jddThickeningZone.clear();
        jddBuffer.clear();

        Layer layerIn = (Layer) PlugInRepresentationScaleEstimation.inputDialog
            .getComboBox("LineString Dataset").getSelectedItem();
        jddIn = UtilJump.convertSelection(context, layerIn);
        CarteTopo carteTopoRoads = CarteTopoFactory.newCarteTopo(jddIn);

        String method = methodComboBox.getSelectedItem().toString();
        themeComboBox.getSelectedItem().toString();
        double scale = 30000;
        boolean roadOverlap = false;
        boolean curveIntersect = false;

        if (method.equals("Road Symbol Overlap")) {
          while (!(roadOverlap == true)) {
            scale = scale + 10000.0;

            if (roadOverlap == false) {
              logger.debug("Scale = 1/" + scale);
              RoadSymbolOverlap detect1 = new RoadSymbolOverlap(carteTopoRoads,
                  400);
              detect1.setScale(scale);
              detect1.execute();
              if (detect1.isThickened() == true) {
                roadOverlap = true;
                jddThickeningZone = detect1.getJddThickeningAreas();
                jddBuffer = detect1.getJddBufferArc();
              }
            }
          }
          PlugInRepresentationScaleResult plugIn = new PlugInRepresentationScaleResult();
          plugIn.setScale(scale);
          plugIn.execute(context);
          logger.debug("Scale = 1/" + scale + " --> Points = "
              + jddThickeningZone.size());
        }

        if (method.equals("Road Coalescence")) {
          double threshold = inputDialog.getDouble("Coalescence Threshold");

          while (!(curveIntersect == true)) {
            scale = scale + 10000.0;
            if (curveIntersect == false) {
              logger.debug("Scale = 1/" + scale);
              RoadCoalescenceDetection detect2 = new RoadCoalescenceDetection(
                  carteTopoRoads);
              detect2.setScale(scale);
              detect2.setThreshold(threshold);
              jddBuffer = detect2.execute();

              if (detect2.isThickened() == true) {
                curveIntersect = true;
                jddThickeningZone = detect2.getJddThickeningPoint();
                jddBuffer = detect2.getJddThickeningBuffer();
              }
            }
          }
          PlugInRepresentationScaleResult plugIn = new PlugInRepresentationScaleResult();
          plugIn.setScale(scale);
          plugIn.execute(context);
          logger.debug("Scale = 1/" + scale + " --> Points = "
              + jddThickeningZone.size());
        }

        // Affichage
        UtilJump.afficheCollection(context, jddIn, "Jeu initial", "Arcs",
            Color.green);
        UtilJump.afficheCollection(context, jddThickeningZone,
            "Scale detection", "Thickening areas", Color.red);
        UtilJump.afficheCollection(context, jddBuffer, "Scale detection",
            "Buffer areas", Color.blue);
      }

    });

    // Centre et rend visible la fen�tre
    GUIUtil.centreOnWindow(inputDialog);
    inputDialog.setVisible(true);
    return inputDialog;
  }
}
