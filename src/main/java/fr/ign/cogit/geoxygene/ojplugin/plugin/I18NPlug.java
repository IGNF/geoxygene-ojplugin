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
 *  Copyright (C) 2009 Institut Géographique National
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

package fr.ign.cogit.geoxygene.ojplugin.plugin;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18NPlug {
    
    /**
     * Private Bundle name pointing to where the language files are.
     */
    private static final String BUNDLE_NAME = "language/traduction"; //$NON-NLS-1$

    /**
     * Private resource Bundle using the bundle name and default locale.
     * @see #BUNDLE_NAME
     * @see #getString(String)
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
        .getBundle(I18NPlug.BUNDLE_NAME, Locale.getDefault());

    /**
     * Private Default Constructor.
     */
    private I18NPlug() {
    }
        
    /**
     * @param key string identifier of the internationalised test
     * @return the internationalised string corresponding to the given key
     */
    public static String getString(final String key) {
      try {
        return I18NPlug.RESOURCE_BUNDLE.getString(key);
      } catch (MissingResourceException e) {
        return '!' + key + '!';
      }
    }
    
}