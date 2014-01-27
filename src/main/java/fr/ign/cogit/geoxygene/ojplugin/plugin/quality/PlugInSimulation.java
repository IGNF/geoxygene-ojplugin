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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

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
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.contrib.quality.estim.digitizing.LsCoordErrorSimulation;
import fr.ign.cogit.geoxygene.contrib.quality.estim.digitizing.LsCoordErrorSimulationAngles;
import fr.ign.cogit.geoxygene.contrib.quality.estim.digitizing.LsDistanceErrorSimulation;
import fr.ign.cogit.geoxygene.contrib.quality.estim.digitizing.LsDistanceErrorSimulationCorrelation;
import fr.ign.cogit.geoxygene.contrib.quality.estim.digitizing.LsDistanceErrorSimulationCorrelationAngles;
import fr.ign.cogit.geoxygene.contrib.quality.estim.digitizing.PgDistanceErrorSimulation;
import fr.ign.cogit.geoxygene.contrib.quality.util.DisplayChart;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.algo.MathUtil;

/**
 * A plugin to simulate digitizing error on polygons and linestrings according
 * to different methods
 * @author JFGirres
 * 
 */
public class PlugInSimulation extends FeatureInfoPlugIn {

  static Logger logger = Logger.getLogger(PlugInSimulation.class.getName());
  public static MultiInputDialog inputDialog;
  public static IFeatureCollection<IFeature> jddSimule = new FT_FeatureCollection<IFeature>();
  public static IFeatureCollection<IFeature> jddInitial = new FT_FeatureCollection<IFeature>();
  public static List<Double> listAbsoluteMeasureDifference = new ArrayList<Double>();
  public static String typeGeom;

