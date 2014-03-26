package dk.lessismore.nojpa.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 3/24/14
 * Time: 11:38 PM
 */
public class NoJpaSerializer extends JsonSerializer<ModelObjectInterface> {
    private PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy strategy = new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy();
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NoJpaSerializer.class);

    @Override
    public void serialize(ModelObjectInterface value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeObjectField("id", value.getObjectID());
        jgen.writeObjectField("creation_date", value.getCreationDate());

        Method[] methods = ((ModelObject) value).getInterface().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.getReturnType().equals(Void.TYPE) && (method.getParameterTypes().length == 0) && !method.isAnnotationPresent(JsonIgnore.class)) {
                try {
                    String fieldName = strategy.translate(method.getName().substring(3));
                    if (method.getReturnType().isArray()) {
                        jgen.writeArrayFieldStart(fieldName);
                        Object[] mois = (Object[]) method.invoke(value);
                        for (int j = 0; mois != null && j < mois.length; j++) {
                            jgen.writeObject(mois[j]);
                        }
                        jgen.writeEndArray();
                    } else {
                        // prevent huge recursions in tightly coupled models
                        if (ModelObjectInterface.class.isAssignableFrom(method.getReturnType())) {
                            Object moiValue = method.invoke(value);
                            if (moiValue != null) {
                                jgen.writeObjectField(fieldName, moiValue.toString());
                            } else {
                                jgen.writeObjectField(fieldName, null);
                            }
                        } else {
                            jgen.writeObjectField(fieldName, method.invoke(value));
                        }
                    }
                } catch (Exception e) {
                    log.error("can't serialize field: " + method.getName(), e);
                }
            }
        }
        jgen.writeEndObject();
    }
}
