package net.onrc.onos.core.topology.web.serializers;

import java.io.IOException;
import java.util.Map.Entry;

import net.onrc.onos.core.topology.Port;
import net.onrc.onos.core.topology.Switch;
import net.onrc.onos.core.topology.TopologyElement;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

/**
 * JSON serializer for Switch objects.
 */
public class SwitchSerializer extends SerializerBase<Switch> {
    /**
     * Default constructor.
     */
    public SwitchSerializer() {
        super(Switch.class);
    }

    /**
     * Serializes a Switch object in JSON.
     *
     * @param sw the Switch that is being converted to JSON
     * @param jsonGenerator generator to place the serialized JSON into
     * @param serializerProvider unused but required for method override
     * @throws IOException if the JSON serialization process fails
     */
    @Override
    public void serialize(Switch sw, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
        throws IOException {

        //
        // TODO: For now, the JSON format of the serialized output should
        // be same as the JSON format of the corresponding class SwitchData.
        // In the future, we will use a single serializer.
        //

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(TopologyElement.TYPE, sw.getType());
        jsonGenerator.writeStringField(TopologyElement.ELEMENT_TYPE, sw.getSwitchType().toString());
        jsonGenerator.writeStringField("dpid", sw.getDpid().toString());
        jsonGenerator.writeStringField("state", "ACTIVE");
        jsonGenerator.writeArrayFieldStart("ports");
        for (Port port : sw.getPorts()) {
            jsonGenerator.writeObject(port);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeObjectFieldStart("stringAttributes");
        for (Entry<String, String> entry : sw.getAllStringAttributes().entrySet()) {
            jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
        }
        jsonGenerator.writeEndObject();         // stringAttributes
        jsonGenerator.writeEndObject();
    }
}
