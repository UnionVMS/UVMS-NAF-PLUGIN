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

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.exchange.model.util.DateUtils;
import eu.europa.ec.fisheries.uvms.plugins.naf.*;
import eu.europa.ec.fisheries.uvms.plugins.naf.constants.NafCodes;
import eu.europa.ec.fisheries.uvms.plugins.naf.exception.PluginException;
import static eu.europa.ec.fisheries.uvms.plugins.naf.mapper.NafMessageResponseMapper.dateString;
import eu.europa.ec.fisheries.uvms.plugins.naf.util.DateUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class NafMessageResponseMapperTest {

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

            Assert.assertEquals(pluginName, retVal.getPluginName());
            Assert.assertEquals(PluginType.NAF, retVal.getPluginType());
            Assert.assertNotNull(retVal.getTimestamp());
            Assert.assertNotNull(retVal.getMovement());
        } catch (PluginException ex) {
            Logger.getLogger(NafMessageResponseMapperTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void mapEntryTest() {
        MovementBaseType movement = new MovementBaseType();

        String[] course = {NafCodes.COURSE, "10.5"};
        NafMessageResponseMapper.mapEntry(course, movement);
        Assert.assertEquals(10.5D, movement.getReportedCourse());

        String[] speed = {NafCodes.SPEED, "70"};
        NafMessageResponseMapper.mapEntry(speed, movement);
        Assert.assertEquals(7.0D, movement.getReportedSpeed());

        String[] date = {NafCodes.DATE, "20160208"};
        NafMessageResponseMapper.mapEntry(date, movement);
        Assert.assertEquals("20160208", NafMessageResponseMapper.dateString);

        String[] time = {NafCodes.TIME, "1527"};
        NafMessageResponseMapper.mapEntry(time, movement);
        Assert.assertEquals("1527", NafMessageResponseMapper.timeString);

        String[] externalMark = {NafCodes.EXTERNAL_MARK, "ABC-123"};
        NafMessageResponseMapper.mapEntry(externalMark, movement);
        Assert.assertEquals("ABC-123", movement.getExternalMarking());

        String[] flag = {NafCodes.FLAG, "SWE"};
        NafMessageResponseMapper.mapEntry(flag, movement);
        Assert.assertEquals("SWE", movement.getFlagState());

        String[] latN = {NafCodes.LATITUDE, "N1213"};
        NafMessageResponseMapper.mapEntry(latN, movement);
        Assert.assertEquals(12.2167D, movement.getPosition().getLatitude());

        String[] latS = {NafCodes.LATITUDE, "S1213"};
        NafMessageResponseMapper.mapEntry(latS, movement);
        Assert.assertEquals(-12.2167D, movement.getPosition().getLatitude());

        String[] lonN = {NafCodes.LONGITUDE, "E1417"};
        NafMessageResponseMapper.mapEntry(lonN, movement);
        Assert.assertEquals(14.2833D, movement.getPosition().getLongitude());

        String[] lonS = {NafCodes.LONGITUDE, "W1417"};
        NafMessageResponseMapper.mapEntry(lonS, movement);
        Assert.assertEquals(-14.2833D, movement.getPosition().getLongitude());

        String[] callSign = {NafCodes.RADIO_CALL_SIGN, "SMIT"};
        NafMessageResponseMapper.mapEntry(callSign, movement);
        Assert.assertEquals("SMIT", movement.getAssetId().getAssetIdList().get(0).getValue());
    }

    @Test
    public void mapDateTimeTest() {
        NafMessageResponseMapper.dateString = "20160208";
        NafMessageResponseMapper.timeString = "1558";
        MovementBaseType movement = new MovementBaseType();
        Date date = DateUtil.parseToUTCDateTime("20160208" + " " + "1558 UTC");

        NafMessageResponseMapper.mapDateTime(movement);
        Assert.assertEquals(movement.getPositionTime(), date);

        movement.setPositionTime(null);
        NafMessageResponseMapper.timeString = null;
        NafMessageResponseMapper.mapDateTime(movement);
        Assert.assertNull(movement.getPositionTime());

        movement.setPositionTime(null);
        NafMessageResponseMapper.timeString = "";
        NafMessageResponseMapper.mapDateTime(movement);
        Assert.assertNull(movement.getPositionTime());

        NafMessageResponseMapper.timeString = "1558";

        movement.setPositionTime(null);
        NafMessageResponseMapper.dateString = null;
        NafMessageResponseMapper.mapDateTime(movement);
        Assert.assertNull(movement.getPositionTime());

        movement.setPositionTime(null);
        NafMessageResponseMapper.dateString = "";
        NafMessageResponseMapper.mapDateTime(movement);
        Assert.assertNull(movement.getPositionTime());
    }

    @Test
    public void mapIRCSTest() {
        MovementBaseType movement = new MovementBaseType();
        NafMessageResponseMapper.mapIRCS("SMIT", movement);

        Assert.assertNotNull(movement.getAssetId());
        Assert.assertNotNull(movement.getAssetId().getAssetIdList());
        Assert.assertEquals(1, movement.getAssetId().getAssetIdList().size());
        Assert.assertEquals("SMIT", movement.getAssetId().getAssetIdList().get(0).getValue());
        Assert.assertEquals(AssetIdType.IRCS, movement.getAssetId().getAssetIdList().get(0).getIdType());
    }

    @Test
    public void mapCFRTest() {
        //MovementBaseType movement = new MovementBaseType();
        //movement.setInternalReferenceNumber("SWE000008121");
        //NafMessageResponseMapper.mapCFR("SWE000008121", movement);

        //Assert.assertNotNull(movement.getAssetId());
        //Assert.assertNotNull(movement.getAssetId().getAssetIdList());
        //Assert.assertEquals(1, movement.getAssetId().getAssetIdList().size());
        //Assert.assertEquals(1, movement.getAssetId().getAssetIdList().size());
       // Assert.assertEquals("SWE000008121", movement.getAssetId().getAssetIdList().get(0).getValue());
        //Assert.assertEquals(AssetIdType.CFR, movement.getAssetId().getAssetIdList().get(0).getIdType());
    }

    @Test
    public void getMovementPointTest() {
        MovementPoint point = NafMessageResponseMapper.getMovementPoint(new MovementBaseType());
        Assert.assertNotNull(point);
    }

    @Test
    public void mapSpeedTest() {
        MovementBaseType movement = new MovementBaseType();
        String speed = "105";
        NafMessageResponseMapper.mapSpeed(movement, speed);

        Assert.assertEquals(10.5, movement.getReportedSpeed());
    }
}