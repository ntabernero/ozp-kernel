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
package org.ozoneplatform.kernel.bundles.server.atmosphere;


import org.atmosphere.config.service.MeteorService;
import org.atmosphere.handler.ReflectorServletProcessor;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.*;

import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Created with IntelliJ IDEA.
 * User: Sean T Booker
 * Date: 1/14/13
 * Time: 2:35 PM
 * To change this template use File | Settings | File Templates.
 */

class AtmosphereBusImpl extends AtmosphereServlet implements AtmosphereBus, BundleActivator {

    protected static final Logger logger = LoggerFactory.getLogger(AtmosphereBusImpl.class);

    private String mapping;

    private final List<AtmosphereInterceptor> interceptors = new ArrayList<AtmosphereInterceptor>();

    private BundleContext context;

    public AtmosphereBusImpl() {
        this(false);
    }

    public AtmosphereBusImpl(boolean isFilter) {
        super(isFilter, false);
    }

    // ----------------------------
    //   BundleActivator impl.
    // ----------------------------
    public void start(BundleContext bundleContext) throws Exception {
         this.context = bundleContext;

         if(mapping == null){
             mapping = DEFAULT_MAPPING;
         }
        logger.info("Have AtmosphereBusImpl {} mapped to {}", mapping);

        //Add Interceptors....
            // for pushing messages to suspended connection
        interceptors.add(new BroadcastOnPostAtmosphereInterceptor());
            // for managing the connection lifecycle
        interceptors.add(new AtmosphereResourceLifecycleInterceptor());
            // for making sure messages are delivered entirely
        interceptors.add(new TrackMessageSizeInterceptor());
            // for keeping the connection active
        interceptors.add(new HeartbeatInterceptor());

         //Register HTTP Servlet via whiteboard...
        //Wire up MeteorPubSub servlet
        //Wire up MeteorSerlvet to point to this MeteorPubSub



    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

    // ----------------------------
    //   AtmosphereBus impl.
    // ----------------------------

    /**
     * @return The AtmosphereFramework default BroadcasterFactory.
     */
    public BroadcasterFactory getBroadcasterFactory(){
        return framework.getBroadcasterFactory();
    }

    /**
     * Add a new AtmosphereHandler to the framework with the given mapping.
     * Be aware that the mapping is added to the atmosphere root, if the root
     * is /atmosphere, then the handler will be accessible through the
     * /atmosphere/<mapping> address.
     * @param hMapping The AtmosphereHandler mapping. (e.g /toto)
     * @param handler The AtmosphereHandler to be added.
     * @throws IllegalArgumentException if an handler is already linked to this mapping.
     */
    public void addAtmosphereHandler(String hMapping, AtmosphereHandler handler){
        framework.addAtmosphereHandler(constructMapping(hMapping), handler);
    }

    public void addAtmosphereHandler(String hMapping, AtmosphereHandler handler, List<AtmosphereInterceptor> interceptors){
        framework.addAtmosphereHandler(constructMapping(hMapping), handler, interceptors);

    }

    public void addAtmosphereHandler(String hMapping, AtmosphereHandler handler, Broadcaster broadcaster, List<AtmosphereInterceptor> interceptors){
        framework.addAtmosphereHandler(constructMapping(hMapping), handler, broadcaster, interceptors);
    }

    /**
     * Remove the existing AtmosphereHandler linked to the given mapping.
     * @param hMapping The existing AtmosphereHandler mapping.
     */
    public void removeAtmosphereHandler(String hMapping){
        framework.removeAtmosphereHandler(constructMapping(hMapping));
    }


    /**
     * Construct the AtmosphereHandler mapping.
     * @param hMapping the given mapping.
     * @return the correct mapping.
     */
    private String constructMapping(String hMapping){
        return (mapping.equals("/") ? "" : mapping) + (hMapping.startsWith("/") ? hMapping : "/" + hMapping);
    }


    @Override
    public void init(final ServletConfig sc) throws ServletException {
        super.init(sc);

        ReflectorServletProcessor r = new ReflectorServletProcessor();
        r.setServletClassName(MeteorPubSub.class.getName());

        addAtmosphereHandler(mapping, r, interceptors);
        framework.initAtmosphereHandler(sc);
    }

    @Override
    public void destroy() {
        super.destroy();
        Meteor.cache.clear();
    }


    @MeteorService
    protected class MeteorPubSub extends HttpServlet {

        @Override
        public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
            // Create a Meteor
            Meteor m = Meteor.build(req);

            // Log all events on the console, including WebSocket events.
            m.addListener(new WebSocketEventListenerAdapter());

            res.setContentType("text/html;charset=ISO-8859-1");

            Broadcaster b = lookupBroadcaster(req.getPathInfo());
            m.setBroadcaster(b);

            m.suspend(-1);
        }

        public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
            Broadcaster b = lookupBroadcaster(req.getPathInfo());

            String message = req.getReader().readLine();

            if (message != null && message.indexOf("message") != -1) {
                b.broadcast(message.substring("message=".length()));
            }
        }

        Broadcaster lookupBroadcaster(String pathInfo) {
            String[] decodedPath = pathInfo.split("/");
            Broadcaster b = BroadcasterFactory.getDefault().lookup(decodedPath[decodedPath.length - 1], true);
            return b;
        }
    }

}
