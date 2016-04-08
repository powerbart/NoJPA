package dk.lessismore.nojpa.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import dk.lessismore.nojpa.db.methodquery.NList;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by seb on 19/1/15.
 */
public class NListSerializer extends JsonSerializer<NList> {
    private PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy strategy = new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy();
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NoJpaSerializer.class);

    @Override
    public void serialize(NList nList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeObjectField("list", new ArrayList(nList));
        jgen.writeObjectField("numberFound", nList.getNumberFound());
        jgen.writeEndObject();
    }
}
