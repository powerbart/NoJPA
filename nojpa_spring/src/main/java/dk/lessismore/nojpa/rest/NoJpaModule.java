package dk.lessismore.nojpa.rest;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import dk.lessismore.nojpa.db.methodquery.NList;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 * Date: 3/25/14
 * Time: 9:30 AM
 */
public class NoJpaModule extends SimpleModule {

    public NoJpaModule() {
        super("NoJpaModule");
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();
        SimpleDeserializers deserializers = new SimpleDeserializers();

        serializers.addSerializer(ModelObjectInterface.class, new NoJpaSerializer());
        serializers.addSerializer(Calendar.class, new CalendarSerializer());
        serializers.addSerializer(NList.class, new NListSerializer());
        context.addSerializers(serializers);
        context.addDeserializers(deserializers);
    }
}
