/*
 * Copyright 2012 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.interceptor;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.cpr.HeaderConfig;

import java.io.IOException;

/**
 * An Intewrceptor that send back to a websocket and http client the value of {@link HeaderConfig.X_ATMOSPHERE_TRACKING_ID}
 * and {@link HeaderConfig.X_CACHE_DATE}
 *
 * @author Jeanfrancois Arcand
 */
public class JavaScriptProtocol implements AtmosphereInterceptor {

    private String wsDelimiter = "|";

    @Override
    public void configure(AtmosphereConfig config) {
        String s = config.getInitParameter(ApplicationConfig.MESSAGE_DELIMITER);
        if (s != null) {
            wsDelimiter = s;
        }
    }

    @Override
    public Action inspect(AtmosphereResource r) {

        String uuid = r.getRequest().getHeader(HeaderConfig.X_ATMOSPHERE_TRACKING_ID);
        String handshakeUUID = r.getRequest().getHeader(HeaderConfig.X_ATMO_PROTOCOL);
        if (uuid != null && uuid.equals("0") && handshakeUUID != null) {
            r.getRequest().header(HeaderConfig.X_ATMO_PROTOCOL, null);
            r.getResponse().write(r.uuid() + wsDelimiter + System.currentTimeMillis());

            // We don't need to reconnect here
            if (r.transport() == AtmosphereResource.TRANSPORT.WEBSOCKET
                    || r.transport() == AtmosphereResource.TRANSPORT.STREAMING
                    || r.transport() == AtmosphereResource.TRANSPORT.SSE) {
                return Action.CONTINUE;
            } else {
                return Action.CANCELLED;
            }
        }
        return Action.CONTINUE;
    }

    @Override
    public void postInspect(AtmosphereResource r) {
    }

    @Override
    public String toString() {
        return "Atmosphere JavaScript Protocol";
    }
}
