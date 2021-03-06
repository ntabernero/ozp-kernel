package org.ozoneplatform.kernel.modules.server.bundletracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.atmosphere.cpr.Broadcaster;
import org.json.JSONException;
import org.json.JSONStringer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;

public class Tracker implements BundleActivator {
  private BundleTracker tracker;
  private BundleContext context;

  public static final String TRACKER_PUBSUB_EVENT_TOPIC = "bundletracker";

  protected AtmosphereBus atmosphereBus;

  //this must be a synchronized data structure
  private static List<Map> events = new Vector<Map>();
  private static Map<String,Map<String,Object>> currentState = new Hashtable<String,Map<String,Object>>();

  public void start(final BundleContext context) {
    this.context = context;
    tracker = new BundleTracker(context,
            Bundle.ACTIVE | Bundle.STOPPING | Bundle.UNINSTALLED,
            null) {
      @Override
      public Object addingBundle(Bundle bundle, BundleEvent event) {
        if (bundle.getSymbolicName() != null && bundle.getHeaders().get("WebClientPlugin-Resources") != null) {
          log(LogService.LOG_INFO,"addingBundle:FoundBlogBundle:" + bundle + ", Bundle State:" + bundle.getState() + ", Event Type:" + (event != null ? event.getType() : null));
          log(LogService.LOG_INFO,"addingBundle:FoundBlogBundle:" + bundle + ", Headers:" + bundle.getHeaders());

          if (bundle.getState() == Bundle.ACTIVE) {
            addEvent("install",bundle);
          }

          return bundle;
        }
        else {
          return null;
        }
      }

      @Override
      public void modifiedBundle(Bundle bundle, BundleEvent event, Object obj) {
        log(LogService.LOG_INFO,"modifiedBundle:Bundle:" + bundle + ", Bundle State:" + bundle.getState() + ", Event Type:" + (event != null ? event.getType() : null));
        log(LogService.LOG_INFO,"modifiedBundle:Bundle:" + bundle + ", Headers:" + bundle.getHeaders());

        if (bundle.getState() == Bundle.STOPPING || bundle.getState() == Bundle.UNINSTALLED) {
          addEvent("uninstall",bundle);
        }

      }

      @Override
      public void removedBundle(Bundle bundle, BundleEvent event, Object obj) {
        log(LogService.LOG_INFO,"removedBundle:Bundle:" + bundle + ", Bundle State:" + bundle.getState() + ", Event Type:" + (event != null ? event.getType() : null));
        log(LogService.LOG_INFO,"removedBundle:Bundle:" + bundle + ", Headers:" + bundle.getHeaders());
      }
    };
    tracker.open();
  }

  public void stop(BundleContext bc) throws Exception {
    tracker.close();
  }

  private void addEvent(String eventType, Bundle bundle)  {
    Map<String,Object> e = new HashMap<String,Object>();

    e.put("Date",new Date());
    e.put("Bundle-SymbolicName",bundle.getSymbolicName());
    e.put("WebClientPlugin-EventType",eventType);
    e.put("WebClientPlugin-Resources",bundle.getHeaders().get("WebClientPlugin-Resources"));

    events.add(e);

    currentState.put(bundle.getSymbolicName(),e);

    log(LogService.LOG_INFO,"addEvent:" + e);

    try{
        //Broadcast Event
        log(LogService.LOG_INFO,"BroadCasting Event :" + e + " {} to Topic: " + TRACKER_PUBSUB_EVENT_TOPIC);
        Broadcaster b = getAtmosphereBusService().lookupBroadcaster(TRACKER_PUBSUB_EVENT_TOPIC);
        b.broadcast(getCurrentStateJson());
    }catch (JSONException jse){
        log(LogService.LOG_ERROR,"JSONException in BundleTracker: " + jse.getStackTrace());
    }
  }

  public static List getEvents() {
    return events;
  }

  //this could be called by any client browser
  public static String getEventsJson() throws JSONException {
    JSONStringer json = new JSONStringer();

    json.array();
    for (Map event : events) {
      json.object()
              .key("Date")
              .value(event.get("Date"))
              .key("Bundle-SymbolicName")
              .value(event.get("Bundle-SymbolicName"))
              .key("WebClientPlugin-EventType")
              .value(event.get("WebClientPlugin-EventType"))
              .key("WebClientPlugin-Resources")
              .value(event.get("WebClientPlugin-Resources"))
          .endObject();
    }
    json.endArray();

    //todo figureout when to clear event list
    //clearEvents();

    return json.toString();
  }
  public static Map<String,Map<String,Object>> getCurrentState() {
    return currentState;
  }

  //this could be called by any client browser
  public static String getCurrentStateJson() throws JSONException {
    JSONStringer json = new JSONStringer();

    json.object();
    for (String bundleName : currentState.keySet()) {
      Map event = currentState.get(bundleName);
      json.key(bundleName);
      json.object()
              .key("Date")
              .value(event.get("Date"))
              .key("Bundle-SymbolicName")
              .value(event.get("Bundle-SymbolicName"))
              .key("WebClientPlugin-EventType")
              .value(event.get("WebClientPlugin-EventType"))
              .key("WebClientPlugin-Resources")
              .value(event.get("WebClientPlugin-Resources"))
          .endObject();
    }
    json.endObject();

    return json.toString();
  }

  public static void clearEvents() {
    events.clear();
  }

  private LogService getLog() {
    ServiceReference logRef = context.getServiceReference(LogService.class.getName());
    if (logRef != null) {
      return (LogService) context.getService(logRef);
    }
    return null;
  }

  private void log(int level, String message) {
    LogService log = getLog();

    if (log != null) {
      log.log(level,message);
    }
    else {
      System.out.println("LOG " + message);
    }
  }

    protected AtmosphereBus getAtmosphereBusService(){
        if(atmosphereBus == null){
            ServiceReference atmosphereBusServiceRef = context.getServiceReference(AtmosphereBus.class.getName());
            if (atmosphereBusServiceRef != null) {
                atmosphereBus = (AtmosphereBus) context.getService(atmosphereBusServiceRef);
            }
        }
        return atmosphereBus;
    }

    public AtmosphereBus getAtmosphereBus() {
        return atmosphereBus;
    }

    public void setAtmosphereBus(AtmosphereBus atmosphereBus) {
        this.atmosphereBus = atmosphereBus;
    }
}
