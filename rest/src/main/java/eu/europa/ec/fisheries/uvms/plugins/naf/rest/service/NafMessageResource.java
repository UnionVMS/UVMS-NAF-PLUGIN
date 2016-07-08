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
package eu.europa.ec.fisheries.uvms.plugins.naf.rest.service;

import eu.europa.ec.fisheries.uvms.plugins.naf.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.naf.constants.NafConfigKeys;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.plugins.naf.rest.dto.RestResponseCode;
import eu.europa.ec.fisheries.uvms.plugins.naf.service.PluginService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/message")
@Stateless
public class NafMessageResource {

    final static Logger LOG = LoggerFactory.getLogger(NafMessageResource.class);

    @EJB
    PluginService pluginService;
    @EJB
    StartupBean startupBean;
    @Context
    private HttpServletRequest httpRequest;

    /**
     *
     * @responseMessage 200 [Success]
     * @responseMessage 500 [Error]
     *
     * @summary Get a list of all exchangeLogs by search criterias
     *
     */
    @GET
    @Consumes(value = {"text/html; charset=UTF-8"})
    @Produces(value = {"text/html; charset=windows-1252"})
    @Path("/{message}")
    public Response getMessage(@PathParam("message") String message) {
        long in = System.currentTimeMillis();
        if (!startupBean.isIsEnabled()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        LOG.info("[ NAF INPUT  ]: {}", message);
        String authType = httpRequest.getAuthType();
        String useLocalStore = startupBean.getSetting(NafConfigKeys.USE_LOCAL_STORE);
        if ("false".equalsIgnoreCase(useLocalStore) || HttpServletRequest.CLIENT_CERT_AUTH.equals(authType)) {
            try {
                pluginService.setMessageReceived(message);
                LOG.info("Response status: {}", Response.Status.OK);
                long time = System.currentTimeMillis() - in;
                LOG.debug("Run time: " + time);
                return Response.ok(RestResponseCode.OK.toString()).build();
            } catch (Exception e) {
                LOG.info("Response status: {}", Response.Status.INTERNAL_SERVER_ERROR);
                LOG.error("[ Exception while handling NAF request ] {}", e);
                return Response.serverError().build();
            }
        } else {
            LOG.info("Response status: {}", Response.Status.UNAUTHORIZED);
            LOG.error("[ Unauthorized NAF request ]");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}