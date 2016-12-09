/*
 Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
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
import eu.europa.ec.fisheries.uvms.plugins.naf.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;


@WebServlet(name="nafServlet", urlPatterns={"/rest/message/*"},
        initParams={ @WebInitParam(name="simpleParam", value="paramValue") } )

public class NafServlet extends HttpServlet {

    final static Logger LOG = LoggerFactory.getLogger(NafServlet.class);

    @EJB
    PluginService pluginService;
    @EJB
    StartupBean startupBean;

    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse response) throws ServletException, IOException {
        long in = System.currentTimeMillis();
        if (!startupBean.isIsEnabled()) {
            respond(response, 404, "NOK");
            return;
        }
        StringBuffer url = httpRequest.getRequestURL();
        if (url == null) {
            respond(response, 400, "Bad request");
            return;
        }
        String[] parts = url.toString().split("/");
        if (parts.length < 1) {
            respond(response, 400, "Bad request");
            return;
        }
        String message = parts[parts.length - 1];
        message = URLDecoder.decode(message, "iso-8859-1");
        message = new String(message.getBytes("ISO-8859-1"),"UTF-8");


        LOG.info("[ NAF INPUT  ]: {}", message);
        String authType = httpRequest.getAuthType();
        String useLocalStore = startupBean.getSetting(NafConfigKeys.USE_LOCAL_STORE);
        if ("false".equalsIgnoreCase(useLocalStore) || HttpServletRequest.CLIENT_CERT_AUTH.equals(authType)) {
            try {
                pluginService.setMessageReceived(message);
                long time = System.currentTimeMillis() - in;
                LOG.debug("Run time: " + time);
                respond(response, 200, "OK");
                return;
            } catch (Exception e) {
                LOG.error("[ Exception while handling NAF request ] {}", e);
                respond(response, 500, "NOK");
                return;
            }
        } else {
            LOG.error("[ Unauthorized NAF request ]");
            respond(response, 401, "Unauthorized");
            return;
        }

    }

    private void respond(HttpServletResponse response, int status, String returnValue) throws IOException {
        LOG.info("Response status: {}", status);
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.println(returnValue);
        out.close();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

}