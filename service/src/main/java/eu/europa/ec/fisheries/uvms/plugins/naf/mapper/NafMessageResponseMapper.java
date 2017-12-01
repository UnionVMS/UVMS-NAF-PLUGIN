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
package eu.europa.ec.fisheries.uvms.plugins.naf.mapper;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementActivityType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.exchange.model.util.DateUtils;
import eu.europa.ec.fisheries.uvms.plugins.naf.constants.NafCodes;
import eu.europa.ec.fisheries.uvms.plugins.naf.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.naf.util.DateUtil;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 **/
public class NafMessageResponseMapper {
    
    final static Logger LOG = LoggerFactory.getLogger(ServiceMapper.class);
    
    static String dateString;
    static String timeString;
    
    public static SetReportMovementType mapToMovementType(String nafMessage, String pluginName) throws PluginException {
        dateString = "";
        timeString = "";
        
        SetReportMovementType movementType = new SetReportMovementType();
        if (nafMessage != null) {
            try {
                nafMessage = URLDecoder.decode(nafMessage, "UTF-8");
                String[] parts = nafMessage.split(NafCodes.DELIMITER);
                if (NafCodes.START_RECORD.equals(parts[1]) &&
                        NafCodes.END_RECORD.equals(parts[parts.length - 1])) {
                    MovementBaseType movement = new MovementBaseType();
                    movement.setComChannelType(MovementComChannelType.NAF);
                    movement.setSource(MovementSourceType.NAF);

                    for (int i = 2; i < parts.length - 1; i++) {
                        handleEntry(parts[i], movement);
                    }

                    mapDateTime(movement);

                    movementType.setMovement(movement);
                    movementType.setPluginType(PluginType.NAF);
                    movementType.setPluginName(pluginName);
                    movementType.setTimestamp(new Date());
                }
            } catch (UnsupportedEncodingException e) {
                throw new PluginException(e.getMessage());
            }
        }
        return movementType;
    }

    static void handleEntry(String part, MovementBaseType movement) {
        if (part != null) {
            String[] keyValuePair = part.split(NafCodes.SUBDELIMITER);
            if (keyValuePair.length == 2) {
                mapEntry(keyValuePair, movement);
            }
        }
    }

    static void mapEntry(String[] keyValuePair, MovementBaseType movement) throws NumberFormatException {
        String key = keyValuePair[0];
        if (key != null) {
            String value = keyValuePair[1];
            switch (key) {
                case NafCodes.RADIO_CALL_SIGN:
                    mapIRCS(value, movement);
                    break;
                case NafCodes.TRIP_NUMBER:
                    mapTripNumber(value, movement);
                    break;
                case NafCodes.VESSEL_NAME:
                    movement.setAssetName(value);
                    break;
                case NafCodes.INTERNAL_REFERENCE_NUMBER:
                    mapCFR(value, movement);
                    break;
                case NafCodes.EXTERNAL_MARK:
                    movement.setExternalMarking(value);
                    break;
                case NafCodes.LATITUDE:
                case NafCodes.LATITUDE_DECIMAL:
                    mapLatitude(movement, value, key);
                    break;
                case NafCodes.LONGITUDE:
                case NafCodes.LONGITUDE_DECIMAL:
                    mapLongitude(movement, value, key);
                    break;
                case NafCodes.SPEED:
                    mapSpeed(movement, value);
                    break;
                case NafCodes.COURSE:
                    movement.setReportedCourse(Double.parseDouble(value));
                    break;
                case NafCodes.DATE:
                    dateString = value;
                    break;
                case NafCodes.TIME:
                    timeString = value;
                    break;
                case NafCodes.ACTIVITY:
                    mapActivity(movement, value);
                case NafCodes.FLAG:
                    movement.setFlagState(value);
                    break;
                case NafCodes.TYPE_OF_MESSAGE:
                    movement.setMovementType(MovementTypeType.valueOf(value));
                case NafCodes.TO:
                default:
                    break;
            }
        }
    }

