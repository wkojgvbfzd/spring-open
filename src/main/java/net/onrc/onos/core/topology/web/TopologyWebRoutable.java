package net.onrc.onos.core.topology.web;

import net.floodlightcontroller.restserver.RestletRoutable;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class TopologyWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        // debug API to dump datastore content
        router.attach("/ds/switches/json", DatastoreSwitchesResource.class);
        router.attach("/ds/links/json", DatastoreLinksResource.class);
        router.attach("/ds/ports/json", DatastorePortsResource.class);

        // Topology API
        router.attach("", TopologyResource.class);
        router.attach("/hosts", HostsResource.class);
        router.attach("/links", LinksResource.class);
        router.attach("/switches", SwitchesResource.class);

        return router;
    }

    @Override
    public String basePath() {
        return "/wm/onos/topology";
    }
}
