/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.ozoneplatform.kernel.bundles.server.atmosphere.api;

import org.atmosphere.cpr.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: Sean T Booker
 * Date: 1/14/13
 * Time: 1:47 PM
 *
 * OSGi Bus which represents an AtmosphereFramework.
 * Thanks to this bus you can add AtmosphereHandler and get the BroadcasterFactory at runtime.
 */
public interface AtmosphereBus {

    public static final String METEOR_PUB_SUB_MAPPING = "/pubsub";


    void publish(String publishTopic, String author, String message) throws IOException;

    void subscribe(HttpServletRequest req, HttpServletResponse res, String subscribeTopic) throws IOException;

    Broadcaster lookupBroadcaster(String pathInfo);

    AtmosphereFramework getFramework();

    /**
     * @return The AtmosphereFramework default BroadcasterFactory.
     */
    BroadcasterFactory getBroadcasterFactory();

    /**
     * Add a new AtmosphereHandler to the framework with the given mapping.
     * Be aware that the mapping is added to the atmosphere root, if the root
     * is /atmosphere, then the handler will be accessible through the
     * /atmosphere/<mapping> address.
     * @param mapping The AtmosphereHandler mapping. (e.g /toto)
     * @param handler The AtmosphereHandler to be added.
     * @throws IllegalArgumentException if an handler is already linked to this mapping.
     */
    void addAtmosphereHandler(String mapping, AtmosphereHandler handler);

    void addAtmosphereHandler(String mapping, AtmosphereHandler handler, List<AtmosphereInterceptor> interceptors);

    void addAtmosphereHandler(String mapping, AtmosphereHandler handler, Broadcaster broadcaster, List<AtmosphereInterceptor> interceptors);

    /**
     * Remove the existing AtmosphereHandler linked to the given mapping.
     * @param mapping The existing AtmosphereHandler mapping.
     */
    void removeAtmosphereHandler(String mapping);




}
