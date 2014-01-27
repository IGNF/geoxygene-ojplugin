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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
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
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.delaunay.TriangulationJTS;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.quality.estim.digitizing.LsDistanceErrorSimulationCorrelationAngles;
import fr.ign.cogit.geoxygene.contrib.quality.estim.granularity.GranularityEvaluation;
import fr.ign.cogit.geoxygene.contrib.quality.estim.projection.LineStringProjectionImpact;
import fr.ign.cogit.geoxygene.contrib.quality.estim.projection.PolygonProjectionImpact;
import fr.ign.cogit.geoxygene.contrib.quality.estim.sinuosity.RoadSinuosityDetection;
import fr.ign.cogit.geoxygene.contrib.quality.estim.terrain.LengthTerrainError;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTM;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTMArea;
import fr.ign.cogit.geoxygene.sig3d.util.ColorShade;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_CubicSpline;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.util.algo.MathUtil;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

/**
 * The plugin developed of the EstIM model developed during the PhD of (Girres,
 * 2012). Allows the computation of different impacts (projection, terrain,
 * polygonal approximation, digitizing error, generalisation) for lengths and
 * areas.
 * 
 * @author JFGirres
 * 
 */
public class PlugInEstim extends FeatureInfoPlugIn {

    public static MultiInputDialog inputDialog;
    static Logger logger = Logger.getLogger(PlugInEstim.class.getName());

    public static String pathInputMnt;
    public static String pathInputDistortionGrid;
    public static String pathInputMountainZone;
    public static String pathInputUrbanZone;
    public static double projectionError;
    public static double terrainError;
    public static double curvatureError;
    public static double digitizingMeanError;
    public static double digitizingMinError;
    public static double digitizingMaxError;
    public static double digitizingStDevError;
    public static double generalisationMeanError;
    public static double generalisationMinError;
    public static double generalisationMaxError;
    public static String datasetName;
    public static double initialMeasure;
    public static String measureType;

    /**
     * initialisation du menu parametrage
     * */
    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
        featureInstaller.addMainMenuPlugin(this, new String[] { MenuNames.PLUGINS, "GeOxygene", "Spatial Data Quality",
                "Estimation" }, "EstIM Model", false, null, createEnableCheck(context.getWorkbenchContext()));
        
        /*EnableCheckFactory factory = new EnableCheckFactory(context.getWorkbenchContext());
        context.getFeatureInstaller().addMainMenuItem(this,
                new String[] { "GeOxygene", "Spatial Data Quality", "Estimation" }, "EstIM Model", false, null,
                new MultiEnableCheck().add(factory.createAtLeastNLayersMustExistCheck(1)));*/
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
        inputDialog = new MultiInputDialog(context.getWorkbenchFrame(), "EstIM Model", false);
        inputDialog.addLayerComboBox("Dataset to evaluate", context.getLayerNamePanel().getLayerManager().getLayer(0),
                context.getLayerNamePanel().getLayerManager());

        List<String> itemsMeasure = Arrays.asList("Area", "Length");
        final JComboBox measureComboBox = inputDialog.addComboBox("Measurement type", "Measurement type", itemsMeasure,
                "Measurement type");

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        final JCheckBox projectionCheckBox = inputDialog.addCheckBox("Projection impact", false);

