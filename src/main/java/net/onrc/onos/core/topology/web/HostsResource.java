package net.onrc.onos.core.topology.web;

import static net.onrc.onos.core.topology.web.TopologyResource.eval;
import net.onrc.onos.core.topology.ITopologyService;
import net.onrc.onos.core.topology.MutableTopology;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * A class to access hosts information from the network topology.
 */
public class HostsResource extends ServerResource {
    /**
     * Gets the hosts information from the network topology.
     *
     * @return a Representation of a Collection of hosts from the network
     * topology.
     */
    @Get("json")
    public Representation retrieve() {
        ITopologyService topologyService =
            (ITopologyService) getContext().getAttributes()
                .get(ITopologyService.class.getCanonicalName());

        MutableTopology mutableTopology = topologyService.getTopology();
        mutableTopology.acquireReadLock();
        try {
            return eval(toRepresentation(mutableTopology.getHosts(), null));
        } finally {
            mutableTopology.releaseReadLock();
        }
    }
}
