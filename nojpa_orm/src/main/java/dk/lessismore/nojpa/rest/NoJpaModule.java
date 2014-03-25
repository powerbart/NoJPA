package dk.lessismore.nojpa.rest;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 3/25/14
 * Time: 9:30 AM
 */
public class NoJpaModule extends SimpleModule {

    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);
        if (_serializers == null) {
            _serializers = new SimpleSerializers();
        }
        _serializers.addSerializer(ModelObjectInterface.class, new NoJpaSerializer());
        _serializers.addSerializer(Calendar.class, new CalendarSerializer());
    }
}