        final JButton distortionGridButton = new JButton("...");
        final JLabel distortionGridLabel = new JLabel("Distortion grid");
        inputDialog.addRow("Distortion grid", distortionGridLabel, distortionGridButton, null,
                "Select the Distortion Grid path (.shp)");
        final JTextField distortionGridPathTxtBox = new JTextField("", 30);
        final JLabel distortionGridPathLabel = new JLabel("Distortion Grid path");
        inputDialog.addRow("Distortion Grid Path", distortionGridPathLabel, distortionGridPathTxtBox, null,
                "Distortion Grid Path");
        distortionGridLabel.setEnabled(false);
        distortionGridButton.setEnabled(false);
        distortionGridPathLabel.setEnabled(false);
        distortionGridPathTxtBox.setEditable(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        final JCheckBox terrainCheckBox = inputDialog.addCheckBox("Terrain impact", false);

        final JButton dtmButton = new JButton("...");
        final JLabel dtmLabel = new JLabel("Digital Terrain Model");
        inputDialog.addRow("Digital Terrain Model", dtmLabel, dtmButton, null, "Select the DTM path (.asc)");
        final JTextField dtmPathTxtBox = new JTextField("", 30);
        final JLabel dtmPathLabel = new JLabel("DTM path");
        inputDialog.addRow("DTM Path", dtmPathLabel, dtmPathTxtBox, null, "DTM Path");
        dtmLabel.setEnabled(false);
        dtmButton.setEnabled(false);
        dtmPathLabel.setEnabled(false);
        dtmPathTxtBox.setEditable(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        final JCheckBox curvatureCheckBox = inputDialog.addCheckBox("Polygonal Approximation impact", false);
        final JLabel sinuosityLabel = new JLabel("Sinuosity detection ?");
        final JComboBox sinuosityComboBox = new JComboBox();
        sinuosityComboBox.addItem("Yes");
        sinuosityComboBox.addItem("No");
        inputDialog.addRow("Sinuosity detection ?", sinuosityLabel, sinuosityComboBox, null, "Sinuosity detection ?");
        sinuosityComboBox.setEnabled(false);
        sinuosityLabel.setEnabled(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        final JCheckBox digitizingCheckBox = inputDialog.addCheckBox("Digitizing error impact", false);
        final JLabel scaleStatusLabel = new JLabel("Scale : ");
        final JComboBox scaleStatusComboBox = new JComboBox();
        scaleStatusComboBox.addItem("Unknown");
        scaleStatusComboBox.addItem("Known");
        inputDialog.addRow("Scale : ", scaleStatusLabel, scaleStatusComboBox, null, "Scale : ");
        final JTextField scaleCaptureTxtBox = new JTextField("50000", 10);
        final JLabel scaleCaptureLabel = new JLabel("Capture Scale");
        inputDialog.addRow("Capture Scale", scaleCaptureLabel, scaleCaptureTxtBox, null, "Capture Scale");
        scaleCaptureLabel.setEnabled(false);
        scaleCaptureTxtBox.setEnabled(false);
        scaleStatusComboBox.setEnabled(false);
        scaleStatusLabel.setEnabled(false);
        final JButton scaleEstimationButton = new JButton("Scale");
        final JLabel scaleEstimationLabel = new JLabel("Scale Estimation");
        inputDialog.addRow("Scale Estimation", scaleEstimationLabel, scaleEstimationButton, null, "Scale Estimation");
        scaleEstimationButton.setEnabled(false);
        scaleEstimationLabel.setEnabled(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        final JCheckBox generalisationCheckBox = inputDialog.addCheckBox("Generalisation impact", false);

        final JLabel scaleRepStatusLabel = new JLabel("Scale : ");
        final JComboBox scaleRepStatusComboBox = new JComboBox();
        scaleRepStatusComboBox.addItem("Unknown");
        scaleRepStatusComboBox.addItem("Known");
        inputDialog.addRow("Scale : ", scaleRepStatusLabel, scaleRepStatusComboBox, null, "Scale : ");
        scaleRepStatusLabel.setEnabled(false);
        scaleRepStatusComboBox.setEnabled(false);

        final JLabel scaleRepresentationLabel = new JLabel("Representation Scale");
        final JTextField scaleRepresentationTxtBox = new JTextField("50000", 10);
        inputDialog.addRow("Representation Scale", scaleRepresentationLabel, scaleRepresentationTxtBox, null,
                "Representation Scale");
        scaleRepresentationLabel.setEnabled(false);
        scaleRepresentationTxtBox.setEnabled(false);

        final JButton scaleRepEstimationButton = new JButton("Scale");
        final JLabel scaleRepEstimationLabel = new JLabel("Scale Estimation");
        inputDialog.addRow("Scale Estimation", scaleRepEstimationLabel, scaleRepEstimationButton, null,
                "Scale Estimation");
        scaleRepEstimationButton.setEnabled(false);
        scaleRepEstimationLabel.setEnabled(false);

        List<String> itemsObjectType = Arrays.asList("Roads", "Railways", "Rivers");
        final JLabel objectTypeLabel = new JLabel("Object type");
        // List<String> itemsPolyObjectType = Arrays.asList("Lakes",
        // "Administrative units", "Agricultural parcels");
        final JComboBox objectTypeComboBox = inputDialog.addComboBox("", objectTypeLabel, itemsObjectType,
                "Object type");
        inputDialog.addRow("", objectTypeLabel, objectTypeComboBox, null, "Object Type");
        objectTypeLabel.setEnabled(false);
        objectTypeComboBox.setEnabled(false);

        final JButton mountainButton = new JButton("...");
        final JLabel mountainLabel = new JLabel("Mountainous areas delineation");
        inputDialog.addRow("Mountainous areas delineation", mountainLabel, mountainButton, null,
                "Mountainous areas delineation");
        final JTextField mountainPathTxtBox = new JTextField("", 30);
        final JLabel mountainPathLabel = new JLabel("Mountainous areas path");
        inputDialog.addRow("Mountainous areas Path", mountainPathLabel, mountainPathTxtBox, null,
                "Mountainous areas Path");
        mountainLabel.setEnabled(false);
        mountainButton.setEnabled(false);
        mountainPathLabel.setEnabled(false);
        mountainPathTxtBox.setEditable(false);

        final JButton urbanButton = new JButton("...");
        final JLabel urbanLabel = new JLabel("Urban areas delineation");
        inputDialog.addRow("Urban areas delineation", urbanLabel, urbanButton, null, "Urban areas delineation");
        final JTextField urbanPathTxtBox = new JTextField("", 30);
        final JLabel urbanPathLabel = new JLabel("Urban areas path");
        inputDialog.addRow("Urban areas Path", urbanPathLabel, urbanPathTxtBox, null, "Urban areas Path");
        urbanLabel.setEnabled(false);
        urbanButton.setEnabled(false);
        urbanPathLabel.setEnabled(false);
        urbanPathTxtBox.setEditable(false);

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        final JLabel agregationLabel = new JLabel("Agregate Impacts : ");
        final JComboBox agregationComboBox = new JComboBox();
        agregationComboBox.addItem("Yes");
        agregationComboBox.addItem("No");
        inputDialog.addRow("Agregate Impacts : ", agregationLabel, agregationComboBox, null, "Agregate Impacts : ");

        final JButton processingButton = new JButton("OK");
        inputDialog.addRow("Estimation", new JLabel("Estimation"), processingButton, null, "Estimation");

        inputDialog
                .addLabel("-----------------------------------------------------------------------------------------------------------");

        // ///////////// Gestion des evenements sur le formulaire
        // ////////////////////////////////

        // Section : Projection Impact
        projectionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                distortionGridButton.setEnabled(projectionCheckBox.isSelected());
                distortionGridLabel.setEnabled(projectionCheckBox.isSelected());

                // selection du fichier mnt (au format asc)
                distortionGridButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // choix d'un fichier shape
                        JFileChooser choixFichierShape = new JFileChooser();
                        /*
                         * crée un filtre qui n'accepte que les fichier shp ou
                         * les répertoires
                         */
                        choixFichierShape.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(final File f) {
                                return (f.isFile() && (f.getAbsolutePath().endsWith(".shp") //$NON-NLS-1$
                                        || f.getAbsolutePath().endsWith(".SHP") //$NON-NLS-1$
                                        ) || f.isDirectory());
                            }

                            @Override
                            public String getDescription() {
                                return "Shapefile";
                            }
                        });

                        choixFichierShape.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        choixFichierShape.setMultiSelectionEnabled(false);
                        JFrame frame = new JFrame();
                        frame.setVisible(true);
                        int returnVal = choixFichierShape.showOpenDialog(frame);
                        frame.dispose();

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            pathInputDistortionGrid = choixFichierShape.getSelectedFile().getAbsolutePath();

                            distortionGridPathLabel.setEnabled(projectionCheckBox.isSelected());
                            distortionGridPathTxtBox.setText(pathInputDistortionGrid);
                        }
                        System.out.println("Input shape : " + pathInputDistortionGrid);
                    }
                });
            }
        });

        // Section : Terrain Impact
        terrainCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dtmButton.setEnabled(terrainCheckBox.isSelected());
                dtmLabel.setEnabled(terrainCheckBox.isSelected());

                // selection du fichier mnt (au format asc)
                dtmButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        JFileChooser choixFichierArcGrid = new JFileChooser();
                        /*
                         * crée un filtre qui n'accepte que les fichier asc ou
                         * les répertoires
                         */
                        choixFichierArcGrid.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(final File f) {
                                return (f.isFile() && (f.getAbsolutePath().endsWith(".asc") //$NON-NLS-1$
                                        || f.getAbsolutePath().endsWith(".ASC") //$NON-NLS-1$
                                        ) || f.isDirectory());
                            }

                            @Override
                            public String getDescription() {
                                return "Arc/Info ASCII Grid"; //$NON-NLS-1$
                            }
                        });
                        choixFichierArcGrid.setFileSelectionMode(JFileChooser.FILES_ONLY);

                        choixFichierArcGrid.setMultiSelectionEnabled(false);
                        JFrame frame = new JFrame();
                        frame = new JFrame();
                        frame.setVisible(true);
                        int returnVal = choixFichierArcGrid.showOpenDialog(frame);
                        frame.dispose();
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            pathInputMnt = choixFichierArcGrid.getSelectedFile().getAbsolutePath();

                            dtmPathLabel.setEnabled(terrainCheckBox.isSelected());
                            dtmPathTxtBox.setText(pathInputMnt);
                        }
                        System.out.println("Input DTM path : " + pathInputMnt);

                    }
                });
            }
        });

        // Section : Curvature Impact
        curvatureCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sinuosityComboBox.setEnabled(curvatureCheckBox.isSelected());
                sinuosityLabel.setEnabled(curvatureCheckBox.isSelected());
            }
        });

        // Section : Digitizing Impact
        digitizingCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaleStatusComboBox.setEnabled(digitizingCheckBox.isSelected());
                scaleStatusLabel.setEnabled(digitizingCheckBox.isSelected());

                if (!(digitizingCheckBox.isSelected())) {
                    scaleCaptureTxtBox.setEnabled(false);
                    scaleCaptureLabel.setEnabled(false);
                    scaleEstimationButton.setEnabled(false);
                    scaleEstimationLabel.setEnabled(false);
                }

                scaleStatusComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        if (scaleStatusComboBox.getSelectedItem().equals("Known")) {
                            scaleCaptureTxtBox.setEnabled(true);
                            scaleCaptureLabel.setEnabled(true);
                            scaleEstimationButton.setEnabled(false);
                            scaleEstimationLabel.setEnabled(false);
                        }
                        if (scaleStatusComboBox.getSelectedItem().equals("Unknown")) {
                            scaleCaptureTxtBox.setEnabled(false);
                            scaleCaptureLabel.setEnabled(false);
                            scaleEstimationButton.setEnabled(true);
                            scaleEstimationLabel.setEnabled(true);
                        }
                    }
                });
            }
        });

        // Section : Generalisation Impact
        generalisationCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectTypeComboBox.setEnabled(generalisationCheckBox.isSelected());
                objectTypeLabel.setEnabled(generalisationCheckBox.isSelected());
                mountainButton.setEnabled(generalisationCheckBox.isSelected());
                mountainLabel.setEnabled(generalisationCheckBox.isSelected());
                urbanButton.setEnabled(generalisationCheckBox.isSelected());
                urbanLabel.setEnabled(generalisationCheckBox.isSelected());
                scaleRepStatusLabel.setEnabled(generalisationCheckBox.isSelected());
                scaleRepStatusComboBox.setEnabled(generalisationCheckBox.isSelected());

                scaleRepStatusComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        if (scaleRepStatusComboBox.getSelectedItem().equals("Known")) {
                            scaleRepresentationTxtBox.setEnabled(true);
                            scaleRepresentationLabel.setEnabled(true);
                            scaleRepEstimationButton.setEnabled(false);
                            scaleRepEstimationLabel.setEnabled(false);
                        }
                        if (scaleRepStatusComboBox.getSelectedItem().equals("Unknown")) {
                            scaleRepresentationTxtBox.setEnabled(false);
                            scaleRepresentationLabel.setEnabled(false);
                            scaleRepEstimationButton.setEnabled(true);
                            scaleRepEstimationLabel.setEnabled(true);
                        }
                    }
                });

                // selection du fichier shp
                mountainButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // choix d'un fichier shape
                        JFileChooser choixFichierShape = new JFileChooser();
                        /*
                         * crée un filtre qui n'accepte que les fichier shp ou
                         * les répertoires
                         */
                        choixFichierShape.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(final File f) {
                                return (f.isFile() && (f.getAbsolutePath().endsWith(".shp") //$NON-NLS-1$
                                        || f.getAbsolutePath().endsWith(".SHP") //$NON-NLS-1$
                                        ) || f.isDirectory());
                            }

                            @Override
                            public String getDescription() {
                                return "Shapefile";
                            }
                        });

                        choixFichierShape.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        choixFichierShape.setMultiSelectionEnabled(false);
                        JFrame frame = new JFrame();
                        frame.setVisible(true);
                        int returnVal = choixFichierShape.showOpenDialog(frame);
                        frame.dispose();
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            pathInputMountainZone = choixFichierShape.getSelectedFile().getAbsolutePath();

                            mountainPathLabel.setEnabled(generalisationCheckBox.isSelected());
                            mountainPathTxtBox.setText(pathInputMountainZone);
                        }
                        System.out.println("Input shape : " + pathInputMountainZone);
                    }
                });

                // selection du fichier shp
                urbanButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // choix d'un fichier shape
                        JFileChooser choixFichierShape = new JFileChooser();
                        /*
                         * crée un filtre qui n'accepte que les fichier shp ou
                         * les répertoires
                         */
                        choixFichierShape.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(final File f) {
                                return (f.isFile() && (f.getAbsolutePath().endsWith(".shp") //$NON-NLS-1$
                                        || f.getAbsolutePath().endsWith(".SHP") //$NON-NLS-1$
                                        ) || f.isDirectory());
                            }

                            @Override
                            public String getDescription() {
                                return "Shapefile";
                            }
                        });

                        choixFichierShape.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        choixFichierShape.setMultiSelectionEnabled(false);
                        JFrame frame = new JFrame();
                        frame.setVisible(true);
                        int returnVal = choixFichierShape.showOpenDialog(frame);
                        frame.dispose();
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            pathInputUrbanZone = choixFichierShape.getSelectedFile().getAbsolutePath();

                            urbanPathLabel.setEnabled(generalisationCheckBox.isSelected());
                            urbanPathTxtBox.setText(pathInputUrbanZone);
                        }
                        System.out.println("Input shape : " + pathInputUrbanZone);
                    }
                });

            }
        });

        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // +++++++++++++ Lancement de l'estimation des erreurs ++++++++++++++++
        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        processingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub

                measureType = (String) PlugInEstim.inputDialog.getComboBox("Measurement type").getSelectedItem();
                projectionError = 99999;
                terrainError = 99999;
                curvatureError = 99999;
                digitizingMinError = 99999;
                digitizingMaxError = 99999;
                digitizingMeanError = 99999;
                generalisationMinError = 99999;
                generalisationMaxError = 99999;
                generalisationMeanError = 99999;

                Layer layerAEvaluer = (Layer) PlugInEstim.inputDialog.getComboBox("Dataset to evaluate")
                        .getSelectedItem();
                datasetName = layerAEvaluer.toString();
                IFeatureCollection<IFeature> jddAEvaluer = UtilJump.convertSelection(context, layerAEvaluer);

                double minX = jddAEvaluer.envelope().minX();
                double minY = jddAEvaluer.envelope().minY();
                double maxX = jddAEvaluer.envelope().maxX();
                double maxY = jddAEvaluer.envelope().maxY();

                GM_LineString perimetreEmprise = new GM_LineString();
                perimetreEmprise.coord().add(new DirectPosition(minX, maxY));
                perimetreEmprise.coord().add(new DirectPosition(maxX, maxY));
                perimetreEmprise.coord().add(new DirectPosition(maxX, minY));
                perimetreEmprise.coord().add(new DirectPosition(minX, minY));
                perimetreEmprise.coord().add(new DirectPosition(minX, maxY));
                GM_Polygon polyEmprise = new GM_Polygon(perimetreEmprise);

                if (measureType.equals("Length")) {
                    initialMeasure = jddAEvaluer.getGeomAggregate().length();
                }
                if (measureType.equals("Area")) {
                    initialMeasure = jddAEvaluer.getGeomAggregate().area();
                }

                // ---------------------------------------------------------------
                // ------------- Calcul de l'impact de la projection
                // -------------
                // ---------------------------------------------------------------
                if (projectionCheckBox.isSelected() == true) {
                    logger.info("Projection Impact Estimation --> START");
                    IFeatureCollection<IFeature> ftCollGrilleAlteration = ShapefileReader.read(pathInputDistortionGrid);

                    if (ftCollGrilleAlteration == null) {
                        projectionError = 66666;
                    } else {

                        IFeatureCollection<IFeature> jddGrilleAlteration = new FT_FeatureCollection<IFeature>();
                        jddGrilleAlteration.addAll(ftCollGrilleAlteration.select(polyEmprise.buffer(10000)));

                        // pour les polylignes (longueur)
                        if (measureComboBox.getSelectedItem().equals("Length")) {
                            LineStringProjectionImpact proj = new LineStringProjectionImpact();
                            proj.setJddAEvaluer(jddAEvaluer);
                            proj.setJddGrilleAlteration(jddGrilleAlteration);
                            proj.execute();
                            projectionError = proj.getCorrectedLength() - jddAEvaluer.getGeomAggregate().length();
                        }
                        // pour les polygones (surface)
                        if (measureComboBox.getSelectedItem().equals("Area")) {
                            // Surechantillonnage du polygone
                            IFeatureCollection<IFeature> jddPoints = new FT_FeatureCollection<IFeature>();
                            for (IFeature ft : jddAEvaluer) {
                                for (IDirectPosition dp : ft.getGeom().coord()) {
                                    jddPoints.add(new DefaultFeature(dp.toGM_Point()));
                                }
                                for (IFeature ft2 : jddGrilleAlteration.select(ft.getGeom())) {
                                    jddPoints.add(new DefaultFeature(ft2.getGeom()));
                                }
                            }

                            // Triangulation JTS : attention c'est long...
                            TriangulationJTS triangule = new TriangulationJTS("TriangulationJTS");
                            triangule.importAsNodes(jddPoints);
                            try {
                                triangule.triangule("BQ");
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                            IFeatureCollection<IFeature> jddFaces = new FT_FeatureCollection<IFeature>();
                            for (Face face : triangule.getPopFaces()) {
                                jddFaces.add(new DefaultFeature(face.getGeom()));
                            }

                            IFeatureCollection<IFeature> jddFacesSelect = new FT_FeatureCollection<IFeature>();
                            for (IFeature ft : jddAEvaluer) {
                                for (IFeature ft2 : jddFaces) {
                                    if (ft2.getGeom().centroid().toGM_Point().within(ft.getGeom())) {
                                        jddFacesSelect.add(ft2);
                                    }
                                }
                            }

                            PolygonProjectionImpact proj = new PolygonProjectionImpact();
                            proj.setJddAEvaluer(jddFacesSelect);
                            proj.setJddGrilleAlteration(jddGrilleAlteration);
                            proj.execute();

                            PolygonProjectionImpact proj2 = new PolygonProjectionImpact();
                            proj2.setJddAEvaluer(jddAEvaluer);
                            proj2.setJddGrilleAlteration(jddGrilleAlteration);
                            proj2.execute();

                            projectionError = proj.getCorrectedArea() - jddAEvaluer.getGeomAggregate().area();
                        }
                    }
                    logger.info("Projection Impact Estimation --> END");
                } else {
                    projectionError = 99999;
                }

                // ---------------------------------------------------------------
                // ------------- Calcul de l'impact du terrain
                // -------------------
                // ---------------------------------------------------------------

                if (terrainCheckBox.isSelected() == true) {
                    logger.info("Terrain Impact Estimation --> START");
                    // pour les polylignes
                    if (measureComboBox.getSelectedItem().equals("Length")) {
                        logger.info("DTM LOADING --> START");
                        // Instanciation du MNT
                        DTM mnt = new DTM(pathInputMnt, "MNT", true, 1, ColorShade.BLUE_PURPLE_WHITE);
                        logger.info("DTM LOADING --> END");
                        // Plaquage du shapefile
                        IFeatureCollection<IFeature> ftCollExp = new FT_FeatureCollection<IFeature>();
                        try {
                            ftCollExp = mnt.mapFeatureCollection(jddAEvaluer, true);
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        // Calcul des longueurs en 2D et 2D5
                        LengthTerrainError lengthError = new LengthTerrainError(ftCollExp);
                        terrainError = lengthError.getLengthError();
                    }

                    // pour les polygones
                    if (measureComboBox.getSelectedItem().equals("Area")) {
                        // Instanciation du MNT
                        DTMArea mnt = new DTMArea(pathInputMnt, "MNT", true, 1, ColorShade.BLUE_PURPLE_WHITE);
                        double aire2D = jddAEvaluer.getGeomAggregate().area();
                        double aire2D5 = 0;
                        try {
                            aire2D5 = mnt.calcul3DArea(jddAEvaluer);
                            terrainError = (aire2D5 - aire2D);
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                    logger.info("Terrain Impact Estimation --> END");
                } else {
                    terrainError = 99999;
                }

                // ---------------------------------------------------------------
                // ----- Calcul de l'impact de l'approximation polygonale
                // --------
                // ---------------------------------------------------------------

                // impact de l'approximation polygonale des courbes
                // (modélisation par
                // une courbe de Bezier)
                if (curvatureCheckBox.isSelected() == true) {
                    logger.info("Polygonal Approximation Impact Estimation --> START");
                    if (measureComboBox.getSelectedItem().equals("Length")) {

                        GranularityEvaluation granularity = new GranularityEvaluation(jddAEvaluer);
                        granularity.execute();
                        double granuMediane = granularity.getMedianDistance();
                        double tension = 0.7;

                        // Transformation des multiCurve en LinString
                        IFeatureCollection<IFeature> jddAEvaluerClean = new FT_FeatureCollection<IFeature>();
                        for (IFeature feature : jddAEvaluer) {
                            if (feature.getGeom().isLineString()) {
                                jddAEvaluerClean.add(feature);
                            }
                            if (feature.getGeom().isMultiCurve()) {
                                @SuppressWarnings("unchecked")
                                IMultiCurve<ILineString> multiLs = (IMultiCurve<ILineString>) feature.getGeom();
                                for (ILineString ls : multiLs.getList()) {
                                    jddAEvaluerClean.add(new DefaultFeature(ls));
                                }
                            }
                        }

                        // Applicatioon ou non d'une sélection des objets
                        // routiers sinueux
                        IFeatureCollection<IFeature> jddAEvaluerSelect = new FT_FeatureCollection<IFeature>();
                        if (sinuosityComboBox.getSelectedItem().equals("Yes")) {
                            RoadSinuosityDetection sinu = new RoadSinuosityDetection(jddAEvaluerClean);
                            sinu.setSigma(85); // paramètre retenu dans la thèse
                            sinu.setStep(1);
                            sinu.execute();
                            jddAEvaluerSelect.addAll(sinu.getJddSinuosityHigh());
                            jddAEvaluerSelect.addAll(sinu.getJddSinuosityHeterogeneous());
                        }
                        if (sinuosityComboBox.getSelectedItem().equals("No")) {
                            jddAEvaluerSelect = jddAEvaluerClean;
                        }

                        // On calcul les objets courbes en échantillonant la Ls
                        // initiale
                        // selon la granularité médiane
                        IFeatureCollection<IFeature> jddCardinalSplineSurech = new FT_FeatureCollection<IFeature>();
                        double lengthCurveTotale = 0;

                        // Creation des géométries Courbes
                        for (IFeature ftFeature : jddAEvaluerSelect) {
                            ILineString line = (ILineString) ftFeature.getGeom();
                            ILineString lsSurech = new GM_LineString();
                            lsSurech = Operateurs.echantillone(line, granuMediane);

                            GM_CubicSpline splineSurech = new GM_CubicSpline(lsSurech.coord());
                            splineSurech.setTangentMethod("cardinalSpline");
                            splineSurech.setTension(tension);
                            splineSurech.setBias(0);
                            splineSurech.setContinuity(0);
                            jddCardinalSplineSurech.add(new DefaultFeature(splineSurech.asLineString(1, 0)));

                            double lengthCurve = splineSurech.asLineString(1, 0).length();
                            lengthCurveTotale = lengthCurveTotale + lengthCurve;
                        }
                        curvatureError = jddCardinalSplineSurech.getGeomAggregate().length()
                                - jddAEvaluerSelect.getGeomAggregate().length();
                        //
                        // UtilJump.afficheCollection(context,
                        // jddCardinalSplineSurech,
                        // "Polygonal Approximation", "LineString", Color.blue);
                    }

                    // pour les polygones
                    if (measureComboBox.getSelectedItem().equals("Area")) {
                        curvatureError = 99999;
                    }
                    logger.info("Polygonal Approximation Impact Estimation --> END");
                } else {
                    curvatureError = 99999;
                }

                // ---------------------------------------------------------------
                // ------- Calcul de l'impact de l'erreur de pointé
                // --------------
                // ---------------------------------------------------------------

                // Simulation de l'erreur de pointé sur l'ensemble de la
                // polyligne
                if (digitizingCheckBox.isSelected() == true) {
                    logger.info("Digitizing Error Impact Estimation --> START");
                    if (scaleStatusComboBox.getSelectedItem().equals("Known")) {
                        if (measureComboBox.getSelectedItem().equals("Length")) {
                            int nbIterations = 1000;

                            // Transformation des multiCurve en LinString
                            IFeatureCollection<IFeature> jddAEvaluerClean = new FT_FeatureCollection<IFeature>();
                            for (IFeature feature : jddAEvaluer) {
                                if (feature.getGeom().isLineString()) {
                                    jddAEvaluerClean.add(feature);
                                }
                                if (feature.getGeom().isMultiCurve()) {
                                    @SuppressWarnings("unchecked")
                                    IMultiCurve<ILineString> multiLs = (IMultiCurve<ILineString>) feature.getGeom();
                                    for (ILineString ls : multiLs.getList()) {
                                        jddAEvaluerClean.add(new DefaultFeature(ls));
                                    }
                                }
                            }
                            CarteTopo carteTopoIn = CarteTopoFactory.newCarteTopo(jddAEvaluerClean);
                            carteTopoIn.filtreNoeudsSimples();
                            // IFeatureCollection<IFeature> jddSimule = new
                            // FT_FeatureCollection<IFeature>();
                            double digitScale = inputDialog.getInteger("Capture Scale");
                            double precision = digitScale / 1000;
                            List<Double> listLengthErrorSimu = new ArrayList<Double>();
                            for (int i = 0; i < nbIterations; i++) {
                                LsDistanceErrorSimulationCorrelationAngles simu = new LsDistanceErrorSimulationCorrelationAngles();
                                simu.setCarteTopoIn(carteTopoIn);
                                simu.setEcartType(Math.sqrt(precision * 0.7));
                                simu.setMoyenne(0);
                                simu.executeSimulation();
                                // jddSimule.addAll(simu.getJddOut());
                                double lengthErrorSimu = simu.getJddOut().getGeomAggregate().length()
                                        - jddAEvaluer.getGeomAggregate().length();
                                listLengthErrorSimu.add(lengthErrorSimu);
                            }
                            digitizingMinError = MathUtil.min(listLengthErrorSimu);
                            digitizingMeanError = MathUtil.moyenne(listLengthErrorSimu);
                            digitizingMaxError = MathUtil.max(listLengthErrorSimu);
                            digitizingStDevError = MathUtil.ecartType(listLengthErrorSimu);
                            // UtilJump.afficheCollection(context, jddSimule,
                            // "Digitizing",
                            // "LineString", Color.blue);
                        }

                        if (measureComboBox.getSelectedItem().equals("Area")) {
                            // Uniquement pour les objets formant une partition
                            // de l'espace
                            // (limites communales, ocs...)
                            int nbIterations = 1000;
                            double digitScale = inputDialog.getInteger("Capture Scale");
                            double precision = digitScale / 1000;

                            // Transformation des multiCurve en LineString
                            IFeatureCollection<IFeature> jddAEvaluerClean = new FT_FeatureCollection<IFeature>();
                            for (IFeature feature : jddAEvaluer) {
                                if (feature.getGeom().isPolygon()) {
                                    jddAEvaluerClean.add(new DefaultFeature(
                                            new GM_LineString(feature.getGeom().coord())));
                                }
                                if (feature.getGeom().isMultiCurve()) {
                                    @SuppressWarnings("unchecked")
                                    IMultiSurface<IPolygon> multiPoly = (IMultiSurface<IPolygon>) feature.getGeom();
                                    for (IPolygon poly : multiPoly.getList()) {
                                        jddAEvaluerClean.add(new DefaultFeature(new GM_LineString(poly.coord())));
                                    }
                                }
                            }
                            CarteTopo carteTopo = CarteTopoFactory.newCarteTopo(jddAEvaluerClean);
                            carteTopo.filtreNoeudsSimples();
                            IFeatureCollection<IFeature> jddSimule = new FT_FeatureCollection<IFeature>();
                            IPopulation<Noeud> popNoeudSimule = new Population<Noeud>();
                            List<Double> listAreaErrorSimu = new ArrayList<Double>();
                            for (int i = 0; i < nbIterations; i++) {
                                LsDistanceErrorSimulationCorrelationAngles simu = new LsDistanceErrorSimulationCorrelationAngles();
                                simu.setCarteTopoIn(carteTopo);
                                simu.setEcartType(Math.sqrt(precision * 0.7));
                                simu.setMoyenne(0);
                                simu.executeSimulation();
                                CarteTopo carteTopoSimu = CarteTopoFactory.newCarteTopo(simu.getJddOut());
                                popNoeudSimule.addAll(carteTopoSimu.getPopNoeuds());
                                IPopulation<Face> popFacesPoly = carteTopoSimu.getPopFaces();

                                IFeatureCollection<IFeature> jddPolySimule = new FT_FeatureCollection<IFeature>();

                                for (Face face : popFacesPoly) {
                                    IPolygon poly = (IPolygon) face.getGeom();
                                    jddPolySimule.add(new DefaultFeature(poly));
                                }

                                // Appariement maison
                                for (int j = 0; j < jddAEvaluer.size(); j++) {
                                    IDirectPosition centrePolyRef = jddAEvaluer.get(j).getGeom().centroid();
                                    double distanceCentreMin = Double.MAX_VALUE;
                                    int idPolyComp = 0;
                                    for (int k = 0; k < jddPolySimule.size(); k++) {
                                        IDirectPosition centrePolyComp = jddPolySimule.get(k).getGeom().centroid();
                                        double distanceCentres = centrePolyComp.distance2D(centrePolyRef);
                                        if (distanceCentres < distanceCentreMin) {
                                            distanceCentreMin = distanceCentres;
                                            idPolyComp = k;
                                        }
                                    }
                                    double areaDifference = jddPolySimule.get(idPolyComp).getGeom().area()
                                            - jddAEvaluer.get(j).getGeom().area();
                                    listAreaErrorSimu.add(areaDifference);

                                }
                                jddSimule.addAll(jddPolySimule);
                            }
                            digitizingMinError = MathUtil.min(listAreaErrorSimu);
                            digitizingMeanError = MathUtil.moyenne(listAreaErrorSimu);
                            digitizingMaxError = MathUtil.max(listAreaErrorSimu);
                            digitizingStDevError = MathUtil.ecartType(listAreaErrorSimu);
                            UtilJump.afficheCollection(context, jddSimule, "Digitizing", "LineString", Color.blue);
                        }
                    } else {
                        digitizingMeanError = 66666;
                        digitizingStDevError = 66666;
                    }
                }

                // ---------------------------------------------------------------
                // ------- Calcul de l'impact de a généralisation
                // ----------------
                // ---------------------------------------------------------------

                // Simulation de l'erreur de pointé sur l'ensemble de la
                // polyligne
                if (generalisationCheckBox.isSelected() == true) {
                    if (measureComboBox.getSelectedItem().equals("Length")) {
                        if (objectTypeComboBox.getSelectedItem().equals("Roads")) {

                            IFeatureCollection<IFeature> ftColRoadsClean = new FT_FeatureCollection<IFeature>();
                            for (IFeature feature : jddAEvaluer) {
                                if (feature.getGeom().isLineString()) {
                                    ftColRoadsClean.add(feature);
                                }
                                if (feature.getGeom().isMultiCurve()) {
                                    @SuppressWarnings("unchecked")
                                    IMultiCurve<ILineString> multiLs = (IMultiCurve<ILineString>) feature.getGeom();
                                    for (ILineString ls : multiLs.getList()) {
                                        ftColRoadsClean.add(new DefaultFeature(ls));
                                    }
                                }
                            }

                            IFeatureCollection<IFeature> ftColMountainArea = new FT_FeatureCollection<IFeature>();
                            IFeatureCollection<IFeature> ftColUrbanArea = new FT_FeatureCollection<IFeature>();
                            IFeatureCollection<IFeature> ftColMountainAreaClean = new FT_FeatureCollection<IFeature>();
                            IFeatureCollection<IFeature> ftColUrbanAreaClean = new FT_FeatureCollection<IFeature>();

                            IFeatureCollection<IFeature> ftColMountainRoads = new FT_FeatureCollection<IFeature>();
                            IFeatureCollection<IFeature> ftColUrbanRoads = new FT_FeatureCollection<IFeature>();
                            IFeatureCollection<IFeature> ftColRuralRoads = new FT_FeatureCollection<IFeature>();

                            boolean isInMountain = false;
                            boolean isInUrban = false;

                            if (pathInputMountainZone != null) {
                                isInMountain = true;
                                ftColMountainArea = ShapefileReader.read(pathInputMountainZone);
                                for (IFeature feature : ftColMountainArea) {
                                    if (feature.getGeom().isPolygon()) {
                                        ftColMountainAreaClean.add(feature);
                                    }
                                    if (feature.getGeom().isMultiSurface()) {
                                        @SuppressWarnings("unchecked")
                                        IMultiSurface<IPolygon> multiPoly = (IMultiSurface<IPolygon>) feature.getGeom();
                                        for (IPolygon poly : multiPoly.getList()) {
                                            ftColMountainAreaClean.add(new DefaultFeature(poly));
                                        }
                                    }
                                }
                            }
                            if (pathInputUrbanZone != null) {
                                isInUrban = true;
                                ftColUrbanArea = ShapefileReader.read(pathInputUrbanZone);
                                for (IFeature feature : ftColUrbanArea) {
                                    if (feature.getGeom().isPolygon()) {
                                        ftColUrbanAreaClean.add(feature);
                                    }
                                    if (feature.getGeom().isMultiSurface()) {
                                        @SuppressWarnings("unchecked")
                                        IMultiSurface<IPolygon> multiPoly = (IMultiSurface<IPolygon>) feature.getGeom();
                                        for (IPolygon poly : multiPoly.getList()) {
                                            ftColUrbanAreaClean.add(new DefaultFeature(poly));
                                        }
                                    }
                                }
                            }

                            // Decoupage des routes en fonction du contexte
                            // géographique
                            if (isInUrban == false && isInMountain == false) {
                                ftColRuralRoads = ftColRoadsClean;
                            }

                            if (isInUrban == true && isInMountain == false) {
                                for (IFeature ftRoad : ftColRoadsClean) {
                                    LineString geomRoad = null;
                                    Geometry geomUrban = null;
                                    try {
                                        geomRoad = (LineString) JtsGeOxygene.makeJtsGeom(ftRoad.getGeom());
                                        geomUrban = (Geometry) JtsGeOxygene.makeJtsGeom(ftColUrbanAreaClean
                                                .getGeomAggregate());
                                    } catch (Exception e2) {
                                        // TODO Auto-generated catch block
                                        e2.printStackTrace();
                                    }
                                    Geometry intersection = geomRoad.intersection(geomUrban);
                                    try {
                                        ftColUrbanRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(intersection)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    Geometry difference = geomRoad.difference(geomUrban);
                                    try {
                                        ftColRuralRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(difference)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            }

                            if (isInUrban == false && isInMountain == true) {
                                for (IFeature ftRoad : ftColRoadsClean) {
                                    LineString geomRoad = null;
                                    Geometry geomMountain = null;
                                    try {
                                        geomRoad = (LineString) JtsGeOxygene.makeJtsGeom(ftRoad.getGeom());
                                        geomMountain = (Geometry) JtsGeOxygene.makeJtsGeom(ftColMountainAreaClean
                                                .getGeomAggregate());
                                    } catch (Exception e2) {
                                        // TODO Auto-generated catch block
                                        e2.printStackTrace();
                                    }
                                    Geometry intersection = geomRoad.intersection(geomMountain);
                                    try {
                                        ftColMountainRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(intersection)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    Geometry difference = geomRoad.difference(geomMountain);
                                    try {
                                        ftColRuralRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(difference)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            }

                            if (isInUrban == true && isInMountain == true) {
                                IFeatureCollection<IFeature> ftColNotUrbanRoads = new FT_FeatureCollection<IFeature>();
                                for (IFeature ftRoad : ftColRoadsClean) {
                                    LineString geomRoad = null;
                                    Geometry geomUrban = null;
                                    try {
                                        geomRoad = (LineString) JtsGeOxygene.makeJtsGeom(ftRoad.getGeom());
                                        geomUrban = (Geometry) JtsGeOxygene.makeJtsGeom(ftColUrbanAreaClean
                                                .getGeomAggregate());
                                    } catch (Exception e2) {
                                        // TODO Auto-generated catch block
                                        e2.printStackTrace();
                                    }
                                    Geometry intersection = geomRoad.intersection(geomUrban);
                                    try {
                                        ftColUrbanRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(intersection)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    Geometry difference = geomRoad.difference(geomUrban);
                                    try {
                                        ftColNotUrbanRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(difference)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                                IFeatureCollection<IFeature> ftColNotUrbanRoadsClean = new FT_FeatureCollection<IFeature>();
                                for (IFeature feature : ftColNotUrbanRoads) {
                                    if (feature.getGeom().isLineString()) {
                                        ftColNotUrbanRoadsClean.add(feature);
                                    }
                                    if (feature.getGeom().isMultiCurve()) {
                                        @SuppressWarnings("unchecked")
                                        IMultiCurve<ILineString> multiLs = (IMultiCurve<ILineString>) feature.getGeom();
                                        for (ILineString ls : multiLs.getList()) {
                                            ftColNotUrbanRoadsClean.add(new DefaultFeature(ls));
                                        }
                                    }
                                }

                                for (IFeature ftRoad : ftColNotUrbanRoadsClean) {
                                    LineString geomRoad = null;
                                    Geometry geomMountain = null;
                                    try {
                                        geomRoad = (LineString) JtsGeOxygene.makeJtsGeom(ftRoad.getGeom());
                                        geomMountain = (Geometry) JtsGeOxygene.makeJtsGeom(ftColMountainAreaClean
                                                .getGeomAggregate());
                                    } catch (Exception e2) {
                                        // TODO Auto-generated catch block
                                        e2.printStackTrace();
                                    }
                                    Geometry intersection = geomRoad.intersection(geomMountain);
                                    try {
                                        ftColMountainRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(intersection)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    Geometry difference = geomRoad.difference(geomMountain);
                                    try {
                                        ftColRuralRoads.add(new DefaultFeature((IGeometry) JtsGeOxygene
                                                .makeGeOxygeneGeom(difference)));
                                    } catch (Exception e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            }

                            // Application du modèle statistique
                            if (scaleRepStatusComboBox.getSelectedItem().equals("Known")) {
                                double repScale = inputDialog.getInteger("Representation Scale");
                                double lengthRefEstim = 0;
                                // pour du 1:50.000
                                if (repScale >= 40000 && repScale < 70000) {
                                    double lengthRefMountain = ftColMountainRoads.getGeomAggregate().length() * 1.0085;
                                    double lengthRefUrban = ftColUrbanRoads.getGeomAggregate().length() * 1.0059;
                                    double lengthRefRural = ftColRuralRoads.getGeomAggregate().length() * 1.0053;
                                    lengthRefEstim = lengthRefMountain + lengthRefUrban + lengthRefRural;
                                }
                                // pour du 1:100.000
                                if (repScale >= 70000 && repScale < 150000) {
                                    double lengthRefMountain = ftColMountainRoads.getGeomAggregate().length() * 1.0562;
                                    double lengthRefUrban = ftColUrbanRoads.getGeomAggregate().length() * 1.0264;
                                    double lengthRefRural = ftColRuralRoads.getGeomAggregate().length() * 1.0307;
                                    lengthRefEstim = lengthRefMountain + lengthRefUrban + lengthRefRural;
                                }
                                // pour du 1:250.000
                                if (repScale >= 150000 && repScale < 300000) {
                                    double lengthRefMountain = ftColMountainRoads.getGeomAggregate().length() * 1.0667;
                                    double lengthRefUrban = ftColUrbanRoads.getGeomAggregate().length() * 1.0566;
                                    double lengthRefRural = ftColRuralRoads.getGeomAggregate().length() * 1.0403;
                                    lengthRefEstim = lengthRefMountain + lengthRefUrban + lengthRefRural;
                                }
                                double lengthErrorGen = lengthRefEstim - ftColRoadsClean.getGeomAggregate().length();
                                generalisationMeanError = lengthErrorGen;
                            } else {
                                generalisationMeanError = 66666;
                            }
                        }
                    }
                }

                PlugInEstimResults plugIn = new PlugInEstimResults();
                plugIn.setImpacts(measureType, datasetName, initialMeasure, projectionError, terrainError,
                        curvatureError, digitizingMeanError, digitizingStDevError, generalisationMeanError);
                plugIn.execute(context);

                if (agregationComboBox.getSelectedItem().equals("Yes")) {
                    if (measureComboBox.getSelectedItem().equals("Length")) {
                        if (projectionError == 99999 || projectionError == 66666)
                            projectionError = 0;
                        if (terrainError == 99999 || terrainError == 66666)
                            terrainError = 0;
                        if (curvatureError == 99999 || curvatureError == 66666)
                            curvatureError = 0;
                        if (digitizingMeanError == 99999 || digitizingMeanError == 66666)
                            digitizingMeanError = 0;
                        if (generalisationMeanError == 99999 || generalisationMeanError == 66666)
                            generalisationMeanError = 0;

                        double minLengthEstim = initialMeasure;
                        double meanLengthEstim = initialMeasure
                                + Math.sqrt(((projectionError * projectionError) + (terrainError * terrainError)
                                        + (curvatureError * curvatureError)
                                        + (digitizingMeanError * digitizingMeanError) + (generalisationMeanError * generalisationMeanError)) / 5);
                        double maxLengthEstim = initialMeasure + projectionError + terrainError + curvatureError
                                + digitizingMeanError + generalisationMeanError;

                        PlugInEstimResultsAgregate plugInAgregate = new PlugInEstimResultsAgregate();
                        plugInAgregate.setImpacts(measureType, datasetName, initialMeasure, minLengthEstim,
                                meanLengthEstim, maxLengthEstim);
                        plugInAgregate.execute(context);
                    }
                }
            }

        });

        /**
         * Estimation of the digitizing scale by computing granularity
         */
        scaleEstimationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                Layer layerAEvaluer = (Layer) PlugInEstim.inputDialog.getComboBox("Dataset to evaluate")
                        .getSelectedItem();
                IFeatureCollection<IFeature> jddAEvaluer = UtilJump.convertSelection(context, layerAEvaluer);
                GranularityEvaluation granularity = new GranularityEvaluation(jddAEvaluer);
                granularity.execute();
                double granuMediane = granularity.getMedianDistance();
                double digitizingScale = (granuMediane * 1000) / 2;
                PlugInRepresentationScaleResult plugIn = new PlugInRepresentationScaleResult();
                plugIn.setScale(digitizingScale);
                plugIn.execute(context);
            }
        });

        /**
         * Estimation of the representation (only for road features)
         */
        scaleRepEstimationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlugInRepresentationScaleEstimation plugIn = new PlugInRepresentationScaleEstimation();
                plugIn.execute(context);
            }
        });

        // Centre et rend visible la fenêtre
        GUIUtil.centreOnWindow(inputDialog);
        inputDialog.setVisible(true);
        return inputDialog;
    }

}
