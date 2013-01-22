package org.ozoneplatform.kernel.bundles.server.atmosphere.rest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.atmosphere.config.service.MeteorService;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResponse;
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;

@Path("/pubsub/{topic}")
@Produces("application/json")
public class PubSubController {

    @Context
    private MessageContext messageContext;

    @PathParam("topic")
    private String topic;

    @QueryParam("author")
    private String author;
    
    private AtmosphereBus atmosphereBus;

    public AtmosphereBus getAtmosphereBus() {
        return atmosphereBus;
    }

    public void setAtmosphereBus(AtmosphereBus atmosphereBus) {
        this.atmosphereBus = atmosphereBus;
    }

    @GET
    public Response pub() {
        if(atmosphereBus != null){
            HttpServletRequest req = (HttpServletRequest) messageContext.getHttpServletRequest();
            HttpServletResponse res = (HttpServletResponse) messageContext.getHttpServletResponse();

            //Need to add AtmoshereResource to attribute before calling Meteor
            AtmosphereRequest aReq = AtmosphereRequest.wrap(req);
            AtmosphereResponse aRes = AtmosphereResponse.wrap(res);

            try{
                //atmosphereBus.getFramework().doCometSupport(aReq, aRes);
                atmosphereBus.subscribe(req, res, topic);
            }catch (IOException ioe){
                  throw new RuntimeException(ioe);
            }
        }
        return Response.ok().build();

    }

    @POST
    public Response sub(String message) {
        if(atmosphereBus != null){
            try{
                atmosphereBus.publish(topic, (author == null ? "" : author), message);
            }catch (IOException ioe){
                throw new RuntimeException(ioe);
            }
        }
        return Response.ok().build();
    }
}
