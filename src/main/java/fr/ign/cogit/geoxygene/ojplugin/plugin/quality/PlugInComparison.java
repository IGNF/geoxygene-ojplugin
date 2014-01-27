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
import fr.ign.cogit.geoxygene.contrib.quality.comparison.LineStringComparison;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.PolygonComparison;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.cutting.SectionsCutting;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.cutting.ExtremityCutting;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.measure.AreaDifference;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.measure.AreaDifferenceRel;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.measure.LengthDifference;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.measure.LengthDifferenceRel;
import fr.ign.cogit.geoxygene.contrib.quality.comparison.measure.Measure;
import fr.ign.cogit.geoxygene.contrib.quality.util.DisplayChart;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;

/**
 * A plugin to compute differences between datasets. Automatic or manuel
 * matching are provided for polygons and linestrings. Different methods to
 * normalize linestring comparisons are also proposed (extremity cutting,
 * section cutting)
 * 
 * @author JFGirres
 * 
 */
public class PlugInComparison extends FeatureInfoPlugIn {

    public static MultiInputDialog inputDialog;
    public static List<String> listMeasure = new ArrayList<String>();
    public static List<Double> listLengthDifference = new ArrayList<Double>();
    public static List<Double> listLength = new ArrayList<Double>();
    public static List<Double> listLengthDifferenceRel = new ArrayList<Double>();
    public static List<Double> listHausdorffDistance = new ArrayList<Double>();
    public static List<Double> listMeanDistance = new ArrayList<Double>();
    public static List<Double> listGranularity = new ArrayList<Double>();
    public static List<Double> listAreaDifference = new ArrayList<Double>();
    public static List<Double> listAreaDifferenceRel = new ArrayList<Double>();
    public static List<Double> listArea = new ArrayList<Double>();
    public static List<Double> listSurfaceDistance = new ArrayList<Double>();
    public static IFeatureCollection<IFeature> jddRef = new FT_FeatureCollection<IFeature>();
    public static IFeatureCollection<IFeature> jddComp = new FT_FeatureCollection<IFeature>();
    public static IFeatureCollection<IFeature> jddRefOut = new FT_FeatureCollection<IFeature>();
    public static IFeatureCollection<IFeature> jddCompOut = new FT_FeatureCollection<IFeature>();
    public static IFeatureCollection<IFeature> jddPointRefOut = new FT_FeatureCollection<IFeature>();
    public static IFeatureCollection<IFeature> jddPointCompOut = new FT_FeatureCollection<IFeature>();

