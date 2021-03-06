package net.onrc.onos.core.intent.runtime.web;

import net.onrc.onos.api.rest.RestError;
import net.onrc.onos.api.rest.RestErrorCode;
import net.onrc.onos.core.intent.Intent;
import net.onrc.onos.core.intent.IntentMap;
import net.onrc.onos.core.intent.runtime.IPathCalcRuntimeService;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * A class to access a single low-level intent.
 */
public class IntentLowObjectResource extends ServerResource {
    // TODO need to assign proper application id.
    private static final String APPLN_ID = "1";

    /**
     * Gets a single low-level intent.
     *
     * @return a Representation of the single low-level intent if found,
     * otherwise a Representation of a RestError object describing the problem.
     */
    @Get("json")
    public Representation retrieve() {
        IPathCalcRuntimeService pathRuntime =
            (IPathCalcRuntimeService) getContext().getAttributes()
                .get(IPathCalcRuntimeService.class.getCanonicalName());

        Representation result;

        String intentId = (String) getRequestAttributes().get("intent-id");

        //
        // Get a single low-level Intent: use the Intent ID to find it
        //
        IntentMap intentMap = pathRuntime.getPathIntents();
        String applnIntentId = APPLN_ID + ":" + intentId;
        Intent intent = intentMap.getIntent(applnIntentId);
        if (intent != null) {
            result = toRepresentation(intent, null);
        } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            final RestError notFound =
                    RestError.createRestError(RestErrorCode.INTENT_NOT_FOUND,
                                              applnIntentId);
            result = toRepresentation(notFound, null);
        }

        return result;
    }
}
