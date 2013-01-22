package org.ozoneplatform.kernel.bundles.server.atmosphere.rest

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
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;

@Path("/pubsub/{topic}")
@Produces("application/json")
public class PubSubController {
    
    private AtmosphereBus atmosphereBus = null;

    AtmosphereBus getAtmosphereBus() {
        return atmosphereBus
    }

    void setAtmosphereBus(AtmosphereBus atmosphereBus) {
        this.atmosphereBus = atmosphereBus
    }

    @GET
    public Response pub() {
        return Response.ok().build();
    }

    @POST
    public Response sub() {
        return Response.ok().build();
    }
}