    /**
     * initialisation du menu parametrage
     * */
    public void initialize(PlugInContext context) throws Exception {
      System.out.println("------------------------------");
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
        featureInstaller.addMainMenuPlugin(this, new String[] { MenuNames.PLUGINS, "GeOxygene", "Spatial Data Quality",
                "Comparison" }, "Comparison", false, null, createEnableCheck(context.getWorkbenchContext()));
        /*
         * EnableCheckFactory factory = new EnableCheckFactory(
         * context.getWorkbenchContext());
         * context.getFeatureInstaller().addMainMenuItem( this, new String[] {
         * "GeOxygene", "Spatial Data Quality", "Comparison" }, "Comparison",
         * false, null, new MultiEnableCheck().add(factory
         * .createAtLeastNLayersMustExistCheck(2)));
         */
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
        inputDialog = new MultiInputDialog(context.getWorkbenchFrame(), "Comparaison de jeux de données", false);
        // Deux combobox permettant de choisir les jeux de données de référence
        // et à
        // comparer
        inputDialog.addLayerComboBox("Jeu de données de référence", context.getLayerNamePanel().getLayerManager()
                .getLayer(0), context.getLayerNamePanel().getLayerManager()); // layer
                                                                              // manager
        inputDialog.addLayerComboBox("Jeu de données à comparer", context.getLayerNamePanel().getLayerManager()
                .getLayer(1), context.getLayerNamePanel().getLayerManager());

        inputDialog.addLabel("---------------------------------------------------------------------------");

        List<String> itemsGeom = Arrays.asList("Polygone", "Polyligne");
        final JComboBox geomComboBox = inputDialog.addComboBox("Type de géométrie à comparer",
                "Type de géométrie à comparer", itemsGeom, "Type de géométrie à comparer");

        List<String> itemsMatching = Arrays.asList("Automatique", "Manuel");
        final JComboBox matchingComboBox = inputDialog.addComboBox("Type d'appariement", "Type d'appariement",
                itemsMatching, "Type d'appariement");

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");
        List<String> itemsCut = Arrays.asList("Pas de découpage", "Découpage aux extrémités", "Découpage en tronçons");
        final JComboBox cuttingComboBox = inputDialog.addComboBox("Méthode de découpage", "Méthode de découpage",
                itemsCut, "Méthode de découpage");
        cuttingComboBox.setEnabled(false);
        final JTextField stepBox = inputDialog.addDoubleField("Pas", 200, 5, "Pas");
        stepBox.setEnabled(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");
        // Bouton appariement et comparaison
        JButton processingButton = new JButton("OK");
        inputDialog.addRow("Appariement et comparaison des jeux de données", new JLabel(
                "Appariement et comparaison des jeux de données"), processingButton, null,
                "Appariement et comparaison des jeux de données");

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");
        List<String> itemsHisto = Arrays.asList("Histogramme des erreurs", "Relations entre erreurs");
        final JComboBox histoComboBox = inputDialog.addComboBox("Type de graphique", "Type de graphique", itemsHisto,
                "Type de graphique");
        histoComboBox.setEnabled(false);
        // Bouton permettant d'afficher l'histogramme
        final JButton visuHistoButton = new JButton("Ok");
        inputDialog.addRow("Histogramme", new JLabel("Histogramme"), visuHistoButton, null, "Histogramme");
        visuHistoButton.setEnabled(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");
        // Bouton permettant d'afficher les objets comparés
        final JButton visuCartoButton = new JButton("Ok");
        inputDialog.addRow("Cartographie des objets traités", new JLabel("Cartographie des objets traités"),
                visuCartoButton, null, "Cartographie des objets traités");
        visuCartoButton.setEnabled(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        // condition d'affichage des options spécifiques aux polylignes
        geomComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (geomComboBox.getSelectedItem().equals("Polyligne")) {
                    cuttingComboBox.setEnabled(true);
                } else {
                    cuttingComboBox.setEnabled(false);
                    stepBox.setEnabled(false);
                }
            }
        });
        cuttingComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cuttingComboBox.getSelectedItem().equals("Découpage en tronçons")) {
                    stepBox.setEnabled(true);
                } else {
                    stepBox.setEnabled(false);
                }
            }
        });

        // traitement appariement et comparaison
        processingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

                if (!(listLengthDifference == null))
                    listLengthDifference.clear();
                if (!(listLengthDifferenceRel == null))
                    listLengthDifferenceRel.clear();
                if (!(listLength == null))
                    listLength.clear();
                if (!(listHausdorffDistance == null))
                    listHausdorffDistance.clear();
                if (!(listMeanDistance == null))
                    listMeanDistance.clear();
                if (!(listSurfaceDistance == null))
                    listSurfaceDistance.clear();
                if (!(listAreaDifference == null))
                    listAreaDifference.clear();
                if (!(listAreaDifferenceRel == null))
                    listAreaDifferenceRel.clear();
                if (!(listArea == null))
                    listArea.clear();
                if (!(listGranularity == null))
                    listGranularity.clear();
                if (!(jddCompOut == null))
                    jddCompOut.clear();
                if (!(jddRefOut == null))
                    jddRefOut.clear();
                if (!(jddPointCompOut == null))
                    jddPointCompOut.clear();
                if (!(jddPointRefOut == null))
                    jddPointRefOut.clear();

                Layer layerReference = (Layer) PlugInComparison.inputDialog.getComboBox("Jeu de données de référence")
                        .getSelectedItem();
                Layer layerAComparer = (Layer) PlugInComparison.inputDialog.getComboBox("Jeu de données à comparer")
                        .getSelectedItem();

                jddComp = UtilJump.convertSelection(context, layerAComparer);
                jddRef = UtilJump.convertSelection(context, layerReference);

                String typeGeom = (String) geomComboBox.getSelectedItem();
                String typeMatching = (String) matchingComboBox.getSelectedItem();
                String typeCut = (String) cuttingComboBox.getSelectedItem();

                List<Class<? extends Measure>> measures = new ArrayList<Class<? extends Measure>>();

                if (typeGeom.equals("Polygone")) {
                    measures.add(AreaDifference.class);
                    measures.add(AreaDifferenceRel.class);
                    PolygonComparison compaPg = new PolygonComparison(jddRef, jddComp, measures);
                    if (typeMatching.equals("Manuel"))
                        compaPg.setAutomaticMatching(false);
                    else
                        compaPg.setAutomaticMatching(true);
                    compaPg.executeComparison();
                    jddRefOut = compaPg.getJddRefOut();
                    jddCompOut = compaPg.getJddCompOut();

                    for (IFeature feature : jddCompOut) {
                        listArea.add(feature.getGeom().area());
                    }

                    listAreaDifference = compaPg.getMeasurements(AreaDifference.class);
                    listAreaDifferenceRel = compaPg.getMeasurements(AreaDifferenceRel.class);

                } else {

                    measures.add(LengthDifference.class);
                    measures.add(LengthDifferenceRel.class);

                    LineStringComparison compaLs = new LineStringComparison(jddRef, jddComp, measures);
                    if (typeMatching.equals("Manuel"))
                        compaLs.setAutomaticMatching(false);
                    else
                        compaLs.setAutomaticMatching(true);
                    if (typeCut.equals("Découpage aux extrémités"))
                        compaLs.setCuttingMethod(new ExtremityCutting());
                    if (typeCut.equals("Découpage en tronçons")) {
                        double pasTroncons = PlugInComparison.inputDialog.getDouble("Pas");
                        compaLs.setCuttingMethod(new SectionsCutting(pasTroncons));
                    }
                    compaLs.executeComparison();
                    jddRefOut = compaLs.getJddRefOut();
                    jddCompOut = compaLs.getJddCompOut();

                    System.out.println(jddRefOut.size());
                    System.out.println(jddCompOut.size());

                    for (IFeature feature : jddCompOut) {
                        listLength.add(feature.getGeom().length());
                    }

                    listLengthDifference = compaLs.getMeasurements(LengthDifference.class);
                    listLengthDifferenceRel = compaLs.getMeasurements(LengthDifferenceRel.class);
                }

                for (Class<? extends Measure> measureClass : measures) {
                    listMeasure.add(measureClass.toString());
                }

                histoComboBox.setEnabled(true);
                visuHistoButton.setEnabled(true);
                visuCartoButton.setEnabled(true);
            }
        });

        // Visualisation de l'histogramme
        visuHistoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String typeGeom = (String) geomComboBox.getSelectedItem();
                String typeHisto = (String) histoComboBox.getSelectedItem();

                if (typeGeom.equals("Polygone")) {
                    if (typeHisto.equals("Histogramme des erreurs")) {
                        DisplayChart.histogramChart(listAreaDifference, "Area Difference");
                        DisplayChart.histogramChart(listAreaDifferenceRel, "Relative Area Difference");

                    } else {
                        DisplayChart.xySerieChart(listAreaDifference, "Area Difference", listArea, "Area");
                    }
                } else {
                    if (typeHisto.equals("Histogramme des erreurs")) {

                        DisplayChart.histogramChart(listLengthDifference, "Length Difference");
                        DisplayChart.histogramChart(listLengthDifferenceRel, "Relative Length Difference");

                    } else {
                        DisplayChart.xySerieChart(listLengthDifference, "Length Difference", listLength, "Length");
                    }
                }

            }
        });

        // Visualisation des objets comparés
        visuCartoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Initiatlisation de la fenêtre
                Task taskCarte = new Task();
                taskCarte.setName("Datasets comparison");
                context.getWorkbenchFrame().addTaskFrame(taskCarte);
                // Affichage des objets
                UtilJump.afficheCollection(context, jddRef, "Input datasets", "JDD Ref", Color.red);
                UtilJump.afficheCollection(context, jddComp, "Input datasets", "JDD Comp", Color.blue);
                UtilJump.afficheCollection(context, jddRefOut, "Output datasets", "JDD Ref", Color.red);
                UtilJump.afficheCollection(context, jddCompOut, "Output datasets", "JDD Comp", Color.blue);
                UtilJump.afficheCollection(context, jddPointRefOut, "Output datasets", "JDD Ref Points", Color.red);
                UtilJump.afficheCollection(context, jddPointCompOut, "Output datasets", "JDD Comp Points", Color.blue);

            }
        });

        // Centre et rend visible la fenêtre
        GUIUtil.centreOnWindow(inputDialog);
        inputDialog.setVisible(true);
        return inputDialog;
    }

}
