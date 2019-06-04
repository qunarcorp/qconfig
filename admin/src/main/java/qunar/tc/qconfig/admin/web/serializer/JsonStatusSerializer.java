/*
 * Copyright (c) 2012 Qunar.com. All Rights Reserved.
 */
package qunar.tc.qconfig.admin.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import qunar.tc.qconfig.common.bean.StatusType;

import java.io.IOException;

public class JsonStatusSerializer extends JsonSerializer<StatusType> {

    @Override
    public void serialize(StatusType value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.text());
    }
}
