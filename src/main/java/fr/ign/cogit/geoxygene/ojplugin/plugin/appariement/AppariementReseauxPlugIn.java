/*
 * This file is part of the GeOxygene-plugin project for OpenJump.
 * GeOxygene aims at providing an open framework which implements OGC/ISO
 * specifications for the development and deployment of geographic(GIS)
 * applications. It is a open source contribution of the COGIT laboratory at the
 * Institut Géographique National (the French National Mapping Agency).
 * See: http://oxygene-project.sourceforge.net
 * Copyright (C) 2009 Institut Géographique National
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * For more information, contact:
 * IGN Laboratoire COGIT - GeOxygene 73 avenue de Paris 94165 SAINT-MANDE Cedex
 * France
 */

package fr.ign.cogit.geoxygene.ojplugin.plugin.appariement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiTabInputDialog;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import org.openjump.core.ui.plugin.window.MosaicInternalFramesPlugIn;
import org.openjump.core.ui.plugin.window.SynchronizationPlugIn;

import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.I18N;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.AppariementIO;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.ParametresApp;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.Recalage;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.topologie.ReseauApp;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.ojplugin.plugin.I18NPlug;

import fr.ign.cogit.geoxygene.ojplugin.geoxygene.UtilJump;

/**
 * Cette classe contient le code permettant de tester l'appariement de réseaux
 * implémenté dans GeOxygene
 * 
 * @author Eric Grosso - IGN / Laboratoire COGIT
 * @author Michaël Michaud - IGN
 */
