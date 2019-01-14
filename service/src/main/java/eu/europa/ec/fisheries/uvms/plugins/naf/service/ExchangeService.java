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
package eu.europa.ec.fisheries.uvms.plugins.naf.service;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.naf.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.naf.producer.PluginToExchangeProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.UUID;

/**
 **/
@Stateless
public class ExchangeService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

    @EJB
    private StartupBean startupBean;

    @EJB
    private PluginToExchangeProducer producer;

    @Asynchronous
    public void sendMovementReportToExchange(SetReportMovementType reportType, String userName) {
        try {
            String text = ExchangeModuleRequestMapper.createSetMovementReportRequest(reportType, userName, null, new Date(), null, PluginType.NAF, PluginType.NAF.value(), null);
            producer.sendModuleMessage(text,null);
        } catch (ExchangeModelMarshallException | MessageException e) {
            LOG.error("Couldn't map movement to String");
            startupBean.getCachedMovement().put(UUID.randomUUID().toString(), reportType);
        }
    }

}