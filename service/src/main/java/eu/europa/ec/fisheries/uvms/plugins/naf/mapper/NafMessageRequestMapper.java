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

import eu.europa.ec.fisheries.schema.exchange.common.v1.ReportType;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.plugins.naf.constants.NafCodes;

/**
 **/
public class NafMessageRequestMapper {
    
    public static String mapToVMSMessage(ReportType report) {
        MovementType movement = report.getMovement();
        StringBuilder naf = new StringBuilder();
        
        appendStartRecord(naf);
        
        // Actual data
        append(naf, NafCodes.TO, report.getRecipient());
        append(naf, NafCodes.TYPE_OF_MESSAGE, movement.getMovementType().name());
        if (!appendAsset(naf, NafCodes.RADIO_CALL_SIGN, AssetIdType.IRCS, movement)) {
            append(naf, NafCodes.RADIO_CALL_SIGN, movement.getIrcs());
        }
        append(naf, NafCodes.TRIP_NUMBER, movement.getTripNumber());
        append(naf, NafCodes.VESSEL_NAME, movement.getAssetName());
        append(naf, NafCodes.INTERNAL_REFERENCE_NUMBER, movement.getInternalReferenceNumber());
        append(naf, NafCodes.EXTERNAL_MARK, movement.getExternalMarking());
        appendPosition(naf, movement);
        if (movement.getReportedSpeed() != null) {
            append(naf, NafCodes.SPEED, (int) (movement.getReportedSpeed() * 10));
        }
        if (movement.getReportedCourse() != null) {
            append(naf, NafCodes.COURSE, movement.getReportedCourse().intValue());
        }
        append(naf, NafCodes.DATE, getDateString(movement));
        append(naf, NafCodes.TIME, getTimeString(movement));
        if (movement.getActivity() != null && movement.getActivity().getMessageType() != null) {
            append(naf, NafCodes.ACTIVITY, movement.getActivity().getMessageType().value());
        }
        
        appendEndRecord(naf);
        
        return naf.toString();
    }

    static void appendEndRecord(StringBuilder naf) {
        naf.append(NafCodes.END_RECORD);
        naf.append(NafCodes.DELIMITER);
    }

    static void appendStartRecord(StringBuilder naf) {
        naf.append(NafCodes.DELIMITER);
        naf.append(NafCodes.START_RECORD);
        naf.append(NafCodes.DELIMITER);
    }

    static void appendPosition(StringBuilder naf, MovementType movement) {
        if (MovementSourceType.MANUAL.equals(movement.getSource())) {
            append(naf, NafCodes.LATITUDE, getLatitudeString(movement.getPosition().getLatitude()));
            append(naf, NafCodes.LONGITUDE, getLongitudeString(movement.getPosition().getLongitude()));
        } else {
            append(naf, NafCodes.LATITUDE_DECIMAL, movement.getPosition().getLatitude());
            append(naf, NafCodes.LONGITUDE_DECIMAL, movement.getPosition().getLongitude());
        }
    }

    static boolean appendAsset(StringBuilder naf, String nafCode, AssetIdType assetId, MovementType movement) {
        if (movement.getAssetId() != null) {
            for (AssetIdList assetIdList : movement.getAssetId().getAssetIdList()) {
                if (assetId.equals(assetIdList.getIdType())) {
                    append(naf, nafCode, assetIdList.getValue());
                    return true;
                }
            }
        }
        
        return false;
    }

    static String getTimeString(MovementType movement) {
        StringBuilder time = new StringBuilder();
        int hour = movement.getPositionTime().getHour();
        if (hour < 10) {
            time.append(0);
        }
        time.append(hour);
        int min = movement.getPositionTime().getMinute();
        if (min < 10) {
            time.append("0");
        }
        time.append(min);
        return time.toString();
    }

    static String getDateString(MovementType movement) {
        StringBuilder date = new StringBuilder();
        date.append(movement.getPositionTime().getYear());
        int month = movement.getPositionTime().getMonth();
        if (month < 10) {
            date.append("0");
        }
        date.append(month);
        int day = movement.getPositionTime().getDay();
        if (day < 10) {
            date.append("0");
        }
        date.append(day);
        return date.toString();
    }
    
    static void append(StringBuilder naf, String key, Number value) {
        if (value != null && Math.floor(value.doubleValue()) == value.doubleValue()) {
            append(naf, key, String.valueOf(value.intValue()));
        } else {
            append(naf, key, String.valueOf(value));
        }
    }
    
    static void append(StringBuilder naf, String key, String value) {
        naf.append(key);
        naf.append(NafCodes.SUBDELIMITER);
        naf.append(value);
        naf.append(NafCodes.DELIMITER);
    }

    static String getLatitudeString(Double coord) {
        StringBuilder sb = new StringBuilder();
        
        if (coord < 0) {
            coord = -coord;
            sb.append("S");
        } else {
            sb.append("N");
        }
        
        int deg = (int) Math.floor(coord);
        int min = (int)((coord - deg) * 60);
        
        sb.append(deg);
        sb.append(min);
        return sb.toString();
    }

    static String getLongitudeString(Double coord) {
        StringBuilder sb = new StringBuilder();
        
        if (coord < 0) {
            coord = -coord;
            sb.append("W");
        } else {
            sb.append("E");
        }
        
        int deg = (int) Math.floor(coord);
        int min = (int)((coord - deg) * 60);
        
        sb.append(deg);
        sb.append(min);
        return sb.toString();
    }
}