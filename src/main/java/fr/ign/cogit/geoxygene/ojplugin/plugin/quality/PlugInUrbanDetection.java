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

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Task;
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
import fr.ign.cogit.geoxygene.contrib.quality.estim.spatialcontext.zone.UrbanAreaZone;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;


/**
 * A plugin to facilitate the delineation of urban areas using a road network.
 * @author JFGirres
 * 
 */
public class PlugInUrbanDetection extends FeatureInfoPlugIn {

  public static MultiInputDialog inputDialog;
  public static IFeatureCollection<IFeature> jddIn = new FT_FeatureCollection<IFeature>();
  public static IFeatureCollection<IFeature> jddOut = new FT_FeatureCollection<IFeature>();

  /**
   * initialisation du menu parametrage
   * */
  public void initialize(PlugInContext context) throws Exception {
      
      FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
      featureInstaller.addMainMenuPlugin(this, new String[] { MenuNames.PLUGINS, "GeOxygene", "Spatial Data Quality",
              "Spatial Context" }, "Urban Detection", false, null, createEnableCheck(context.getWorkbenchContext()));
    
      /*EnableCheckFactory factory = new EnableCheckFactory(
        context.getWorkbenchContext());
    context.getFeatureInstaller()
        .addMainMenuItem(
            this,
            new String[] { "GeOxygene", "Spatial Data Quality",
                "Spatial Context" },
            "Urban Detection",
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

  public boolean execute(PlugInContext context) {
    // le plugin commence
    inputDialog = createDialog(context);
    return true;
  }

  public MultiInputDialog createDialog(final PlugInContext context) {
    // input
    inputDialog = new MultiInputDialog(context.getWorkbenchFrame(),
        "Spatial Context Detection", false);

    List<String> itemsContextType = Arrays.asList("Polygonal Detection");
    final JComboBox contextTypeComboBox = inputDialog.addComboBox(
        "Urban Detection Type", "Urban Detection Type", itemsContextType,
        "Urban Detection Type");

    inputDialog
        .addLabel("-----------------------------------------------------------------------------------");
    // Une combobox pour charger le jeu de données servant à la délimitation
    final JComboBox layerComboBox = inputDialog.addLayerComboBox(
        "Road network", // nom de la variable
        context.getLayerNamePanel().getLayerManager().getLayer(0),// un layer
                                                                  // initial
        context.getLayerNamePanel().getLayerManager()); // layer manager

    inputDialog
        .addLabel("-----------------------------------------------------------------------------------");
    final JLabel zoneLabel = inputDialog
        .addLabel("Parameters - Polygonal Detection");
    final JTextField supMaxBlockBox = inputDialog.addDoubleField(
        "Blocks maximum area (sq.m) : ", 200000, 10, "Blocks area");
    final JTextField supMinZoneBox = inputDialog.addDoubleField(
        "Urban zone minimal area (sq.m) : ", 400000, 10, "Urban zone area");
    final JTextField OFBox = inputDialog.addDoubleField(
        "Opening-Closing distance (m) : ", 20, 5, "Opening-Closing");
    final JTextField compaBox = inputDialog.addDoubleField(
        "Compacity Degree (0 to 1) : ", 0.2, 5, "Compacity degree");
    inputDialog
        .addLabel("-----------------------------------------------------------------------------------");
    // Bouton traitement
    JButton processingButton = new JButton("OK");
    inputDialog.addRow("Detection", new JLabel("Detection"), processingButton,
        null, "Detection");
    inputDialog
        .addLabel("-----------------------------------------------------------------------------------");
    // Bouton visualisation
    JButton visuCartoButton = new JButton("OK");
    inputDialog.addRow("Cartography", new JLabel("Cartography"),
        visuCartoButton, null, "Cartography");
    inputDialog
        .addLabel("-----------------------------------------------------------------------------------");

    // condition d'affichage selon le type de contexte spatial à détecter
    contextTypeComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (contextTypeComboBox.getSelectedItem().equals("Polygonal Detection")) {
          zoneLabel.setEnabled(true);
          supMaxBlockBox.setEnabled(true);
          supMinZoneBox.setEnabled(true);
          OFBox.setEnabled(true);
          compaBox.setEnabled(true);
        }
      }
    });

    layerComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

      }
    });

    // traitement appariement et comparaison
    processingButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {

        if (contextTypeComboBox.getSelectedItem().equals("Polygonal Detection")) {

          // sélection du jeu de données
          Layer layerRoads = (Layer) layerComboBox.getSelectedItem();
          IFeatureCollection<IFeature> jddRoads = UtilJump.convertSelection(
              context, layerRoads);

          double tailleBlocks = inputDialog
              .getDouble("Blocks maximum area (sq.m) : ");
          System.out.println(tailleBlocks);
          double tailleZone = inputDialog
              .getDouble("Urban zone minimal area (sq.m) : ");
          System.out.println(tailleZone);
          double seuilCompacite = inputDialog
              .getDouble("Compacity Degree (0 to 1) : ");
          System.out.println(seuilCompacite);
          double seuilBuffer = inputDialog
              .getDouble("Opening-Closing distance (m) : ");
          System.out.println(seuilBuffer);

          UrbanAreaZone urbanArea = new UrbanAreaZone();
          urbanArea.setTailleBlocks(tailleBlocks);
          urbanArea.setTailleZone(tailleZone);
          urbanArea.setSeuilCompacite(seuilCompacite);
          urbanArea.setSeuilBuffer(seuilBuffer);

          urbanArea.createAreasFromRoads(jddRoads);

          jddIn = jddRoads;
          jddOut = urbanArea.getJddUrbanArea();
        }

      }
    });

    // Visualisation des objets comparés
    visuCartoButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        // Initiatlisation de la fenêtre
        Task taskCarte = new Task();
        taskCarte.setName("Spatial context cartography");
        context.getWorkbenchFrame().addTaskFrame(taskCarte);
        // Affichage des objets
        UtilJump.afficheCollection(context, jddIn, "Input datasets",
            "Road network", Color.red);
        UtilJump.afficheCollection(context, jddOut, "Output datasets",
            "Urban Area", Color.green);

      }
    });

    // Centre et rend visible la fenêtre
    GUIUtil.centreOnWindow(inputDialog);
    inputDialog.setVisible(true);
    return inputDialog;
  }

}
