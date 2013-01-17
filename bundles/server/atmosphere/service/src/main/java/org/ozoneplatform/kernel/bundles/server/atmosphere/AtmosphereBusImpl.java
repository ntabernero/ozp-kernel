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


import org.atmosphere.cache.HeaderBroadcasterCache;
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
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;
import org.ops4j.pax.web.extender.whiteboard.ExtenderConstants;
import org.ops4j.pax.web.service.WebContainerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.log.LogService;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.Servlet;
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

public class AtmosphereBusImpl implements AtmosphereBus, BundleActivator {

    protected LoggerService loggerService;

    public static final String METEOR_PUB_SUB_SERVLET_NAME = "KernelAtmosphereMeteorPubSubServlet";
    public static final String METEOR_PUB_SUB_CONTEXT_ID = "pubsub";
    public static final String METEOR_PUB_SUB_MAPPING = "/" + METEOR_PUB_SUB_CONTEXT_ID;

    private ServiceRegistration m_meteorPubSubServletReg;

    private final AtmosphereFramework framework;

    private BundleContext context;

    public AtmosphereBusImpl() {
        //Instantiate the AtmosphereFramework
        framework = new AtmosphereFramework(false, false);
     }


    // ----------------------------
    //   BundleActivator impl.
    // ----------------------------
    public void start(BundleContext bundleContext) throws Exception {
         this.context = bundleContext;

        loggerService = new LoggerService(AtmosphereBusImpl.class, context);

        //for caching message.
        framework.setBroadcasterCacheClassName(HeaderBroadcasterCache.class.getName());

         //Register HTTP Servlet via whiteboard...
        //Wire up MeteorPubSub servlet
        //Dictionary props = new Hashtable();
        //props.put( ExtenderConstants.PROPERTY_ALIAS, METEOR_PUB_SUB_MAPPING );
        //props.put( ExtenderConstants.PROPERTY_HTTP_CONTEXT_ID, METEOR_PUB_SUB_CONTEXT_ID );
        //props.put( WebContainerConstants.SERVLET_NAME, METEOR_PUB_SUB_SERVLET_NAME );
        //Init Params
            /*  You can force this Servlet to use native API of the Web Server instead of
             *  the Servlet 3.0 Async API you are deploying on by adding  */
        //props.put( "org.atmosphere.useNative" ,true);
        //m_meteorPubSubServletReg = bundleContext.registerService( Servlet.class.getName(), new MeteorPubSub(), props );

        loggerService.log(LogService.LOG_INFO, "[AtmosphereBusImpl]: Registered '" + METEOR_PUB_SUB_SERVLET_NAME + "' {} " + METEOR_PUB_SUB_MAPPING);
     }

    public void stop(BundleContext bundleContext) throws Exception {
        removeAtmosphereHandler(METEOR_PUB_SUB_MAPPING);
        //m_meteorPubSubServletReg.unregister();
        //m_meteorPubSubServletReg = null;
        loggerService.log(LogService.LOG_INFO, "[AtmosphereBusImpl]: Unregistered '" + METEOR_PUB_SUB_SERVLET_NAME + "' {} " + METEOR_PUB_SUB_MAPPING);
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
        return (METEOR_PUB_SUB_MAPPING.equals("/") ? "" : METEOR_PUB_SUB_MAPPING) + (hMapping.startsWith("/") ? hMapping : "/" + hMapping);
    }

    public ServiceRegistration getM_meteorPubSubServletReg() {
        return m_meteorPubSubServletReg;
    }

    public void setM_meteorPubSubServletReg(ServiceRegistration m_meteorPubSubServletReg) {
        this.m_meteorPubSubServletReg = m_meteorPubSubServletReg;
    }
}
