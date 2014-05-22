package dk.lessismore.nojpa.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
            if (method.getReturnType().equals(Void.TYPE) || method.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }

            String fieldName = strategy.translate(method.getName().substring(3));
            jgen.writeFieldName(fieldName);
            try {
                if (method.getReturnType().isArray()) {
                    Object[] mois = (Object[]) method.invoke(value);
                    jgen.writeStartArray();
                    for (int j = 0; mois != null && j < mois.length; j++) {
                        jgen.writeObject(getObject(method.getReturnType().getComponentType(), mois[j]));
                    }
                    jgen.writeEndArray();
                } else {
                    jgen.writeObject(getObject(method.getReturnType(), method.invoke(value)));
                }
            } catch (Exception e) {
                log.error("can't serialize field: " + method.getName(), e);
            }
        }
        jgen.writeEndObject();
    }

    private static Object getObject(Class type, Object moi) {
        if (moi == null) {
            return null;
        }
        if (ModelObjectInterface.class.isAssignableFrom(type)) {
            if (type.isAnnotationPresent(JsonInclude.class)) {
                return moi;
            } else {
                return moi.toString();
            }
        } else {
            return moi;
        }
    }
}
