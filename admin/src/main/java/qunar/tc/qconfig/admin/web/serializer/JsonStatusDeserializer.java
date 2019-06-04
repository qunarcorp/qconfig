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
import qunar.tc.qconfig.common.bean.StatusType;

import java.io.IOException;

public class JsonStatusDeserializer extends JsonDeserializer<StatusType> {
    private static final Logger logger = LoggerFactory.getLogger(JsonStatusDeserializer.class);

    @Override
    public StatusType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        try {
            return StatusType.fromText(node.asText());
        } catch (RuntimeException e) {
            logger.error("json format error", e);
        }
        return null;
    }
}