  /** initialisation du menu parametrage */
  public void initialize(PlugInContext context) throws Exception {
    
      FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
      featureInstaller.addMainMenuPlugin(this, new String[] { MenuNames.PLUGINS, "GeOxygene", "Spatial Data Quality",
              "Util" }, "Digitizing Error Simulation", false, null, createEnableCheck(context.getWorkbenchContext()));
      
      /*EnableCheckFactory factory = new EnableCheckFactory(
        context.getWorkbenchContext());
    context.getFeatureInstaller().addMainMenuItem(
        this,
        new String[] { "GeOxygene", "Spatial Data Quality", "Util" },
        "Digitizing Error Simulation",
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
        "Simulation d'erreurs", false);

    // Une combobox permettant de choisir le jdd à bruiter
    inputDialog.addLayerComboBox("Jeu de données à bruiter", context
        .getLayerNamePanel().getLayerManager().getLayer(0), context
        .getLayerNamePanel().getLayerManager());
    inputDialog
        .addLabel("--------------------------------------------------------------------------------------------------------------------------------");

    // Création d'une liste avec les valeurs prises par le menu déroulant du
    // choix de méthode de bruitage
    List<String> items = Arrays.asList(
        "LineString : Bruitage selon une distance",
        "LineString : Bruitage selon une distance (avec correlation)",
        "LineString : Bruitage selon une distance (avec correlation+angle)",
        "LineString : Bruitage selon une précision en XY",
        "LineString : Bruitage selon une précision en XY (avec angle)",
        "Polygone : Bruitage selon une distance",
        "Polygone : Bruitage selon une distance (avec correlation+angle)");
    final JComboBox noiseComboBox = inputDialog.addComboBox(
        "Choix de la méthode de bruitage", "Choix de la méthode de bruitage",
        items, "Choix de la méthode de bruitage");
    inputDialog
        .addLabel("--------------------------------------------------------------------------------------------------------------------------------");
    inputDialog.addLabel("Paramétrage de la loi normale");
    final JTextField ecartTypeBox = inputDialog.addDoubleField("Ecart-type",
        0.2, 5, "Ecart-type");
    final JTextField moyenneBox = inputDialog.addDoubleField("Moyenne", 0, 5,
        "Moyenne");

    inputDialog
        .addLabel("--------------------------------------------------------------------------------------------------------------------------------");

    final JTextField nbSimuBox = inputDialog.addIntegerField(
        "Nombre de simulations", 1, 5, "Moyenne");
    // Bouton validation
    JButton validationButton = new JButton("OK");
    inputDialog.addRow("Bruitage du jeu de données", new JLabel(
        "Bruitage du jeu de données"), validationButton, null,
        "Valider les noeuds et arcs sélectionnés");

    // Bouton permettant d'afficher l'histogramme
    final JButton visuHistoButton = new JButton("Ok");
    inputDialog.addRow("Histogramme", new JLabel("Histogramme"),
        visuHistoButton, null, "Histogramme");
    visuHistoButton.setEnabled(false);

    inputDialog
        .addLabel("--------------------------------------------------------------------------------------------------------------------------------");

    // Bouton validation
    validationButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {

        jddInitial.clear();
        jddSimule.clear();
        listAbsoluteMeasureDifference.clear();

        Layer layerABruiter = (Layer) inputDialog.getComboBox(
            "Jeu de données à bruiter").getSelectedItem();
        jddInitial = UtilJump.convertSelection(context, layerABruiter);

        double ecartType = inputDialog.getDouble("Ecart-type");
        double moyenne = inputDialog.getDouble("Moyenne");
        String choixMethode = (String) inputDialog.getComboBox(
            "Choix de la méthode de bruitage").getSelectedItem();

        int nbIterations = inputDialog.getInteger("Nombre de simulations");

        if (nbIterations == 0) {
          System.out.println("SIMULATIONS = 0 !!!!");
        }

        else {

          logger.info("Début Simulation");

          if (choixMethode.equals("LineString : Bruitage selon une distance")) {
            typeGeom = "LineString";
            boolean simulate = true;
            for (IFeature ft : jddInitial) {
              if (ft.getGeom().isPolygon() || ft.getGeom().isMultiSurface()) {
                simulate = false;
              }
            }
            if (simulate == true) {
              CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(jddInitial);
              for (int i = 0; i < nbIterations; i++) {
                LsDistanceErrorSimulation simu = new LsDistanceErrorSimulation();
                simu.setCarteTopoIn(carteTopo);
                simu.setEcartType(ecartType);
                simu.setMoyenne(moyenne);
                simu.executeSimulation();
                jddSimule.addAll(simu.getJddOut());
                double lengthDifference = simu.getJddOut().getGeomAggregate()
                    .length()
                    - jddInitial.getGeomAggregate().length();
                double lengthDifferenceRelative = (lengthDifference / jddInitial
                    .getGeomAggregate().length()) * 100;
                listAbsoluteMeasureDifference.add(lengthDifferenceRelative);
              }
            }
          }
          if (choixMethode
              .equals("LineString : Bruitage selon une distance (avec correlation)")) {
            typeGeom = "LineString";
            boolean simulate = true;
            for (IFeature ft : jddInitial) {
              if (ft.getGeom().isPolygon() || ft.getGeom().isMultiSurface()) {
                simulate = false;
              }
            }
            if (simulate == true) {
              CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(jddInitial);
              for (int i = 0; i < nbIterations; i++) {
                LsDistanceErrorSimulationCorrelation simu = new LsDistanceErrorSimulationCorrelation();
                simu.setCarteTopoIn(carteTopo);
                simu.setEcartType(ecartType);
                simu.setMoyenne(moyenne);
                simu.executeSimulation();
                jddSimule.addAll(simu.getJddOut());
                double lengthDifference = simu.getJddOut().getGeomAggregate()
                    .length()
                    - jddInitial.getGeomAggregate().length();
                listAbsoluteMeasureDifference.add(lengthDifference);
              }
            }
          }
          if (choixMethode
              .equals("LineString : Bruitage selon une distance (avec correlation+angle)")) {
            typeGeom = "LineString";
            boolean simulate = true;
            for (IFeature ft : jddInitial) {
              if (ft.getGeom().isPolygon() || ft.getGeom().isMultiSurface()) {
                simulate = false;
              }
            }
            if (simulate == true) {
              CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(jddInitial);
              for (int i = 0; i < nbIterations; i++) {
                LsDistanceErrorSimulationCorrelationAngles simu = new LsDistanceErrorSimulationCorrelationAngles();
                simu.setCarteTopoIn(carteTopo);
                simu.setEcartType(ecartType);
                simu.setMoyenne(moyenne);
                simu.executeSimulation();
                jddSimule.addAll(simu.getJddOut());
                double lengthDifference = simu.getJddOut().getGeomAggregate()
                    .length()
                    - jddInitial.getGeomAggregate().length();
                listAbsoluteMeasureDifference.add(lengthDifference);
              }
            }
          }
          if (choixMethode
              .equals("LineString : Bruitage selon une précision en XY")) {
            typeGeom = "LineString";
            boolean simulate = true;
            for (IFeature ft : jddInitial) {
              if (ft.getGeom().isPolygon() || ft.getGeom().isMultiSurface()) {
                simulate = false;
              }
            }
            if (simulate == true) {
              CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(jddInitial);
              for (int i = 0; i < nbIterations; i++) {
                LsCoordErrorSimulation simu = new LsCoordErrorSimulation();
                simu.setCarteTopoIn(carteTopo);
                simu.setEcartType(ecartType);
                simu.setMoyenne(moyenne);
                simu.executeSimulation();
                jddSimule.addAll(simu.getJddOut());
                double lengthDifference = simu.getJddOut().getGeomAggregate()
                    .length()
                    - jddInitial.getGeomAggregate().length();
                listAbsoluteMeasureDifference.add(lengthDifference);
              }
            }
          }
          if (choixMethode
              .equals("LineString : Bruitage selon une précision en XY (avec angle)")) {
            typeGeom = "LineString";
            boolean simulate = true;
            for (IFeature ft : jddInitial) {
              if (ft.getGeom().isPolygon() || ft.getGeom().isMultiSurface()) {
                simulate = false;
              }
            }
            if (simulate == true) {
              CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(jddInitial);
              carteTopo.filtreNoeudsSimples();
              for (int i = 0; i < nbIterations; i++) {
                LsCoordErrorSimulationAngles simu = new LsCoordErrorSimulationAngles();
                simu.setCarteTopoIn(carteTopo);
                simu.setEcartType(ecartType);
                simu.setMoyenne(0);
                simu.executeSimulation();
                jddSimule.addAll(simu.getJddOut());
                double lengthDifference = simu.getJddOut().getGeomAggregate()
                    .length()
                    - jddInitial.getGeomAggregate().length();
                listAbsoluteMeasureDifference.add(lengthDifference);
              }
            }
          }
          if (choixMethode.equals("Polygone : Bruitage selon une distance")) {
            typeGeom = "Polygon";
            boolean simulate = true;
            for (IFeature ft : jddInitial) {
              if (ft.getGeom().isLineString() || ft.getGeom().isMultiCurve()) {
                simulate = false;
              }
            }
            if (simulate == true) {
              IFeatureCollection<IFeature> jddABruiter = new FT_FeatureCollection<IFeature>();
              jddABruiter.addAll(jddInitial);
              for (int i = 0; i < nbIterations; i++) {
                PgDistanceErrorSimulation simu = new PgDistanceErrorSimulation();
                simu.setJddIn(jddABruiter);
                simu.setEcartType(ecartType);
                simu.setMoyenne(moyenne);
                simu.executeSimulation();
                jddSimule.addAll(simu.getJddOut());
                double areaDifference = simu.getJddOut().getGeomAggregate()
                    .area()
                    - jddInitial.getGeomAggregate().area();
                listAbsoluteMeasureDifference.add(areaDifference);
              }
            }
          }

          if (choixMethode
              .equals("Polygone : Bruitage selon une distance (avec correlation+angle)")) {
            typeGeom = "Polygon";
            boolean simulate = true;
            for (IFeature ft : jddInitial) {
              if (ft.getGeom().isLineString() || ft.getGeom().isMultiCurve()) {
                simulate = false;
              }
            }
            if (simulate == true) {
              IFeatureCollection<IFeature> jddABruiter = new FT_FeatureCollection<IFeature>();
              jddABruiter.addAll(jddInitial);

              IFeatureCollection<IFeature> jddInitialLs = new FT_FeatureCollection<IFeature>();
              for (IFeature feature : jddInitial) {
                if (feature.getGeom().isPolygon()) {
                  jddInitialLs.add(new DefaultFeature(new GM_LineString(feature
                      .getGeom().coord())));
                }
                if (feature.getGeom().isMultiCurve()) {
                  @SuppressWarnings("unchecked")
                  IMultiSurface<IPolygon> multiPoly = (IMultiSurface<IPolygon>) feature
                      .getGeom();
                  for (IPolygon poly : multiPoly.getList()) {
                    jddInitialLs.add(new DefaultFeature(new GM_LineString(poly
                        .coord())));
                  }
                }
              }
              CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(jddInitialLs);
              carteTopo.filtreNoeudsSimples();

              for (int i = 0; i < nbIterations; i++) {
                LsDistanceErrorSimulationCorrelationAngles simu = new LsDistanceErrorSimulationCorrelationAngles();
                simu.setCarteTopoIn(carteTopo);
                simu.setEcartType(ecartType);
                simu.setMoyenne(moyenne);
                simu.executeSimulation();
                CarteTopo carteTopoSimu = CarteTopoFactory.newCarteTopo(simu
                    .getJddOut());
                IPopulation<Face> popFacesPoly = carteTopoSimu.getPopFaces();
                IFeatureCollection<IFeature> jddPolySimule = new FT_FeatureCollection<IFeature>();
                for (Face face : popFacesPoly) {
                  IPolygon poly = (IPolygon) face.getGeom();
                  jddPolySimule.add(new DefaultFeature(poly));
                }
                // Appariement maison
                for (int j = 0; j < jddInitial.size(); j++) {
                  IDirectPosition centrePolyRef = jddInitial.get(j).getGeom()
                      .centroid();
                  double distanceCentreMin = Double.MAX_VALUE;
                  int idPolyComp = 0;
                  for (int k = 0; k < jddPolySimule.size(); k++) {
                    IDirectPosition centrePolyComp = jddPolySimule.get(k)
                        .getGeom().centroid();
                    double distanceCentres = centrePolyComp
                        .distance2D(centrePolyRef);
                    if (distanceCentres < distanceCentreMin) {
                      distanceCentreMin = distanceCentres;
                      idPolyComp = k;
                    }
                  }
                  double areaDifference = jddPolySimule.get(idPolyComp)
                      .getGeom().area()
                      - jddInitial.get(j).getGeom().area();
                  listAbsoluteMeasureDifference.add(areaDifference);
                }
                jddSimule.addAll(jddPolySimule);
              }
            }
          }
          logger.info("Fin Simulation");
          visuHistoButton.setEnabled(true);
        }

        // Initiatlisation de la fenêtre cartographique
        Task taskCarte = new Task();
        taskCarte.setName("Simulation d'erreurs");
        context.getWorkbenchFrame().addTaskFrame(taskCarte);

        // Affichage
        UtilJump.afficheCollection(context, jddInitial, "Jeu initial", "Arcs",
            Color.red);
        UtilJump.afficheCollection(context, jddSimule, "Jeu simulé", "Arcs",
            Color.blue);

      }

    });

    // Bouton histogramme
    visuHistoButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (typeGeom.equals("Polygon")) {
          DisplayChart.histogramChart(listAbsoluteMeasureDifference,
              "Area Differences (Initial area = "
                  + jddInitial.getGeomAggregate().area() + " m²)");
          System.out.println("ECART-TYPE = "
              + MathUtil.ecartType(listAbsoluteMeasureDifference));
          System.out.println("MOYENNE = "
              + MathUtil.moyenne(listAbsoluteMeasureDifference));
        }

        else {
          DisplayChart.histogramChart(listAbsoluteMeasureDifference,
              "Length Differences (Initial length = "
                  + jddInitial.getGeomAggregate().length() + " m)");
          System.out.println("ECART-TYPE = "
              + MathUtil.ecartType(listAbsoluteMeasureDifference));
          System.out.println("MOYENNE = "
              + MathUtil.moyenne(listAbsoluteMeasureDifference));
        }
      }
    });

    // Centre et rend visible la fenêtre
    GUIUtil.centreOnWindow(inputDialog);
    inputDialog.setVisible(true);
    return inputDialog;
  }
}
