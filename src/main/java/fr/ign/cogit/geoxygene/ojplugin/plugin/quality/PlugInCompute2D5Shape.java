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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import com.vividsolutions.jump.workbench.WorkbenchContext;
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
import fr.ign.cogit.geoxygene.contrib.quality.estim.terrain.LengthTerrainError;
import fr.ign.cogit.geoxygene.sig3d.process.CalculZShape;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTM;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTMArea;
import fr.ign.cogit.geoxygene.sig3d.util.ColorShade;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

/**
 * A plugin to generate a 2D5 vector dataset (in .shp) using a DTM (in .asc
 * format)
 * @author JFGirres
 * 
 */
public class PlugInCompute2D5Shape extends FeatureInfoPlugIn {

  static Logger logger = Logger.getLogger(PlugInSimulation.class.getName());
  public static MultiInputDialog inputDialog;
  public static String pathInputShp;
  public static String pathInputMnt;

  /** initialisation du menu parametrage */
  public void initialize(PlugInContext context) throws Exception {
      FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
      featureInstaller.addMainMenuPlugin(this, new String[] { MenuNames.PLUGINS, "GeOxygene", "Spatial Data Quality",
              "Util" }, "Create 2D5 Dataset", false, null, createEnableCheck(context.getWorkbenchContext()));
      
      /*EnableCheckFactory factory = new EnableCheckFactory(
        context.getWorkbenchContext());
    context.getFeatureInstaller().addMainMenuItem(
        this,
        new String[] { "GeOxygene", "Spatial Data Quality", "Util" },
        "Create 2D5 Dataset",
        false,
        null,
        new MultiEnableCheck().add(factory
            .createAtLeastNLayersMustExistCheck(0)));*/
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
        "Create 2D5 Dataset", false);

    inputDialog
        .addLabel("---------------------------------------------------------------------------");

    // Création d'une liste avec les valeurs prises par le menu déroulant du
    // choix de méthode de bruitage
    List<String> items = Arrays.asList("LineString", "Polygon");
    final JComboBox geomComboBox = inputDialog.addComboBox("Type de Géométrie",
        "Géométrie", items, "Géométrie");
    inputDialog
        .addLabel("---------------------------------------------------------------------------");
    inputDialog.addLabel("Choix des données");

    final JButton choixShpButton = new JButton("...");
    inputDialog
        .addRow("Choix du fichier .shp", new JLabel("Choix du fichier .shp"),
            choixShpButton, null, "Choix du fichier .shp");

    final JButton choixMntButton = new JButton("...");
    inputDialog.addRow("Choix du MNT (.asc)",
        new JLabel("Choix du MNT (.asc)"), choixMntButton, null,
        "Choix du MNT (.asc)");
    choixMntButton.setEnabled(false);

    inputDialog
        .addLabel("---------------------------------------------------------------------------");

    // Bouton permettant d'afficher l'histogramme
    final JButton processingButton = new JButton("Ok");
    inputDialog.addRow("Création du jeu 2D5",
        new JLabel("Création du jeu 2D5"), processingButton, null,
        "Création du jeu 2D5");
    processingButton.setEnabled(false);

    inputDialog
        .addLabel("---------------------------------------------------------------------------");

    choixShpButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // choix d'un fichier shape
        JFileChooser choixFichierShape = new JFileChooser();
        /*
         * crée un filtre qui n'accepte que les fichier shp ou les répertoires
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
          pathInputShp = choixFichierShape.getSelectedFile().getAbsolutePath();
          choixMntButton.setEnabled(true);
          // JFileChooser choixFichierArcGrid = new JFileChooser();
        }
        System.out.println("Input shape" + pathInputShp);
      }
    });

    // Bouton validation
    choixMntButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser choixFichierArcGrid = new JFileChooser();
        /*
         * crée un filtre qui n'accepte que les fichier GeoTiff ou les
         * répertoires
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
          pathInputMnt = choixFichierArcGrid.getSelectedFile()
              .getAbsolutePath();
          processingButton.setEnabled(true);
        }
        System.out.println("Input mnt" + pathInputMnt);

      }
    });

    // Bouton validation
    processingButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {

        String pathOutputShp = pathInputShp.substring(0,
            pathInputShp.lastIndexOf('.'))
            + "_2D5.shp";

        if (geomComboBox.getSelectedItem().equals("LineString")) {

          // Instanciation du MNT
          DTM mnt = new DTM(pathInputMnt,// Fichier à charger
              "MNT",// Nom de la couche (le MNT est fait pour être chargé)
              true, // option d'affichage (inutile dans ce cas, sert à indiquer
                    // si l'on veut un
              1, // Le coefficient d'exaggération du MNT
              ColorShade.BLUE_PURPLE_WHITE// La couleur du dégradé
          );

          // Plaquage du shapefile
          IFeatureCollection<IFeature> ftCollExp = mnt.mapShapeFile(
              pathInputShp, // On indique le nom du shape en entrée
              false); // Indique si l'on surechantillonne ou pas

          LengthTerrainError lengthError = new LengthTerrainError(ftCollExp);

          System.out.println("LENGTH 2D = " + lengthError.getLength2D());
          System.out.println("LENGTH 2D5 = " + lengthError.getLength2D5());
          System.out.println("LINESTRING : Length Difference = "
              + lengthError.getLengthError());

          CalculZShape.calcul(pathInputMnt, pathInputShp, pathOutputShp);

          // FT_FeatureCollection<DefaultFeature> jddShp2D5 =
          // ShapefileReader.read(pathOutputShp);
          //
          // // Initiatlisation de la fenêtre cartographique
          // Task taskCarte = new Task();
          // taskCarte.setName("Create 2D5 Shape");
          // context.getWorkbenchFrame().addTaskFrame(taskCarte);
          //
          // // Affichage
          // UtilJump.afficheCollection(context, jddShp2D5, "JDD 2D5",
          // geomComboBox.getSelectedItem().toString(), Color.red);
          //

        }

        if (geomComboBox.getSelectedItem().equals("Polygon")) {

          // Instanciation du MNT
          DTMArea mnt = new DTMArea(pathInputMnt,// Fichier à charger
              "MNT",// Nom de la couche (le MNT est fait pour être chargé)
              true, // option d'affichage (inutile dans ce cas, sert à
                    // indiquer si l'on veut un
              1, // Le coefficient d'exaggération du MNT
              ColorShade.BLUE_PURPLE_WHITE// La couleur du dégradé
          );

          CalculZShape.calcul(pathInputMnt, pathInputShp, pathOutputShp);

          IFeatureCollection<IFeature> featColl = ShapefileReader
              .read(pathInputShp);
          double aire2D = featColl.getGeomAggregate().area();
          double aire2D5 = 0;
          try {
            aire2D5 = mnt.calcul3DArea(featColl);

            System.out.println("AREA 2D = " + aire2D);
            System.out.println("AREA 2D5 = " + aire2D5);
            System.out.println("POLYGON : Area Difference = "
                + (aire2D5 - aire2D));

          } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }

        }

      }
    });

    // Centre et rend visible la fen�tre
    GUIUtil.centreOnWindow(inputDialog);
    inputDialog.setVisible(true);
    return inputDialog;
  }
}
