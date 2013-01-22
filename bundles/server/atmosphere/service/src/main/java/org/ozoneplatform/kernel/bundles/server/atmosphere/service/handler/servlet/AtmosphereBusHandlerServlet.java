package org.ozoneplatform.kernel.bundles.server.atmosphere.service.handler.servlet;

import org.atmosphere.config.service.MeteorService;
import org.ozoneplatform.kernel.bundles.server.atmosphere.api.AtmosphereBus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Sean T Booker
 * Date: 1/19/13
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
//@MeteorService(path=AtmosphereBus.METEOR_PUB_SUB_MAPPING)
public class AtmosphereBusHandlerServlet extends HttpServlet {


    protected AtmosphereBus atmosphereBus;

    public AtmosphereBusHandlerServlet(){

    }

    public AtmosphereBusHandlerServlet(AtmosphereBus atmosphereBus){
        this.atmosphereBus = atmosphereBus;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        atmosphereBus.subscribe(req, res, req.getPathInfo());
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        atmosphereBus.publish(req.getPathInfo(), "", req.getReader().readLine());
    }

    public AtmosphereBus getAtmosphereBus() {
        return atmosphereBus;
    }

    public void setAtmosphereBus(AtmosphereBus atmosphereBus) {
        this.atmosphereBus = atmosphereBus;
    }
}