    static void mapTripNumber(String value, MovementBaseType movement) {
        try {
            Double tripNumber = Double.valueOf(value);
            movement.setTripNumber(tripNumber);
        } catch (NumberFormatException e) {
            LOG.error("Received malformed TN: {}", value);
        }
    }

    static void mapActivity(MovementBaseType movement, String value) {
        MovementActivityType activity = new MovementActivityType();
        activity.setMessageType(MovementActivityTypeType.valueOf(value));
        movement.setActivity(activity);
    }

    static void mapSpeed(MovementBaseType movement, String value) throws NumberFormatException {
        BigDecimal bd = new BigDecimal(Double.valueOf(value) / 10).setScale(4, RoundingMode.HALF_EVEN);
        double speed = bd.doubleValue();
        movement.setReportedSpeed(speed);
    }

    static void mapDateTime(MovementBaseType movement) {
        if ((dateString == null || dateString.isEmpty()) ||
                (timeString == null || timeString.isEmpty())) {
            return;
        }
        while (timeString.length() < 4) {
            timeString = "0" + timeString;
        }
        Date date = DateUtil.parseToUTCDateTime(dateString + " " + timeString + " UTC");
        LOG.info("Time String: {}; Converted date: {}", dateString + " " + timeString + " UTC", date);
        movement.setPositionTime(date);
    }

    static void mapIRCS(String value, MovementBaseType movement) {
    	if (movement.getAssetId() == null) {
    		AssetId assetId = new AssetId();
    		movement.setAssetId(assetId);
    	}
        AssetIdList ircs = new AssetIdList();
        ircs.setIdType(AssetIdType.IRCS);
        ircs.setValue(value);
        movement.getAssetId().getAssetIdList().add(ircs);
        movement.setIrcs(value);
    }

    static void mapCFR(String value, MovementBaseType movement) {
    	if (movement.getAssetId() == null) {
    		AssetId assetId = new AssetId();
    		movement.setAssetId(assetId);
    	}
        AssetIdList cfr = new AssetIdList();
        cfr.setIdType(AssetIdType.CFR);
        cfr.setValue(value);
        movement.getAssetId().getAssetIdList().add(cfr);
        movement.setInternalReferenceNumber(value);
    }

    static MovementPoint getMovementPoint(MovementBaseType movement) {
        MovementPoint pos = movement.getPosition();
        if (pos == null) {
            pos = new MovementPoint();
        }
        return pos;
    }

    static void mapLongitude(MovementBaseType movement, String value, String key) throws NumberFormatException {
        MovementPoint pos = getMovementPoint(movement);
        if (NafCodes.LONGITUDE_DECIMAL.equals(key)) {
            pos.setLongitude(Double.valueOf(value));
        } else {
            double decimalDegrees = positionStringToDecimalDegrees(value);
            if (value.charAt(0) == 'W') {
                decimalDegrees *= -1;
            }
            getMovementPoint(movement).setLongitude(decimalDegrees);
        }
        movement.setPosition(pos);
    }

    static void mapLatitude(MovementBaseType movement, String value, String key) throws NumberFormatException {
        MovementPoint pos = getMovementPoint(movement);
        if (NafCodes.LATITUDE_DECIMAL.equals(key)) {
            pos.setLatitude(Double.valueOf(value));
        } else {
            double decimalDegrees = positionStringToDecimalDegrees(value);
            if (value.charAt(0) == 'S') {
                decimalDegrees *= -1;
            }
            pos.setLatitude(decimalDegrees);
        }
        movement.setPosition(pos);
    }

    static double positionStringToDecimalDegrees(String value) {
        double deg = (charToDouble(value.charAt(1)) * 10) + charToDouble(value.charAt(2));
        double min = (charToDouble(value.charAt(3)) * 10) + charToDouble(value.charAt(4));
        double decimalDegrees = deg + (min / 60);
        BigDecimal bd = new BigDecimal(decimalDegrees).setScale(4, RoundingMode.HALF_EVEN);
        decimalDegrees = bd.doubleValue();
        return Double.valueOf(decimalDegrees);
    }
    
    static double charToDouble(char val) {
        String str = "" + val;
        return Double.valueOf(str);
    }
}