public class AppariementReseauxPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

  private static MultiTabInputDialog dialog;

  private final static String DONNEESBRUTES = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.donneesbrutes");
  private final static String RESEAU = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.reseau");
  private final static String REFERENCE = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.reference");
  private final static String COMPARAISON = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.comparaison");
  private final static String LIENS = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.liens");
  private final static String LIENSPEUSURS = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.lienstrespeusurs");
  private final static String LIENSINCERTAINS = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.liensincertains");
  private final static String LIENSSURS = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.lienssurs");
  private final static String ARCSAPP = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.arcsapparies");
  private final static String ARCSINCERTAINS = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.arcsincertains");
  private final static String ARCSNONAPP = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.arcsnonapparies");
  private final static String NOEUDSAPP = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.noeudsapparies");
  private final static String NOEUDSINCERTAINS = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.noeudsincertains");
  private final static String NOEUDSNONAPP = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.noeudsnonapparies");
  private final static String ARCS = I18NPlug
      .getString("arcs");
  private final static String NOEUDS = I18NPlug
      .getString("noeuds");
  private final static String RESEAU_RECALE = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.reseauRecale");

  private final static String RECALER =  I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.recaler");

  private final static String DISTANCE_NOEUD_MAX = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.distanceNoeudsMax");
  private final static String DISTANCE_NOEUDS_IMPASSE_MAX = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.distanceNoeudsImpassesMax");
  private final static String DISTANCE_ARCS_MAX = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.distanceArcsMax");
  private final static String DISTANCE_ARCS_MIN = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.distanceArcsMin");

  private final static String SEUIL_FUSION_NOEUDS_1 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieSeuilFusionNoeud1");
  private final static String SEUIL_FUSION_NOEUDS_2 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieSeuilFusionNoeud2");
  private final static String ELIMINE_NOEUDS_AVEC_2_ARCS_1 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieElimineNoeudsAvecDeuxArcs1");
  private final static String ELIMINE_NOEUDS_AVEC_2_ARCS_2 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieElimineNoeudsAvecDeuxArcs2");
  private final static String GRAPHE_PLANAIRE_1 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieGraphePlanaire1");
  private final static String GRAPHE_PLANAIRE_2 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieGraphePlanaire2");
  private final static String FUSION_ARCS_DOUBLES_1 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieFusionArcsDoubles1");
  private final static String FUSION_ARCS_DOUBLES_2 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.topologieFusionArcsDoubles2");

  private final static String PROJETE_NOEUDS_1_SUR_RESEAU_2 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds1SurReseau2");
  private final static String PROJETE_NOEUDS_1_SUR_RESEAU_2_DISTANCE_NOEUD_ARC = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds1SurReseau2DistanceNoeudArc");
  private final static String PROJETE_NOEUDS_1_SUR_RESEAU_2_DISTANCE_PROJECTION_NOEUD = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds1SurReseau2DistanceProjectionNoeud");
  private final static String PROJETE_NOEUDS_1_SUR_RESEAU_2_IMPASSES_SEULEMENT = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds1SurReseau2ImpassesSeulement");

  private final static String PROJETE_NOEUDS_2_SUR_RESEAU_1 = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds2SurReseau1");
  private final static String PROJETE_NOEUDS_2_SUR_RESEAU_1_DISTANCE_NOEUD_ARC = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds2SurReseau1DistanceNoeudArc");
  private final static String PROJETE_NOEUDS_2_SUR_RESEAU_1_DISTANCE_PROJECTION_NOEUD = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds2SurReseau1DistanceProjectionNoeud");
  private final static String PROJETE_NOEUDS_2_SUR_RESEAU_1_IMPASSES_SEULEMENT = I18NPlug
      .getString("appariement.AppariementReseauxPlugIn.projeteNoeuds2SurReseau1ImpassesSeulement");

  boolean recaler = true;

  double  distanceNoeudsMax = 150;
  double  distanceNoeudsImpassesMax = -1;
  double  distanceArcsMax = 100;
  double  distanceArcsMin = 30;

  double  seuilFusionNoeuds1 = -1;
  double  seuilFusionNoeuds2 = -1;
  boolean elimineNoeudsAvecDeuxArcs1 = false;
  boolean elimineNoeudsAvecDeuxArcs2 = false;
  boolean graphePlanaire1 = false;
  boolean graphePlanaire2 = false;
  boolean fusionArcsDoubles1 = false;
  boolean fusionArcsDoubles2 = false;

  boolean projeteNoeuds1SurReseau2 = false;
  double  projeteNoeuds1SurReseau2DistanceNoeudArc = 0;
  double  projeteNoeuds1SurReseau2DistanceProjectionNoeud = 0;
  boolean projeteNoeuds1SurReseau2ImpassesSeulement = false;

  boolean projeteNoeuds2SurReseau1 = false;
  double  projeteNoeuds2SurReseau1DistanceNoeudArc = 0;
  double  projeteNoeuds2SurReseau1DistanceProjectionNoeud = 0;
  boolean projeteNoeuds2SurReseau1ImpassesSeulement = false;


  @Override
  public void initialize(PlugInContext context) throws Exception {
    FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
    featureInstaller.addMainMenuPlugin(this, 
        new String[] {MenuNames.PLUGINS, "GeOxygene", I18NPlug.getString("appariement")},
        I18NPlug.getString("appariementreseaux"), false, null, createEnableCheck(context.getWorkbenchContext()));
  }

  public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
    return new MultiEnableCheck().add(checkFactory.createAtLeastNLayersMustExistCheck(2));
  }

  @Override
  public boolean execute(PlugInContext context) throws Exception {
    this.reportNothingToUndoYet(context);
    dialog = new MultiTabInputDialog(context.getWorkbenchFrame(), getName(), "Options principales", true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (!dialog.wasOKPressed()) {
      return false;
    }
    return true;
  }

  private void setDialogValues(MultiTabInputDialog dialog, PlugInContext context) {
    dialog.setSideBarDescription(I18NPlug.getString("appariementreseaux"));

    dialog.addLayerComboBox(
        I18NPlug.getString("appariement.AppariementReseauxPlugIn.reseaureference"),
        context.getLayerNamePanel().getLayerManager().getLayer(0),
        context.getLayerNamePanel().getLayerManager());

    dialog.addLayerComboBox(
        I18NPlug.getString("appariement.AppariementReseauxPlugIn.reseaucomparaison"),
        context.getLayerNamePanel().getLayerManager().getLayer(1),
        context.getLayerNamePanel().getLayerManager());

    //dialog.addCheckBox(RECALER, recaler);

    dialog.addDoubleField(DISTANCE_NOEUD_MAX, distanceNoeudsMax, 8);
    dialog.addDoubleField(DISTANCE_NOEUDS_IMPASSE_MAX, distanceNoeudsImpassesMax, 8);
    dialog.addDoubleField(DISTANCE_ARCS_MAX, distanceArcsMax, 8);
    dialog.addDoubleField(DISTANCE_ARCS_MIN, distanceArcsMin, 8);

    dialog.addPane("Topologie");

    dialog.addDoubleField(SEUIL_FUSION_NOEUDS_1, seuilFusionNoeuds1, 8);
    dialog.addDoubleField(SEUIL_FUSION_NOEUDS_2, seuilFusionNoeuds2, 8);
    dialog.addCheckBox(ELIMINE_NOEUDS_AVEC_2_ARCS_1, elimineNoeudsAvecDeuxArcs1);
    dialog.addCheckBox(ELIMINE_NOEUDS_AVEC_2_ARCS_2, elimineNoeudsAvecDeuxArcs2);
    dialog.addCheckBox(GRAPHE_PLANAIRE_1, graphePlanaire1);
    dialog.addCheckBox(GRAPHE_PLANAIRE_2, graphePlanaire2);
    dialog.addCheckBox(FUSION_ARCS_DOUBLES_1, fusionArcsDoubles1);
    dialog.addCheckBox(FUSION_ARCS_DOUBLES_2, fusionArcsDoubles2);

    dialog.addPane("Projections");

    dialog.addCheckBox(PROJETE_NOEUDS_1_SUR_RESEAU_2, projeteNoeuds1SurReseau2);
    dialog.addDoubleField(PROJETE_NOEUDS_1_SUR_RESEAU_2_DISTANCE_NOEUD_ARC , projeteNoeuds1SurReseau2DistanceNoeudArc, 8);
    dialog.addDoubleField(PROJETE_NOEUDS_1_SUR_RESEAU_2_DISTANCE_PROJECTION_NOEUD , projeteNoeuds1SurReseau2DistanceProjectionNoeud, 8);
    dialog.addCheckBox(PROJETE_NOEUDS_1_SUR_RESEAU_2_IMPASSES_SEULEMENT, projeteNoeuds1SurReseau2ImpassesSeulement);

    dialog.addSeparator();

    dialog.addCheckBox(PROJETE_NOEUDS_2_SUR_RESEAU_1, projeteNoeuds2SurReseau1);
    dialog.addDoubleField(PROJETE_NOEUDS_2_SUR_RESEAU_1_DISTANCE_NOEUD_ARC , projeteNoeuds2SurReseau1DistanceNoeudArc, 8);
    dialog.addDoubleField(PROJETE_NOEUDS_2_SUR_RESEAU_1_DISTANCE_PROJECTION_NOEUD , projeteNoeuds2SurReseau1DistanceProjectionNoeud, 8);
    dialog.addCheckBox(PROJETE_NOEUDS_2_SUR_RESEAU_1_IMPASSES_SEULEMENT, projeteNoeuds2SurReseau1ImpassesSeulement);
  }

  @Override
  public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
    apparie(context);
    System.gc();
  }

  /**
   * @return vrai si l'appariement s'est bien passé
   */
  private boolean apparie(PlugInContext context) throws Exception {

    // on prend en compte les paramètres des données de référence et à comparer
    Layer layerReference = dialog.getLayer(I18NPlug
        .getString("appariement.AppariementReseauxPlugIn.reseaureference"));
    Layer layerComparaison = dialog.getLayer(I18NPlug
        .getString("appariement.AppariementReseauxPlugIn.reseaucomparaison"));

    //recaler = dialog.getBoolean(RECALER);

    distanceNoeudsMax = dialog.getDouble(DISTANCE_NOEUD_MAX);
    distanceNoeudsImpassesMax = dialog.getDouble(DISTANCE_NOEUDS_IMPASSE_MAX);
    distanceArcsMax = dialog.getDouble(DISTANCE_ARCS_MAX);
    distanceArcsMin = dialog.getDouble(DISTANCE_ARCS_MIN);

    seuilFusionNoeuds1 = dialog.getDouble(SEUIL_FUSION_NOEUDS_1);
    seuilFusionNoeuds2 = dialog.getDouble(SEUIL_FUSION_NOEUDS_2);
    elimineNoeudsAvecDeuxArcs1 = dialog.getBoolean(ELIMINE_NOEUDS_AVEC_2_ARCS_1);
    elimineNoeudsAvecDeuxArcs2 = dialog.getBoolean(ELIMINE_NOEUDS_AVEC_2_ARCS_2);
    graphePlanaire1 = dialog.getBoolean(GRAPHE_PLANAIRE_1);
    graphePlanaire2 = dialog.getBoolean(GRAPHE_PLANAIRE_2);
    fusionArcsDoubles1 = dialog.getBoolean(FUSION_ARCS_DOUBLES_1);
    fusionArcsDoubles2 = dialog.getBoolean(FUSION_ARCS_DOUBLES_2);

    projeteNoeuds1SurReseau2 = dialog.getBoolean(PROJETE_NOEUDS_1_SUR_RESEAU_2);
    projeteNoeuds1SurReseau2DistanceNoeudArc = dialog.getDouble(PROJETE_NOEUDS_1_SUR_RESEAU_2_DISTANCE_NOEUD_ARC);
    projeteNoeuds1SurReseau2DistanceProjectionNoeud = dialog.getDouble(PROJETE_NOEUDS_1_SUR_RESEAU_2_DISTANCE_PROJECTION_NOEUD);
    projeteNoeuds1SurReseau2ImpassesSeulement = dialog.getBoolean(PROJETE_NOEUDS_1_SUR_RESEAU_2_IMPASSES_SEULEMENT);

    projeteNoeuds2SurReseau1 = dialog.getBoolean(PROJETE_NOEUDS_2_SUR_RESEAU_1);
    projeteNoeuds2SurReseau1DistanceNoeudArc = dialog.getDouble(PROJETE_NOEUDS_2_SUR_RESEAU_1_DISTANCE_NOEUD_ARC);
    projeteNoeuds2SurReseau1DistanceProjectionNoeud = dialog.getDouble(PROJETE_NOEUDS_2_SUR_RESEAU_1_DISTANCE_PROJECTION_NOEUD);
    projeteNoeuds2SurReseau1ImpassesSeulement = dialog.getBoolean(PROJETE_NOEUDS_2_SUR_RESEAU_1_IMPASSES_SEULEMENT);


    // On récupère les objets sélectionnés dans différentes couches
    IFeatureCollection<DefaultFeature> tronconsReference = new FT_FeatureCollection<DefaultFeature>();
    IFeatureCollection<DefaultFeature> tronconsComparaison = new FT_FeatureCollection<DefaultFeature>();

    try {
      tronconsReference = UtilJump.getObjectsFromLayer(layerReference);
      tronconsComparaison = UtilJump.getObjectsFromLayer(layerComparaison);
    } catch (Exception e) {
      e.printStackTrace();
    }
		
    // Appariement
		
    // Initialisation des paramètres
    ParametresApp param = new ParametresApp();
    param.populationsArcs1.add(tronconsReference);
    param.populationsArcs2.add(tronconsComparaison);
    param.distanceNoeudsMax = (float)distanceNoeudsMax;
    param.distanceNoeudsImpassesMax = (float)distanceNoeudsImpassesMax;
    param.distanceArcsMax = (float)distanceArcsMax;
    param.distanceArcsMin = (float)distanceArcsMin;

    param.topologieSeuilFusionNoeuds1 = (float)seuilFusionNoeuds1;
    param.topologieSeuilFusionNoeuds2 = (float)seuilFusionNoeuds1;
    param.topologieElimineNoeudsAvecDeuxArcs1 = elimineNoeudsAvecDeuxArcs1;
    param.topologieElimineNoeudsAvecDeuxArcs2 = elimineNoeudsAvecDeuxArcs2;
    param.topologieGraphePlanaire1 = graphePlanaire1;
    param.topologieGraphePlanaire2 = graphePlanaire2;
    param.topologieFusionArcsDoubles1 = fusionArcsDoubles1;
    param.topologieFusionArcsDoubles2 = fusionArcsDoubles2;

    param.projeteNoeuds1SurReseau2 = projeteNoeuds1SurReseau2;
    param.projeteNoeuds1SurReseau2DistanceNoeudArc = projeteNoeuds1SurReseau2DistanceNoeudArc;
    param.projeteNoeuds1SurReseau2DistanceProjectionNoeud = projeteNoeuds1SurReseau2DistanceProjectionNoeud;
    param.projeteNoeuds1SurReseau2ImpassesSeulement = projeteNoeuds1SurReseau2ImpassesSeulement;

    param.projeteNoeuds2SurReseau1 = projeteNoeuds2SurReseau1;
    param.projeteNoeuds2SurReseau1DistanceNoeudArc = projeteNoeuds2SurReseau1DistanceNoeudArc;
    param.projeteNoeuds2SurReseau1DistanceProjectionNoeud = projeteNoeuds2SurReseau1DistanceProjectionNoeud;
    param.projeteNoeuds2SurReseau1ImpassesSeulement = projeteNoeuds2SurReseau1ImpassesSeulement;

    param.varianteForceAppariementSimple = true;
    param.varianteFiltrageImpassesParasites = false;
    param.varianteRedecoupageArcsNonApparies = true;
    param.debugTirets = false;
    param.debugBilanSurObjetsGeo = false;
    param.varianteRedecoupageArcsNonApparies = true;
    param.debugAffichageCommentaires = 2;

    // Si le paramètre recaler est demandé par l'utilisateur
    // il est important de mettre debugBilanSurObjetsGeo à false
    if (recaler) param.debugBilanSurObjetsGeo = false;
		
    // Lance les traitement et récupère les liens d'appariement
    List<ReseauApp> cartesTopo = new ArrayList<ReseauApp>();
    EnsembleDeLiens liens = AppariementIO.appariementDeJeuxGeo(param, cartesTopo);

    // Récupération des réseaux (ReseauApp hérite de CarteTopo)
    ReseauApp carteTopoReference = cartesTopo.get(0);
    ReseauApp carteTopoComparaison = cartesTopo.get(1);

    // Classement des arcs selon le résultat (évaluation des résultats)
    List<String> valeursClassement = new ArrayList<String>();
    valeursClassement.add(I18N.getString("Appariement.Matched"));
    valeursClassement.add(I18N.getString("Appariement.Uncertain"));
    valeursClassement.add(I18N.getString("Appariement.Unmatched"));

    // Récupération des liens puis classement en sûrs, incertains et très incertains
    List<Double> valeursClassementL = new ArrayList<Double>();
    valeursClassementL.add(new Double(0.5));
    valeursClassementL.add(new Double(1));

    List<EnsembleDeLiens> liensClasses = liens.classeSelonSeuilEvaluation(valeursClassementL);
    EnsembleDeLiens liensNuls = liensClasses.get(0);
    EnsembleDeLiens liensIncertains = liensClasses.get(1);
    EnsembleDeLiens liensSurs = liensClasses.get(2);

    // Récupération des arcs et des noeuds puis classement en "appariés", "incertains" ou
    // "non appariés" (BD référence)
    List<ReseauApp> cartesTopoReferenceValuees = AppariementIO
        .scindeSelonValeursResultatsAppariement(carteTopoReference, valeursClassement);
    IPopulation<Arc> arcsReferenceApparies = cartesTopoReferenceValuees.get(0).getPopArcs();
    IPopulation<Arc> arcsReferenceIncertains = cartesTopoReferenceValuees.get(1).getPopArcs();
    IPopulation<Arc> arcsReferenceNonApparies = cartesTopoReferenceValuees.get(2).getPopArcs();
    IPopulation<Noeud> noeudsReferenceApparies = cartesTopoReferenceValuees.get(0).getPopNoeuds();
    IPopulation<Noeud> noeudsReferenceIncertains = cartesTopoReferenceValuees.get(1).getPopNoeuds();
    IPopulation<Noeud> noeudsReferenceNonApparies = cartesTopoReferenceValuees.get(2).getPopNoeuds();

    // Récupération des arcs et des noeuds puis classement en "appariés", "incertains" ou
    // "non appariés" (BD comparaison)
    List<ReseauApp> cartesTopoComparaisonValuees = AppariementIO
        .scindeSelonValeursResultatsAppariement(carteTopoComparaison, valeursClassement);
    IPopulation<Arc> arcsComparaisonApparies = cartesTopoComparaisonValuees.get(0).getPopArcs();
    IPopulation<Arc> arcsComparaisonIncertains = cartesTopoComparaisonValuees.get(1).getPopArcs();
    IPopulation<Arc> arcsComparaisonNonApparies = cartesTopoComparaisonValuees.get(2).getPopArcs();
    IPopulation<Noeud> noeudsComparaisonApparies = cartesTopoComparaisonValuees.get(0).getPopNoeuds();
    IPopulation<Noeud> noeudsComparaisonIncertains = cartesTopoComparaisonValuees.get(1).getPopNoeuds();
    IPopulation<Noeud> noeudsComparaisonNonApparies = cartesTopoComparaisonValuees.get(2).getPopNoeuds();

    // Recalage 
    CarteTopo reseauRecale0 = Recalage.recalage(carteTopoReference, carteTopoComparaison, liens);


    // Affichage

    // Initiatlisation des fenêtres
    TaskFrame taskFrame = context.getWorkbenchFrame().getActiveTaskFrame();
    // TaskFrame taskFrameReference, taskFrameComparaison, taskFrameApp, taskFrameEval;
    TaskFrame taskFrameApp;
    TaskFrame taskFrameEval;
    // Task taskReference = new Task();
    // Task taskComparaison = new Task(); 
    Task taskApp = new Task();
    Task taskEval = new Task();

    // taskReference.setName(REFERENCE);
    // taskComparaison.setName(COMPARAISON);
    taskApp.setName(I18NPlug.getString("appariement.AppariementReseauxPlugIn.appeval"));
    taskEval.setName(I18NPlug.getString("appariement.AppariementReseauxPlugIn.appnonapp"));

    // Calcul des dimensions des fenêtres en fonction de la résolution de l'écran
    Dimension tailleEcran = Toolkit.getDefaultToolkit().getScreenSize();
    int width = (tailleEcran.width - 50) / 2;
    int height = (tailleEcran.height - 50) / 2 - (new Double(Math.floor(0.05 * tailleEcran.height))).intValue();

    if (recaler) {
        UtilJump.afficheCollection(context, reseauRecale0.getPopArcs(), RESEAU_RECALE, RESEAU_RECALE, Color.blue);
    }

    // FENETRE BD REFERENCE
    //context.getWorkbenchFrame().addTaskFrame(taskReference);
    //context.getActiveInternalFrame().setBounds(0, 0, width, height);
    //UtilJump.afficheCollection(context, tronconsReference, DONNEESBRUTES, ARCS, Color.blue);
    //UtilJump.afficheCollection(context, carteTopoReference.getPopArcs(), RESEAU, ARCS, Color.blue);
    //UtilJump.afficheCollection(context, carteTopoReference.getPopNoeuds(), RESEAU, NOEUDS, Color.blue);

    // FENETRE BD COMPARAISON
    //context.getWorkbenchFrame().addTaskFrame(taskComparaison);
    //context.getActiveInternalFrame().setBounds(width + 2, 0, width, height);
    //UtilJump.afficheCollection(context, tronconsComparaison, DONNEESBRUTES, ARCS, Color.black);
    //UtilJump.afficheCollection(context, carteTopoComparaison.getPopArcs(), RESEAU, ARCS, Color.black);
    //UtilJump.afficheCollection(context, carteTopoComparaison.getPopNoeuds(), RESEAU, NOEUDS, Color.black);

    // FENETRE APPARIEMENT
    taskFrameApp = context.getWorkbenchFrame().addTaskFrame(taskApp);
    //context.getActiveInternalFrame().setBounds(0, height + 2, width, height);
    UtilJump.afficheCollection(context, carteTopoReference.getPopArcs(), REFERENCE, ARCS, Color.blue);
    UtilJump.afficheCollection(context, carteTopoReference.getPopNoeuds(), REFERENCE, NOEUDS, Color.blue);
    UtilJump.afficheCollection(context, carteTopoComparaison.getPopArcs(), COMPARAISON, ARCS, Color.black);
    UtilJump.afficheCollection(context, carteTopoComparaison.getPopNoeuds(), COMPARAISON, NOEUDS, Color.black);
    UtilJump.afficheCollection(context, liensNuls, LIENS, LIENSPEUSURS, Color.red);
    UtilJump.afficheCollection(context, liensIncertains, LIENS, LIENSINCERTAINS, Color.orange);
    UtilJump.afficheCollection(context, liensSurs, LIENS, LIENSSURS, Color.green);

    // FENETRE EVALUATION
    taskFrameEval = context.getWorkbenchFrame().addTaskFrame(taskEval);
    //context.getActiveInternalFrame().setBounds(width+2,height+2,width,height);
    UtilJump.afficheCollection(context, arcsReferenceApparies, REFERENCE, ARCSAPP, Color.green);
    UtilJump.afficheCollection(context, arcsReferenceIncertains, REFERENCE, ARCSINCERTAINS, Color.orange);
    UtilJump.afficheCollection(context, arcsReferenceNonApparies, REFERENCE, ARCSNONAPP, Color.red);
    UtilJump.afficheCollection(context, noeudsReferenceApparies, REFERENCE, NOEUDSAPP, Color.green);
    UtilJump.afficheCollection(context, noeudsReferenceIncertains, REFERENCE, NOEUDSINCERTAINS, Color.orange);
    UtilJump.afficheCollection(context, noeudsReferenceNonApparies, REFERENCE, NOEUDSNONAPP, Color.red);

    UtilJump.afficheCollection(context, arcsComparaisonApparies, COMPARAISON, ARCSAPP, Color.green);
    UtilJump.afficheCollection(context, arcsComparaisonIncertains, COMPARAISON, ARCSINCERTAINS, Color.orange);
    UtilJump.afficheCollection(context, arcsComparaisonNonApparies, COMPARAISON, ARCSNONAPP, Color.red);
    UtilJump.afficheCollection(context, noeudsComparaisonApparies, COMPARAISON, NOEUDSAPP, Color.green);
    UtilJump.afficheCollection(context, noeudsComparaisonIncertains, COMPARAISON, NOEUDSINCERTAINS, Color.orange);
    UtilJump.afficheCollection(context, noeudsComparaisonNonApparies, COMPARAISON, NOEUDSNONAPP, Color.red);

    new MosaicInternalFramesPlugIn().execute(context);
    taskFrame.getLayerViewPanel().getViewport().zoomToFullExtent();
    taskFrameApp.getLayerViewPanel().getViewport().zoomToFullExtent();
    taskFrameEval.getLayerViewPanel().getViewport().zoomToFullExtent();
    //context.getLayerViewPanel().getViewport().zoomToFullExtent();
    
    MySynchro synchro = new MySynchro("");
    synchro.setWorkbenchContext(context.getWorkbenchContext());
    synchro.synchronize(true);
		
    return true;
  }
  
  // Classe permettant de contourner un probleme d'acces a la methode
  // setWorkbenchContext de SynchronizationPlugIn sans laquelle il est 
  // difficile d'utiliser le plugin (à améliorer coté OpenJUMP)
  class MySynchro extends SynchronizationPlugIn {
	  public MySynchro(String s) {
		  super(s);
	  }
	  protected void setWorkbenchContext(WorkbenchContext workbenchContext) {
		    super.setWorkbenchContext(workbenchContext);
	  }
  }
}