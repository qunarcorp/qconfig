/*
 * Copyright (c) 2012 Qunar.com. All Rights Reserved.
 */
package qunar.tc.qconfig.admin.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.sql.Timestamp;

public class JsonDateSerializer extends JsonSerializer<Timestamp> {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(Timestamp value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        DateTime dateTime = new DateTime(value);
        jgen.writeString(dateTimeFormatter.print(dateTime));
    }
}
