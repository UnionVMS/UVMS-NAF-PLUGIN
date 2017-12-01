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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.plugins.naf.constants.NafCodes;
import eu.europa.ec.fisheries.uvms.plugins.naf.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.naf.util.DateUtil;

/**
 * Unit test for simple App.
 */
public class NafMessageResponseMapperTest {
	
    private static final double DELTA_VALUE = 0.001;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void mapToMovementTypeTest() {
        try {
            String nafMessage = "//SR//FR/SWE//AD/XNE//TM/POS//RD/20050110//RT/1201//RN/3434//RC/SSSS//ER//";
            String pluginName = "NafMapperTest";
            SetReportMovementType retVal = NafMessageResponseMapper.mapToMovementType(nafMessage, pluginName);

            assertEquals(pluginName, retVal.getPluginName());
            assertEquals(PluginType.NAF, retVal.getPluginType());
            assertNotNull(retVal.getTimestamp());
            assertNotNull(retVal.getMovement());
        } catch (PluginException ex) {
            Logger.getLogger(NafMessageResponseMapperTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void mapEntryTest() {
        MovementBaseType movement = new MovementBaseType();

        String[] course = {NafCodes.COURSE, "10.5"};
        NafMessageResponseMapper.mapEntry(course, movement);
		assertEquals(10.5D, movement.getReportedCourse().doubleValue(), DELTA_VALUE);

        String[] speed = {NafCodes.SPEED, "70"};
        NafMessageResponseMapper.mapEntry(speed, movement);
        assertEquals(7.0D, movement.getReportedSpeed().doubleValue(), DELTA_VALUE);

        String[] date = {NafCodes.DATE, "20160208"};
        NafMessageResponseMapper.mapEntry(date, movement);
        assertEquals("20160208", NafMessageResponseMapper.dateString);

        String[] time = {NafCodes.TIME, "1527"};
        NafMessageResponseMapper.mapEntry(time, movement);
        assertEquals("1527", NafMessageResponseMapper.timeString);

        String[] externalMark = {NafCodes.EXTERNAL_MARK, "ABC-123"};
        NafMessageResponseMapper.mapEntry(externalMark, movement);
        assertEquals("ABC-123", movement.getExternalMarking());

        String[] flag = {NafCodes.FLAG, "SWE"};
        NafMessageResponseMapper.mapEntry(flag, movement);
        assertEquals("SWE", movement.getFlagState());

        String[] latN = {NafCodes.LATITUDE, "N1213"};
        NafMessageResponseMapper.mapEntry(latN, movement);
        assertEquals(12.2167D, movement.getPosition().getLatitude().doubleValue(), DELTA_VALUE);

        String[] latS = {NafCodes.LATITUDE, "S1213"};
        NafMessageResponseMapper.mapEntry(latS, movement);
        assertEquals(-12.2167D, movement.getPosition().getLatitude().doubleValue(), DELTA_VALUE);

        String[] lonN = {NafCodes.LONGITUDE, "E1417"};
        NafMessageResponseMapper.mapEntry(lonN, movement);
        assertEquals(14.2833D, movement.getPosition().getLongitude().doubleValue(), DELTA_VALUE);

        String[] lonS = {NafCodes.LONGITUDE, "W1417"};
        NafMessageResponseMapper.mapEntry(lonS, movement);
        assertEquals(-14.2833D, movement.getPosition().getLongitude().doubleValue(), DELTA_VALUE);

        String[] callSign = {NafCodes.RADIO_CALL_SIGN, "SMIT"};
        NafMessageResponseMapper.mapEntry(callSign, movement);
        assertEquals("SMIT", movement.getAssetId().getAssetIdList().get(0).getValue());
    }

    @Test
    public void mapDateTimeTest() {
        NafMessageResponseMapper.dateString = "20160208";
        NafMessageResponseMapper.timeString = "1558";
        MovementBaseType movement = new MovementBaseType();
        Date date = DateUtil.parseToUTCDateTime("20160208" + " " + "1558 UTC");

        NafMessageResponseMapper.mapDateTime(movement);
        assertEquals(movement.getPositionTime(), date);

        movement.setPositionTime(null);
        NafMessageResponseMapper.timeString = null;
        NafMessageResponseMapper.mapDateTime(movement);
        assertNull(movement.getPositionTime());

        movement.setPositionTime(null);
        NafMessageResponseMapper.timeString = "";
        NafMessageResponseMapper.mapDateTime(movement);
        assertNull(movement.getPositionTime());

        NafMessageResponseMapper.timeString = "1558";

        movement.setPositionTime(null);
        NafMessageResponseMapper.dateString = null;
        NafMessageResponseMapper.mapDateTime(movement);
        assertNull(movement.getPositionTime());

        movement.setPositionTime(null);
        NafMessageResponseMapper.dateString = "";
        NafMessageResponseMapper.mapDateTime(movement);
        assertNull(movement.getPositionTime());
    }

    @Test
    public void mapIRCSTest() {
        MovementBaseType movement = new MovementBaseType();
        NafMessageResponseMapper.mapIRCS("SMIT", movement);

        assertNotNull(movement.getAssetId());
        assertNotNull(movement.getAssetId().getAssetIdList());
        assertEquals(1, movement.getAssetId().getAssetIdList().size());
        assertEquals("SMIT", movement.getAssetId().getAssetIdList().get(0).getValue());
        assertEquals(AssetIdType.IRCS, movement.getAssetId().getAssetIdList().get(0).getIdType());
    }

    @Test
    public void mapCFRTest() {
        MovementBaseType movement = new MovementBaseType();
        NafMessageResponseMapper.mapCFR("SWE000008121", movement);

        assertNotNull(movement.getAssetId());
        assertNotNull(movement.getAssetId().getAssetIdList());
        assertEquals(1, movement.getAssetId().getAssetIdList().size());
        assertEquals(1, movement.getAssetId().getAssetIdList().size());
        assertEquals("SWE000008121", movement.getInternalReferenceNumber());
        assertEquals("SWE000008121", movement.getAssetId().getAssetIdList().get(0).getValue());
        assertEquals(AssetIdType.CFR, movement.getAssetId().getAssetIdList().get(0).getIdType());
    }

    @Test
    public void getMovementPointTest() {
        MovementPoint point = NafMessageResponseMapper.getMovementPoint(new MovementBaseType());
        assertNotNull(point);
    }

    @Test
    public void mapSpeedTest() {
        MovementBaseType movement = new MovementBaseType();
        String speed = "105";
        NafMessageResponseMapper.mapSpeed(movement, speed);

        assertEquals(10.5, movement.getReportedSpeed().doubleValue(), DELTA_VALUE);
    }
}