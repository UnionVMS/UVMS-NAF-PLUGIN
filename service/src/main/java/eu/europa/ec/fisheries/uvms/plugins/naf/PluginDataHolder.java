/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.plugins.naf;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 **/
public abstract class PluginDataHolder {

    public final static String PLUGIN_PROPERTIES = "naf.properties";
    public final static String PROPERTIES = "settings.properties";
    public final static String CAPABILITIES = "capabilities.properties";

    private Properties nafApplicaitonProperties;
    private Properties nafProperties;
    private Properties nafCapabilities;

    private final ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> capabilities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SetReportMovementType> cachedMovement = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, String> getSettings() {
        return settings;
    }

    public ConcurrentHashMap<String, String> getCapabilities() {
        return capabilities;
    }

    public ConcurrentHashMap<String, SetReportMovementType> getCachedMovement() {
        return cachedMovement;
    }

    public Properties getPluginApplicaitonProperties() {
        return nafApplicaitonProperties;
    }

    public void setPluginApplicaitonProperties(Properties nafApplicaitonProperties) {
        this.nafApplicaitonProperties = nafApplicaitonProperties;
    }

    public Properties getPluginProperties() {
        return nafProperties;
    }

    public void setPluginProperties(Properties nafProperties) {
        this.nafProperties = nafProperties;
    }

    public Properties getPluginCapabilities() {
        return nafCapabilities;
    }

    public void setPluginCapabilities(Properties nafCapabilities) {
        this.nafCapabilities = nafCapabilities;
    }

}