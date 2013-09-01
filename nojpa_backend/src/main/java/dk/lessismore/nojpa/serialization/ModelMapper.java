package dk.lessismore.nojpa.serialization;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.lang.reflect.Proxy;

public class ModelMapper extends MapperWrapper {

    public ModelMapper(Mapper wrapped) {
        super(wrapped);
    }

    public String serializedClass(Class type) {
        if(type == null) return null;
        if (Proxy.isProxyClass(type)) {
            Class<? extends ModelObjectInterface> modelInterface = findModelInterface(type);
            if (modelInterface != null) {
                return modelInterface.getName();
            }
        }
        return super.serializedClass(type);
    }

    public Class realClass(String elementName) {
        Class elementClass;
        try {
            elementClass = this.getClass().getClassLoader().loadClass(elementName);
        } catch (ClassNotFoundException e) {
            return super.realClass(elementName);
        }
        if (ModelObjectInterface.class.isAssignableFrom(elementClass)) {
            return elementClass;
        } else {
            return super.realClass(elementName);
        }
    }

    public static Class<? extends ModelObjectInterface> findModelInterface(Class proxyClass) {
        if (!Proxy.isProxyClass(proxyClass)) throw new ConversionException("Proxy class expected!! Got "+proxyClass.getName());
        for (Class interfaceClass: proxyClass.getInterfaces()) {
            if ( !interfaceClass.equals(ModelObjectInterface.class) && ModelObjectInterface.class.isAssignableFrom(interfaceClass))
                return interfaceClass;
        }
        return null;
    }
}