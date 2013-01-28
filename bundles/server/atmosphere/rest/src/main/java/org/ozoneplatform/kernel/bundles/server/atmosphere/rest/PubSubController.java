package org.ozoneplatform.kernel.bundles.server.atmosphere.rest;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;
/*import org.apache.cxf.jaxrs.ext.MessageContext;*/

@Path("/pubsub/{topic}")
@Produces("application/json")
public class PubSubController {

    /*@Context
    private MessageContext messageContext;*/

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
        /*if(atmosphereBus != null){
            HttpServletRequest req = (HttpServletRequest) messageContext.getHttpServletRequest();
            HttpServletResponse res = (HttpServletResponse) messageContext.getHttpServletResponse();

            //Need to add AtmoshereResource to attribute before calling Meteor
            AtmosphereRequest aReq = AtmosphereRequest.wrap(req);
            AtmosphereResponse aRes = AtmosphereResponse.wrap(res);

            try{
            	//TODO: Need to setup how to externally subscribe to a topic...
                //atmosphereBus.getFramework().doCometSupport(aReq, aRes);
                atmosphereBus.subscribe(req, res, topic);
            }catch (IOException ioe){
                  throw new RuntimeException(ioe);
            }
        }*/
        return Response.ok("NOT IMPLEMENTED YET").build();

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
