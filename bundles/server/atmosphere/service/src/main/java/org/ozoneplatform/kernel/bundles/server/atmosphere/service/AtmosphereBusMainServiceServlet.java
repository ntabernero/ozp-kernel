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
package org.ozoneplatform.kernel.bundles.server.atmosphere.service;


import org.atmosphere.cache.HeaderBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.container.JettyAsyncSupportWithWebSocket;
import org.atmosphere.cpr.*;
import org.atmosphere.handler.ReflectorServletProcessor;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.data.PubSubData;
import org.ozoneplatform.kernel.bundles.server.atmosphere.service.handler.servlet.AtmosphereBusHandlerServlet;
import org.ozoneplatform.kernel.bundles.server.atmosphere.service.util.LoggerService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: Sean T Booker
 * Date: 1/14/13
 * Time: 2:35 PM
 * To change this template use File | Settings | File Templates.
 */

public class AtmosphereBusMainServiceServlet extends AtmosphereServlet implements AtmosphereBus {

    protected LoggerService loggerService;

    protected static final String PUB_SUB_INNER_MAPPING = "/*";

    protected BundleContext bundleContext;

    protected AtmosphereBusHandlerServlet atmosphereBusHandlerServlet;

    protected final List<AtmosphereInterceptor> interceptors = new ArrayList<AtmosphereInterceptor>();

    protected final List<String> mappings = new ArrayList<String>();

    protected PubSubData pubSubData;

    public AtmosphereBusMainServiceServlet() {
        super(false);
    }

     public PubSubData getPubSubData() {
        return pubSubData;
    }

    public void setPubSubData(PubSubData pubSubData) {
        this.pubSubData = pubSubData;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        Dictionary props = new Hashtable();
        props.put("alias", METEOR_PUB_SUB_MAPPING);
        props.put("servlet-name", "Atmoshere Bus Servlet");
        String[] registerServiceNames = {HttpServlet.class.getName(), AtmosphereBus.class.getName()};
        bundleContext.registerService(registerServiceNames, this, props);

        loggerService = new LoggerService(AtmosphereBusMainServiceServlet.class, bundleContext);
        loggerService.log(LogService.LOG_INFO, "[AtmosphereBus]: Started '" + AtmosphereBusMainServiceServlet.class.getName() + "' {} " + METEOR_PUB_SUB_MAPPING);
    }

    @Override
    public void destroy(){
        removeAtmosphereHandler(PUB_SUB_INNER_MAPPING);
        interceptors.clear();
        loggerService.log(LogService.LOG_INFO, "[AtmosphereBus]: Stopped '" + AtmosphereBusMainServiceServlet.class.getName() + "' {} " + METEOR_PUB_SUB_MAPPING);
        super.destroy();
    }

    // ----------------------------
    //   AtmosphereBus impl.
    // ----------------------------

    public Broadcaster lookupBroadcaster(String pathInfo) {
        String[] decodedPath = pathInfo.split("/");
        Broadcaster b = getBroadcasterFactory().lookup(decodedPath[decodedPath.length - 1], true);
        return b;
    }

    /**
     * @return The AtmosphereFramework default BroadcasterFactory.
     */
    public BroadcasterFactory getBroadcasterFactory(){
        return framework.getBroadcasterFactory();
    }

    public AtmosphereFramework getFramework(){
        return this.framework();
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
    	String mapping = constructMapping(hMapping);
    	mappings.add(mapping);
        framework.addAtmosphereHandler(mapping, handler);
    }

    public void addAtmosphereHandler(String hMapping, AtmosphereHandler handler, List<AtmosphereInterceptor> interceptors){
    	String mapping = constructMapping(hMapping);
    	mappings.add(mapping);
        framework.addAtmosphereHandler(mapping, handler, interceptors);

    }

    public void addAtmosphereHandler(String hMapping, AtmosphereHandler handler, Broadcaster broadcaster, List<AtmosphereInterceptor> interceptors){
    	String mapping = constructMapping(hMapping);
    	mappings.add(mapping);
        framework.addAtmosphereHandler(mapping, handler, broadcaster, interceptors);
    }

    /**
     * Remove the existing AtmosphereHandler linked to the given mapping.
     * @param hMapping The existing AtmosphereHandler mapping.
     */
    public void removeAtmosphereHandler(String hMapping){
    	String mapping = constructMapping(hMapping);
    	if(mappings.remove(mapping)){//If indeed the mapping was removed, pass that along...
    		framework.removeAtmosphereHandler(mapping);
    	}
    }

    public void publish(String publishTopic, String author, String message) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        Broadcaster b = lookupBroadcaster(publishTopic);

        if (message != null && message.indexOf("message") != -1) {
            loggerService.log(LogService.LOG_INFO, "[MeteorPubSub]: Published : Broadcast to '" + b.toString() + "' { with Message }: " + message);
            pubSubData.setAuthor(author);
            pubSubData.setMessage(message.substring("message=".length()));
            b.broadcast(mapper.writeValueAsString(pubSubData));
        }
    }

    public void subscribe(HttpServletRequest req, HttpServletResponse res, String subscribeTopic)  throws IOException{

        // Create a Meteor
        Meteor m = Meteor.build(req);

        // Log all events on the console, including WebSocket events.
        m.addListener(new WebSocketEventListenerAdapter());

        res.setContentType("text/html;charset=ISO-8859-1");

        Broadcaster b = lookupBroadcaster(subscribeTopic);
        m.setBroadcaster(b);

        loggerService.log(LogService.LOG_INFO, "[MeteorPubSub]: Subscribed to Broadcast from '" + b.toString() + "' {}");
        m.suspend(-1);
     }

    /**
     * Construct the AtmosphereHandler mapping.
     * @param hMapping the given mapping.
     * @return the correct mapping.
     */
    private String constructMapping(String hMapping){
        return (METEOR_PUB_SUB_MAPPING.equals("/") ? "" : METEOR_PUB_SUB_MAPPING) + (hMapping.startsWith("/") ? hMapping : "/" + hMapping);
    }

    // ----------------------------
    //   AtmosphereServlet impl.
    // ----------------------------
    @Override
    public void init(ServletConfig sc) throws ServletException {

        //interceptors.add(new BroadcastOnPostAtmosphereInterceptor());           // for pushing messages to suspended connection
        interceptors.add(new AtmosphereResourceLifecycleInterceptor());          // for managing the connection lifecycle
        interceptors.add(new TrackMessageSizeInterceptor());            // for making sure messages are delivered entirely
        interceptors.add(new HeartbeatInterceptor());           // for keeping the connection active

        framework.addInitParameter("org.atmosphere.useNative", "true");

        //for caching message.
        framework.setBroadcasterCacheClassName(HeaderBroadcasterCache.class.getName());

        atmosphereBusHandlerServlet = new AtmosphereBusHandlerServlet(this);

        ReflectorServletProcessor reflectorServletProcessor = new ReflectorServletProcessor(atmosphereBusHandlerServlet);

        reflectorServletProcessor.setServletClassName(AtmosphereBusHandlerServlet.class.getName());

        addAtmosphereHandler(PUB_SUB_INNER_MAPPING, reflectorServletProcessor, interceptors);

        super.init(sc);


        framework.setAsyncSupport(new JettyAsyncSupportWithWebSocket(framework.getAtmosphereConfig()));


        framework.initAtmosphereHandler(sc);
     }
}
