package org.ozoneplatform.kernel.bundles.server.atmosphere;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.MeteorService;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Meteor;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Sean T Booker
 * Date: 1/16/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
@MeteorService(path = AtmosphereBusImpl.METEOR_PUB_SUB_MAPPING,
        interceptors = {BroadcastOnPostAtmosphereInterceptor.class, // for pushing messages to suspended connection
                AtmosphereResourceLifecycleInterceptor.class,       // for managing the connection lifecycle
                TrackMessageSizeInterceptor.class,                  // for making sure messages are delivered entirely
                HeartbeatInterceptor.class})                        // for keeping the connection active
public class MeteorPubSub extends HttpServlet {

    protected final LoggerService loggerService;

    //Used to handle JSON Mapping
    private final ObjectMapper mapper = new ObjectMapper();

    protected BundleContext bundleContext;

    public MeteorPubSub(){
        this.loggerService = new LoggerService(MeteorPubSub.class, bundleContext);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Create a Meteor
        Meteor m = Meteor.build(req);

        // Log all events on the console, including WebSocket events.
        m.addListener(new WebSocketEventListenerAdapter());

        res.setContentType("text/html;charset=ISO-8859-1");

        Broadcaster b = lookupBroadcaster(req.getPathInfo());
        m.setBroadcaster(b);

        loggerService.log(LogService.LOG_INFO, "[MeteorPubSub]: Subscribed to Broadcast from '" + b.toString() + "' {}");
        m.suspend(-1);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Broadcaster b = lookupBroadcaster(req.getPathInfo());

        String message = req.getReader().readLine();

        //TODO: Fix up to use JSON
        //mapper.writeValueAsString(mapper.readValue(message, Data.class))
        if (message != null && message.indexOf("message") != -1) {
            loggerService.log(LogService.LOG_INFO, "[MeteorPubSub]: Published : Broadcast to '" + b.toString() + "' { with Message }: " + message);
            b.broadcast(message.substring("message=".length()));
        }
    }

    Broadcaster lookupBroadcaster(String pathInfo) {
        String[] decodedPath = pathInfo.split("/");
        Broadcaster b = BroadcasterFactory.getDefault().lookup(decodedPath[decodedPath.length - 1], true);
        return b;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /*
            public final static class Data {

            private String message;
            private String author;
            private long time;

            public Data() {
                this("", "");
            }

            public Data(String author, String message) {
                this.author = author;
                this.message = message;
                this.time = new Date().getTime();
            }

            public String getMessage() {
                return message;
            }

            public String getAuthor() {
                return author;
            }

            public void setAuthor(String author) {
                this.author = author;
            }

            public void setMessage(String message) {
                this.message = message;
            }

            public long getTime() {
                return time;
            }

            public void setTime(long time) {
                this.time = time;
            }

        }
        */
}
