package org.ozoneplatform.kernel.bundles.server.atmosphere;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Sean T Booker
 * Date: 1/16/13
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */
public final class LoggerService {

    protected Logger defatultLogger;

    private BundleContext context;

    public LoggerService(Class loggerClazz, BundleContext context){
        defatultLogger = LoggerFactory.getLogger(loggerClazz);
        this.context = context;
    }

    public void log(int level, String message) {
        LogService log = getLogger();

        if (log != null) {
            log.log(level,message);
        }
        else {
            switch(level){
                case LogService.LOG_DEBUG:
                    defatultLogger.debug(message);
                    break;
                case LogService.LOG_INFO:
                    defatultLogger.info(message);
                    break;
                case LogService.LOG_WARNING:
                    defatultLogger.warn(message);
                    break;
                case LogService.LOG_ERROR:
                    defatultLogger.error(message);
                    break;
                default:
                    System.out.println("LOG [AtmosphereBusImpl]: " + message);
                    break;
            }
        }
    }

    protected LogService getLogger() {
        if(context != null){
            ServiceReference logRef = context.getServiceReference(LogService.class.getName());
            if (logRef != null) {
                return (LogService) context.getService(logRef);
            }
        }
        return null;
    }
}
