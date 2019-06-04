/*
 * Copyright (c) 2012 Qunar.com. All Rights Reserved.
 */
package qunar.tc.qconfig.admin.web.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;

public class JsonDateDeserializer extends JsonDeserializer<Timestamp> {
    private static final Logger logger = LoggerFactory.getLogger(JsonDateDeserializer.class);

    @Override
    public Timestamp deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        try {
            return Timestamp.valueOf(node.asText());
        } catch (RuntimeException e) {
            logger.error("json format error", e);
        }
        return null;
    }
}
