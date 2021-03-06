/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2013-2014 ForgeRock AS.
 */

package org.forgerock.jaspi.test.server;

import org.forgerock.jaspi.test.server.endpoint.EndpointResource;
import org.forgerock.jaspi.test.server.endpoint.ModuleConfigurationResource;
import org.forgerock.json.resource.Resources;
import org.forgerock.json.resource.Router;

import javax.servlet.ServletException;

public class ConnectionFactory {

    public static org.forgerock.json.resource.ConnectionFactory getConnectionFactory() throws ServletException {
        try {
            Router router = new Router();

            router.addRoute("/configure", ModuleConfigurationResource.INSTANCE);
            router.addRoute("/endpoint", new EndpointResource());
            router.addRoute("/status", new StatusResource());

            return Resources.newInternalConnectionFactory(router);
        } catch (final Exception e) {
            throw new ServletException(e);
        }
    }
}
