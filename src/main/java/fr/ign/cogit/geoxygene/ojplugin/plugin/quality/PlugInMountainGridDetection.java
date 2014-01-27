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
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.contrib.quality.estim.spatialcontext.grid.Contour;
import fr.ign.cogit.geoxygene.contrib.quality.estim.spatialcontext.grid.MountainAreaGrid;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;

/**
 * A plugin to facilitate the delineation of mountainous areas
 * @author JFGirres
 * 
 */
public class PlugInMountainGridDetection extends FeatureInfoPlugIn {

  public static MultiInputDialog inputDialog;
  public static IFeatureCollection<IFeature> jddIn = new FT_FeatureCollection<IFeature>();
  public static IFeatureCollection<IFeature> jddOut = new FT_FeatureCollection<IFeature>();
  public static IFeatureCollection<IFeature> jddOutFilter = new FT_FeatureCollection<IFeature>();

  /**
   * initialisation du menu parametrage
   * */
  public void initialize(PlugInContext context) throws Exception {
      FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
      featureInstaller.addMainMenuPlugin(this, new String[] { MenuNames.PLUGINS, "GeOxygene", "Spatial Data Quality",
              "Spatial Context" }, "Mountains Detection", false, null, createEnableCheck(context.getWorkbenchContext()));
      
    /*EnableCheckFactory factory = new EnableCheckFactory(
        context.getWorkbenchContext());
    context.getFeatureInstaller()
        .addMainMenuItem(
            this,
            new String[] { "GeOxygene", "Spatial Data Quality",
                "Spatial Context" },
            "Mountains Detection",
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

    List<String> itemsContextType = Arrays.asList("Grid Detection");
    final JComboBox contextTypeComboBox = inputDialog.addComboBox(
        "Mountain Detection Type", "Mountain Detection Type", itemsContextType,
        "Mountain Detection Type");

    inputDialog
        .addLabel("-----------------------------------------------------------------------------------");
    // Une combobox pour charger le jeu de données servant à la délimitation
    final JComboBox layerComboBox = inputDialog.addLayerComboBox("Contour",
        context.getLayerNamePanel().getLayerManager().getLayer(0), context
            .getLayerNamePanel().getLayerManager());
    final JTextField altitudeLabel = inputDialog.addTextField("Altitude Field",
        "ALTITUDE", 15, null, "Champs altitude");
    inputDialog
        .addLabel("-----------------------------------------------------------------------------------");
    final JLabel gridLabel = inputDialog.addLabel("Parameters - Grid Approach");
    final JTextField cellBox = inputDialog.addIntegerField("Cell size = ", 500,
        5, "Cell size");
    final JTextField stepBox = inputDialog.addIntegerField(
        "Search radius size = ", 500, 5, "Search radius size");
    final JTextField seuilHautBox = inputDialog.addDoubleField("Threshold = ",
        200, 5, "Threshold");
    final JTextField tailleMinZoneBox = inputDialog.addDoubleField(
        "Mountain zone minimal area (sq.m) : ", 4000000, 10,
        "Mountain zone minimal area");
    List<String> itemsBoucheTrou = Arrays.asList("Yes", "No");
    final JComboBox boucheTrousComboBox = inputDialog.addComboBox(
        "Eliminate holes ?", "Eliminate holes", itemsBoucheTrou,
        "Eliminate holes");

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
        if (contextTypeComboBox.getSelectedItem().equals("Grid Detection")) {
          gridLabel.setEnabled(true);
          cellBox.setEnabled(true);
          stepBox.setEnabled(true);
          seuilHautBox.setEnabled(true);
          tailleMinZoneBox.setEnabled(true);
          boucheTrousComboBox.setEnabled(true);
        } else {
          gridLabel.setEnabled(false);
          cellBox.setEnabled(false);
          stepBox.setEnabled(false);
          seuilHautBox.setEnabled(false);
          tailleMinZoneBox.setEnabled(false);
          boucheTrousComboBox.setEnabled(false);
        }
      }
    });

    layerComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

      }
    });

    // traitement
    processingButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {

        if (contextTypeComboBox.getSelectedItem().equals("Grid Detection")) {

          // sélection du jeu de données
          Layer layerCourbesDeNiveau = (Layer) layerComboBox.getSelectedItem();
          IFeatureCollection<IFeature> jddCourbes = UtilJump.convertSelection(
              context, layerCourbesDeNiveau);
          IFeatureCollection<Contour> jddCourbesDeNiveau = new FT_FeatureCollection<Contour>();

          // Affectation de l'altitude à chaque courbe
          String champsAlti = inputDialog.getText("Altitude Field");
          for (IFeature feature : jddCourbes) {
            ILineString lsCourbe = null;
            if (feature.getGeom().isMultiCurve()) {
              IMultiCurve<IOrientableCurve> multiCourbe = (IMultiCurve<IOrientableCurve>) feature
                  .getGeom();
              for (IOrientableCurve ls : multiCourbe.getList()) {
                lsCourbe = (ILineString) ls;
                Contour courbe = new Contour(lsCourbe, ((Double) feature
                    .getAttribute(champsAlti)).intValue());
                jddCourbesDeNiveau.add(courbe);
              }
            } else {
              lsCourbe = (ILineString) feature.getGeom();
              Contour courbe = new Contour(lsCourbe, ((Double) feature
                  .getAttribute(champsAlti)).intValue());
              jddCourbesDeNiveau.add(courbe);
            }
          }

          int tailleCellule = inputDialog.getInteger("Cell size = ");
          int rayon = inputDialog.getInteger("Search radius size = ");

          double seuilBasRatio = 0;
          double seuilHautRatio = inputDialog.getDouble("Threshold = ");

          MountainAreaGrid mountainGrid = new MountainAreaGrid(
              jddCourbesDeNiveau);
          mountainGrid.setTailleCellule(tailleCellule);
          mountainGrid.setRayon(rayon);
          mountainGrid.setSeuilBasRatio(seuilBasRatio);
          mountainGrid.setSeuilHautRatio(seuilHautRatio);
          mountainGrid.createArea();

          double superficieMin = inputDialog
              .getDouble("Mountain zone minimal area (sq.m) : ");

          if (boucheTrousComboBox.getSelectedItem().equals("No"))
            mountainGrid.setBoucheTrou(false);
          else
            mountainGrid.setBoucheTrou(true);
          mountainGrid.setSuperficieMin(superficieMin);
          mountainGrid.filterArea();

          jddIn = jddCourbes;
          jddOut = mountainGrid.getJddMoutainArea();
          jddOutFilter = mountainGrid.getJddFilterMoutainArea();
          System.out.println(jddOutFilter.size());

        } else {

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
            "Jeu de données initial", Color.red);
        UtilJump.afficheCollection(context, jddOut, "Output datasets",
            "Zone brute", Color.green);
        if (!jddOutFilter.isEmpty()) {
          UtilJump.afficheCollection(context, jddOutFilter, "Output datasets",
              "Zone brute", Color.blue);
        }
      }
    });

    // Centre et rend visible la fenêtre
    GUIUtil.centreOnWindow(inputDialog);
    inputDialog.setVisible(true);
    return inputDialog;
  }

}
