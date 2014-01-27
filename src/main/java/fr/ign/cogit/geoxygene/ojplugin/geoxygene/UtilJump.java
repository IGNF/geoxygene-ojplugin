package fr.ign.cogit.geoxygene.ojplugin.geoxygene;
        

import java.awt.Color;
import java.awt.Paint;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.FillPatternFactory;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringEndpointStyle.SolidEnd;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_Feature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.SchemaDefaultFeature;
import fr.ign.cogit.geoxygene.util.algo.JtsAlgorithms;
import fr.ign.cogit.geoxygene.util.conversion.AdapterFactory;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;

/**
 * Cette classe contient des méthodes utiles pour l'affichage et la conversion
 * des objets GeOxygene vers des objets JUMP dans les deux sens, la gestion des
 * sélections d'objets et des sélection de couches.
 * 
 * English : JUMP functions for GeOxygene
 * 
 * @author Olivier Bonin - IGN / Laboratoire COGIT
 * @author Eric Grosso - IGN / Laboratoire COGIT
 * @author Julien Perret - IGN / Laboratoire COGIT
 */
public class UtilJump {
	/**
	 * Static attribute to determine if we have to export links to openjump.
	 * Attribut statique pour exporter les liens vers OpenJump ou non.
	 */
	public static boolean exportLinks = true;

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP comme des surfaces.
	 * Cette méthode est notamment pratique pour dessiner des routes à trois bandes : les lignes passées
	 * en paramètre sont transformées en surface à l'aide d'un tampon et c'est cette surface qui est affichée.
	 * Attention : tous les objets de la collection sont fusionnés en un seul et les attributs (qui n'ont plus de sens)
	 * ne sont pas ajoutés.
	 * 
	 * On précise notamment la  couleur voulue, le groupe dans laquelle la couche apparaîtra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param lineColor couleur des lignes des objets à afficher
	 * @param lineWidth épaisseur de la ligne
	 * @param alpha transparence des objets à afficher
	 * @param paintIndex index du motif à utiliser pour le remplissage des objets à afficher
	 * @param sizeBuffer taille du tampon utilisé sur les objets de la collection
	 */
	public static void afficheCollectionCommeSurface (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, String descr, Color fillColor, Color lineColor, int lineWidth, int alpha, int paintIndex, double sizeBuffer){
		if (coll.size() ==0) return;
		try {
			FeatureSchema schema = new FeatureSchema();
			schema.addAttribute("Geometrie",AttributeType.GEOMETRY); //$NON-NLS-1$
			FeatureCollection collObjets = new FeatureDataset(schema);
			BasicFeature feature = new BasicFeature(schema);
			Geometry[] geometries = new Geometry[coll.size()];
			for (int i = 0; i<coll.size(); i++) geometries[i]=fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene.makeJtsGeom(coll.get(i).getGeom()).buffer(sizeBuffer);
			feature.setGeometry(JtsAlgorithms.union(geometries));
			collObjets.add(feature);
			
			// Le problème est que la frame active n'est pas forcément une TaskFrame.
			// Par défaut, on affiche dans la première TasFrame qui se présente.
			JInternalFrame[] frames = context.getWorkbenchFrame().getInternalFrames();
			TaskFrame taskframe;
			for (int i = 0; i < frames.length; i++) {
				if(frames[i] instanceof TaskFrame){
					taskframe = (TaskFrame) frames[i];
					Layer layer = new Layer(descr,fillColor,collObjets,taskframe.getLayerManager());
					layer.getBlackboard().put("Collection",coll); //$NON-NLS-1$
					taskframe.getLayerManager().addLayer(groupe,layer);
					if ((paintIndex!=-1)||(alpha!=layer.getBasicStyle().getAlpha())) {
						BasicStyle basicStyle = new BasicStyle();
						basicStyle.setFillColor(fillColor);
						basicStyle.setLineColor(lineColor);
						basicStyle.setAlpha(alpha);
						basicStyle.setRenderingFill(true);
						basicStyle.setLineWidth(lineWidth);
						if (paintIndex!=-1) {
							basicStyle.setRenderingFillPattern(true);
							FillPatternFactory patternFactory = new FillPatternFactory();
							Paint[] paintArray = patternFactory.createFillPatterns();
							if (paintIndex < paintArray.length) {
								basicStyle.setFillPattern(paintArray[paintIndex]);
							}
						}
						layer.removeStyle(layer.getBasicStyle());
						layer.addStyle(basicStyle);
						layer.fireAppearanceChanged();
					}
					return;
				}
			}
			JOptionPane.showMessageDialog(null, "Il faut au moins une TaskFrame ouverte"); //$NON-NLS-1$
			return;

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaîtra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param groupIndex indice du groupe (catégorie)
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param lineColor couleur des lignes des objets à afficher
	 * @param lineWidth épaisseur de la ligne
	 * @param alpha transparence des objets à afficher
	 * @param paintIndex index du motif à utiliser pour le remplissage des objets à afficher
	 * @param arrowEnd vrai si on affiche une flèche de fin de ligne
	 */
	public static Layer afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, int groupIndex, String descr, Color fillColor, Color lineColor,
			int lineWidth, int alpha, int paintIndex, boolean arrowEnd, String labelName){
		Layer layer = null;
		try {
			FeatureSchema schema = new FeatureSchema();
			schema.addAttribute("Geometrie",AttributeType.GEOMETRY); //$NON-NLS-1$
			FeatureCollection collObjets = new FeatureDataset(schema);
			BasicFeature feature = new BasicFeature(schema);

			if (coll.isEmpty()) {
				return layer;
			}

			// On crée le schema en récupérant les java beans.

			IFeature ft_feature = coll.get(0);
			Method[] methodes = ft_feature.getClass().getMethods();
			for (int l = 0; l < methodes.length; l++){
				if(methodes[l].getName().substring(0,3).equals("get") & //$NON-NLS-1$
						methodes[l].getParameterTypes().length == 0){
					AttributeType type = toAttributeType(methodes[l].getReturnType());
					if (type != AttributeType.OBJECT) {
						schema.addAttribute(nomAttribut(methodes[l].getName()),type);
					}
				}
			}
			if (exportLinks) {
				schema.addAttribute("Lien",AttributeType.OBJECT); //$NON-NLS-1$
			}
			for (int i = 0; i<coll.size(); i++){
				ft_feature = coll.get(i);
				feature = new BasicFeature(schema);
				feature.setGeometry(fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene.makeJtsGeom(ft_feature.getGeom()));
				if (exportLinks) {
					feature.setAttribute("Lien",ft_feature); //$NON-NLS-1$
				}
				// On renseigne la valeur des attributs dans Jump.
				for (int l=0; l < methodes.length; l++){
					if(methodes[l].getName().substring(0,3).equals("get") && //$NON-NLS-1$
							methodes[l].getParameterTypes().length == 0 &&
							(methodes[l].invoke(ft_feature,(Object[]) null) != null)
					){
						AttributeType type = toAttributeType(methodes[l].getReturnType());
						if (type != AttributeType.OBJECT) {
							feature.setAttribute(nomAttribut(methodes[l].getName()),
									methodes[l].invoke(ft_feature,(Object[]) null));
						}
					}
				}
				collObjets.add(feature);
			}

			// Le problème est que la frame active n'est pas forcément une TaskFrame.
			// Par défaut, on affiche dans la première TasFrame qui se présente.
			JInternalFrame[] frames = context.getWorkbenchFrame().getInternalFrames();
			TaskFrame taskframe;
			for (int i = 0; i < frames.length; i++) {
				if(frames[i] instanceof TaskFrame){
					taskframe = (TaskFrame) frames[i];
					layer = new Layer(descr,fillColor,collObjets,taskframe.getLayerManager());
					layer.getBlackboard().put("Collection",coll); //$NON-NLS-1$
					//taskframe.getLayerManager().addLayer(groupe,layer);
					if (groupIndex == -1) {
						taskframe.getLayerManager().addCategory(groupe);
					} else {
						taskframe.getLayerManager().addCategory(groupe, groupIndex);
					}
					Category cat = taskframe.getLayerManager().getCategory(groupe);
					cat.add(cat.getLayerables().size(), layer);
					if ((paintIndex!=-1)||(alpha!=layer.getBasicStyle().getAlpha())||
							(lineWidth!=1)||(lineColor!=layer.getBasicStyle().getLineColor())) {
						BasicStyle basicStyle = new BasicStyle();
						basicStyle.setFillColor(fillColor);
						basicStyle.setLineColor(lineColor);
						basicStyle.setAlpha(alpha);
						basicStyle.setRenderingFill(true);
						basicStyle.setLineWidth(lineWidth);
						if (paintIndex!=-1) {
							basicStyle.setRenderingFillPattern(true);
							FillPatternFactory patternFactory = new FillPatternFactory();
							Paint[] paintArray = patternFactory.createFillPatterns();
							if (paintIndex < paintArray.length) {
								basicStyle.setFillPattern(paintArray[paintIndex]);
							}
						}
						layer.removeStyle(layer.getBasicStyle());
						layer.addStyle(basicStyle);
						if (arrowEnd) { layer.addStyle(new SolidEnd()); }
						if (labelName != null) {
							
							LabelStyle labelStyle = new LabelStyle();
							labelStyle.setEnabled(true);
							labelStyle.setAttribute(labelName);
							layer.addStyle(labelStyle);
						}
						layer.fireAppearanceChanged();
					}
					return layer;
				}
			}
			JOptionPane.showMessageDialog(null, "Il faut au moins une TaskFrame ouverte"); //$NON-NLS-1$
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return layer;
	}


	private static AttributeType toAttributeType(Class<?> returnType) {
		if (Integer.class.isAssignableFrom(returnType)) {
			return AttributeType.INTEGER;
		}
		if (Double.class.isAssignableFrom(returnType)) {
			return AttributeType.DOUBLE;
		}
		if (String.class.isAssignableFrom(returnType)) {
			return AttributeType.STRING;
		}
		if (Date.class.isAssignableFrom(returnType)) {
			return AttributeType.DATE;
		}
		if (returnType.isPrimitive()) {
			if (Integer.TYPE.isAssignableFrom(returnType)) {
				return AttributeType.INTEGER;
			}
			if (Double.TYPE.isAssignableFrom(returnType)) {
				return AttributeType.DOUBLE;
			}
		}
		//if (GM_Object.class.isAssignableFrom(returnType)) {
//			return AttributeType.GEOMETRY;
		//}
		return AttributeType.OBJECT;
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param lineColor couleur des lignes des objets à afficher
	 * @param lineWidth épaisseur de la ligne
	 * @param alpha transparence des objets à afficher
	 * @param paintIndex index du motif à utiliser pour le remplissage des objets à afficher
	 * @param arrowEnd vrai si on affiche une flèche de fin de ligne
	 */
	public static void afficheCollection(PlugInContext context,
	    IFeatureCollection<?> coll, String groupe, String descr,
			Color fillColor, Color lineColor, int lineWidth, int alpha,
			int paintIndex, boolean arrowEnd, String labelName) {
		afficheCollection(context, coll, groupe, -1, descr, fillColor,
				lineColor, lineWidth, alpha, paintIndex, arrowEnd, labelName);
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param alpha transparence des objets à afficher
	 * @param paintIndex index du motif à utiliser pour le remplissage des objets à afficher
	 * 
	 * @see #afficheCollection(PlugInContext, FT_FeatureCollection, String, String, Color, Color, int, int, int)
	 */
	public static void afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, String descr, Color fillColor, int alpha, int paintIndex){
		afficheCollection(context, coll, groupe, -1, descr, fillColor, fillColor.darker(), 1, alpha,
				paintIndex, false, null);
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param alpha transparence des objets à afficher
	 * 
	 * @see #afficheCollection(PlugInContext, FT_FeatureCollection, String, String, Color, Color, int, int, int)
	 */
	public static void afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, String descr, Color fillColor, int alpha){
		afficheCollection (context,coll,groupe,descr,fillColor,alpha,-1);
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param lineColor couleur des lignes des objets à afficher
	 * @param lineWidth épaisseur de la ligne
	 * @param alpha transparence des objets à afficher
	 * 
	 * @see #afficheCollection(PlugInContext, FT_FeatureCollection, String, String, Color, Color, int, int, int)
	 */
	public static Layer afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, String descr, Color fillColor, Color lineColor, int lineWidth , int alpha){
		return afficheCollection (context,coll,groupe,-1,descr,fillColor,lineColor,lineWidth, alpha,-1, false, null);
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param lineColor couleur des lignes des objets à afficher
	 * @param lineWidth épaisseur de la ligne
	 * 
	 * @see #afficheCollection(PlugInContext, FT_FeatureCollection, String, String, Color, Color, int, int, int)
	 */
	public static Layer afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, String descr, Color fillColor, Color lineColor, int lineWidth){
		return afficheCollection (context,coll,groupe,descr,fillColor,lineColor,lineWidth,150);
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * @param lineColor couleur des lignes des objets à afficher
	 * 
	 * @see #afficheCollection(PlugInContext, FT_FeatureCollection, String, String, Color, Color, int, int, int)
	 */
	public static Layer afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, String descr, Color fillColor, Color lineColor){
		return afficheCollection (context,coll,groupe,descr,fillColor,lineColor,1);
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * 
	 * @see #afficheCollection(PlugInContext, FT_FeatureCollection, String, String, Color, Color, int, int, int)
	 */
	public static Layer afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, String descr, Color fillColor){
		return afficheCollection (context,coll,groupe,descr,fillColor,fillColor.darker());
	}

	/**
	 * Affichage d'une FT_FeatureCollection de GeOxygene dans JUMP. On précise notamment la
	 * couleur voulue, le groupe dans laquelle la couche apparaitra ("working"
	 * par exemple), et le nom donné à la couche. Techniquement, on utilise le
	 * blackboard de jump pour garder le lien de la couche vers la
	 * FT_FeatureCollection, et on crée un attribut de type objet pour chaque
	 * objet jump pointant vers l'objet GeOxygene correspondant.
	 * 
	 * On crée à la volée les attributs JUMP correspondant aux attributs GeOxygene,
	 * mais sans les typer.
	 * @param context contexte du plugin
	 * @param coll collection d'objets à afficher
	 * @param groupe nom du groupe d'objets
	 * @param groupIndex indice du groupe (catégorie)
	 * @param descr description de la couche
	 * @param fillColor couleur de remplissage des objets à afficher
	 * 
	 * @see #afficheCollection(PlugInContext, FT_FeatureCollection, String, String, Color, Color, int, int, int)
	 */
	public static void afficheCollection (PlugInContext context,IFeatureCollection<?> coll,
			String groupe, int groupIndex, String descr, Color fillColor){
		afficheCollection (context,coll,groupe,groupIndex,descr,fillColor,fillColor.darker(),
				1, 150, -1, false, null);
	}

	/** Renvoie une FT_FeatureCollection contenant les objets Sélectionnés dans
	 * l'interface graphique JUMP.
	 */
	@SuppressWarnings("unchecked")
	public static IFeatureCollection<IFeature> convertSelection (PlugInContext context) {
		ArrayList<?> objSel = new ArrayList();
		Layer[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
		for (int i=0; i < selectedLayers.length; i++){
			Layer layer = selectedLayers[i];
			objSel.addAll(context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(layer));
		}
		IFeatureCollection<IFeature> poptest = new FT_FeatureCollection<IFeature>();
		if (objSel.size()>0){
			for (int i=0; i<objSel.size();i++){
				BasicFeature select = (BasicFeature) objSel.get(i);
				poptest.add((IFeature) select.getAttribute(1));
			}
		}
		return poptest;
	}

	/**
	 * Crée une liste de FT_Features correspondant aux features sélectionnés.
	 *
	 * @param context
	 *            contexte du plugin
	 * @param layer
	 *            jeu de données sélectionné
	 * @return liste de IFeatures correspondant aux features sélectionnés
	 */
	public static IFeatureCollection<IFeature> convertSelection(
			PlugInContext context, Layer layer) {
		// .. on récupère les objets sélectionnés (de cette couche)
		Collection<?> objSel = context.getLayerViewPanel()
				.getSelectionManager().getFeaturesWithSelectedItems(layer);

		// si des objets sont sélectionnés dans la couche...
		IFeatureCollection<IFeature> collectionObjetsSelectionnes = new FT_FeatureCollection<IFeature>();
		// création du schéma et du feature type
		SchemaDefaultFeature schemaDefaultFeature = featureSchemaToSchemaConceptuel(layer.getFeatureCollectionWrapper().getFeatureSchema());
        collectionObjetsSelectionnes.setFeatureType(schemaDefaultFeature.getFeatureType());

		for (Object o : objSel) {
			BasicFeature select = (BasicFeature) o; // ... alors on les
													// convertit en FT_Feature
			// (passage d'OpenJump - BasicFeature - à GeOxygene - FT_Feature)
			try {
				// Si les données sont des BasicFeature avec un "Lien" vers des
				// FT_Feature (PostGIS)
				int indexAttribute = select.getSchema().getAttributeIndex(
						"Lien"); //$NON-NLS-1$
				collectionObjetsSelectionnes.add((FT_Feature) select
						.getAttribute(indexAttribute));
				// Lien entre features openjump et geox
			} catch (IllegalArgumentException exception) {
			  // Sinon, récupérer la géométrie uniquement
			  IFeature feature = UtilJump.basicFeatureToFT_Feature(select, schemaDefaultFeature);
			  collectionObjetsSelectionnes.add(feature);
			}
		}
		return collectionObjetsSelectionnes;
	}

	/**
	 * Renvoie une ArryList des FT_FeatureCollection correspondant aux thèmes
	 * actifs dans le panneau de gauche de JUMP.
	 */
	public static ArrayList<Object> convertSelectedLayers(PlugInContext context) {
		Layer[] selectedLayers = context.getLayerNamePanel()
				.getSelectedLayers();
		ArrayList<Object> selectedPops = new ArrayList<Object>();
		for (int i = 0; i < selectedLayers.length; i++) {
			Layer layer = selectedLayers[i];
			selectedPops.add(layer.getBlackboard().get("Collection")); //$NON-NLS-1$
		}
		return selectedPops;
	}

	/**
	 * Renvoie une FT_FeatureCollection contenant tous les objets des thèmes
	 * actifs dans le panneau de gauche de JUMP.
	 */
	public static IFeatureCollection<IFeature> getObjectsFromSelectedLayers (PlugInContext context) {
		Layer[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
		IFeatureCollection<IFeature> poptest = new FT_FeatureCollection<IFeature>();
		Feature feature;
		for (int i=0; i < selectedLayers.length; i++){
			Layer layer = selectedLayers[i];
			for (int j=0; j< layer.getFeatureCollectionWrapper().size(); j++){
				feature = (Feature) layer.getFeatureCollectionWrapper().getFeatures().get(j);
				poptest.add((IFeature) feature.getAttribute(1));
			}
		}
		return poptest;
	}

	/**
	 * Renvoie une FT_FeatureCollection contenant tous les objets d'un thème
	 * actif dans le panneau de gauche de JUMP.
	 */
	public static IFeatureCollection<IFeature> getObjectsFromSelectedLayer (PlugInContext context, String nom) {
		Layer[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
		Layer layer = null;
		for (int i = 0; i < selectedLayers.length && layer == null; i++){
			if (selectedLayers[i].getName().equalsIgnoreCase(nom)) {
				layer = selectedLayers[i];
			}
		}
		return convertLayer(layer);
	}

	/**
	 * Convert the features of an OpenJump Layer into GeOxygene Features and
	 * return them in a featurecollection.
	 * @param layer
	 *            the layer to convert
	 * @return a feature collection containing all features in the layer
	 *         converted into geoxygene features
	 */
	public static IFeatureCollection<IFeature> convertLayer(Layer layer) {
		IFeatureCollection<IFeature> result = new FT_FeatureCollection<IFeature>();
		SchemaDefaultFeature schemaDefaultFeature = featureSchemaToSchemaConceptuel(layer
				.getFeatureCollectionWrapper().getFeatureSchema());
		result.setFeatureType(schemaDefaultFeature.getFeatureType());

		for (int j = 0; j < layer.getFeatureCollectionWrapper().size(); j++) {
			BasicFeature feature = (BasicFeature) layer
					.getFeatureCollectionWrapper().getFeatures().get(j);
			// if the feature collection was created from a geoxygene feature
			// collection (afficheCollection)
			if (FT_Feature.class.isAssignableFrom(feature.getAttribute(1)
					.getClass())) {
				result.add((FT_Feature) feature.getAttribute(1));
			} else {
				result.add(UtilJump.basicFeatureToFT_Feature(feature,
						schemaDefaultFeature));
			}
		}
		return result;
	}

	/**
	 * Guess the name of the attribute corresponding to the given method name.
	 * @param nomMethode the name of a method
	 * @return the name of the attribute corresponding to the given method name
	 */
	public static String nomAttribut(String nomMethode){
		return nomMethode.substring(3,4).toLowerCase() + nomMethode.substring(4);
	}

	/**
	 * Convert an OpenJump FeatureSchema to a GeOxygene Schema.
	 * @param schema a schema
	 * @return a GeOxygene Schema corresponding to the given FeatureSchema
	 */
	public static SchemaDefaultFeature featureSchemaToSchemaConceptuel(FeatureSchema schema) {
		// création du schéma
	    SchemaDefaultFeature schemaDefaultFeature = new SchemaDefaultFeature();
        /** créer un featuretype de jeu correspondant */
        fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType
        newFeatureType= new fr.ign.cogit.geoxygene.schema
        .schemaConceptuelISOJeu.FeatureType();

        int nbFields = schema.getAttributeCount();
        Map<Integer,String[]> attLookup = new HashMap<Integer,String[]>();
        for (int i = 0; i < nbFields; i++) {
        	fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType type = new fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType();
            String nomField = schema.getAttributeName(i);
            String memberName = schema.getAttributeName(i);
            String valueType = schema.getAttributeType(i).toJavaClass().getSimpleName();
            type.setNomField(nomField);
            type.setMemberName(memberName);
            type.setValueType(valueType);
            newFeatureType.addFeatureAttribute(type);
            attLookup.put(new Integer(i), new String[]{nomField,memberName});
        }
        int geomIndex = schema.getGeometryIndex();
        Class<?> geomClass = schema.getAttributeType(geomIndex).toJavaClass();
        /** création d'un schéma associé au featureType */
        newFeatureType.setGeometryType(AdapterFactory.toGeometryType(geomClass));
        schemaDefaultFeature.setFeatureType(newFeatureType);
        newFeatureType.setSchema(schemaDefaultFeature);
        schemaDefaultFeature.setAttLookup(attLookup);
		return schemaDefaultFeature;
	}

	/**
	 * Convert a BasicFeature to a IFeature.
	 * @param basicFeature
	 *            the feature to convert
	 * @param schema the schema of the feature
	 * @return a IFeature corresponding to the given BasicFeature
	 */
	public static IFeature basicFeatureToFT_Feature(
			BasicFeature basicFeature,
			SchemaDefaultFeature schema) {
		DefaultFeature feature = new DefaultFeature();
		feature.setSchema(schema);
		feature.setFeatureType(schema.getFeatureType());
		feature.setAttributes(new Object[schema.getFeatureAttributes().size()]);
		for (int i = 0; i < basicFeature.getSchema().getAttributeCount(); i++) {
			String attributeName = basicFeature.getSchema().getAttributeName(i);
			feature.setAttribute(attributeName, basicFeature.getAttribute(i));
		}
		try {feature.setGeom(JtsGeOxygene.makeGeOxygeneGeom(basicFeature.getGeometry()));}
		catch (Exception e) {e.printStackTrace();}
		return feature;
	}
	
	/** 
     * Renvoie une FT_FeatureCollection contenant tous les objets d'une Layer Jump
     * @throws Exception 
     */
    public static FT_FeatureCollection<DefaultFeature> getObjectsFromLayer (Layer layer) throws Exception {
        FT_FeatureCollection<DefaultFeature> poptest = new FT_FeatureCollection<DefaultFeature>();
        Feature feature;
        for (int j=0; j < layer.getFeatureCollectionWrapper().size(); j++) {
            feature = (Feature) layer.getFeatureCollectionWrapper().getFeatures().get(j);
            DefaultFeature featureGeo = new DefaultFeature();
            featureGeo.setGeom(fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene.makeGeOxygeneGeom(feature.getGeometry()));
            //poptest.add((FT_Feature) feature.getAttribute(1));
            poptest.add(featureGeo);
        }
        return poptest;
    }
}