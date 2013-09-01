package dk.lessismore.nojpa.serialization;

import com.thoughtworks.xstream.converters.*;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.util.ReflectionUtil;
import dk.lessismore.nojpa.reflection.attributes.Attribute;
import dk.lessismore.nojpa.reflection.attributes.AttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;

import java.lang.reflect.Proxy;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ModelConverter implements Converter {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ModelConverter.class);

    private final static String OBJECT_ID_FILED_NAME = "objectID";
    private ClassLoader classLoader;
    private Mapper mapper;

    public ModelConverter(Mapper mapper) {
        this(mapper, ModelConverter.class.getClassLoader());
    }

    public ModelConverter(Mapper mapper, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.mapper = mapper;
    }

    public boolean canConvert(Class type) {
        //log.debug("canConvert: type="+ type.getSimpleName());
        boolean isModelProxyClass = Proxy.isProxyClass(type) && ModelMapper.findModelInterface(type) != null;
        boolean isModelInterface = ModelObjectInterface.class.isAssignableFrom(type);
        return  isModelInterface || isModelProxyClass;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
//        log.debug("marshal: source=" + source);
        Class<? extends ModelObjectInterface> modelInterface = ModelMapper.findModelInterface(source.getClass());
        if (modelInterface == null) throw new ConversionException("Unable to find model interface for proxy object.");
        AttributeContainer container = new AttributeContainer();
        container.findAttributes(modelInterface);

        // Write 'objectID' as the first element
        Attribute idAttribute = container.getAttribute(OBJECT_ID_FILED_NAME);
        Object objectId = idAttribute.getAttributeValue(source);
        writer.startNode(idAttribute.getAttributeName());
        context.convertAnother(objectId);
        writer.endNode();

        // Write the other elements
        for(Attribute attribute: container.getAttributes().values()) {
            if (!attribute.getAttributeClassName().equals(OBJECT_ID_FILED_NAME)) {
                Object value = attribute.getAttributeValue(source);
                if (value != null) {
                    writer.startNode(attribute.getAttributeName());
                    context.convertAnother(value);
                    writer.endNode();
                }
            }
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Class clazz = context.getRequiredType();
        log.debug("unmarshal: interfaceClassName=" + clazz.getCanonicalName());

        if (! ModelObjectInterface.class.isAssignableFrom(clazz)) {
            throw new ConversionException("The class "+ clazz.getCanonicalName() + " is not implementing "+ModelObjectInterface.class.getName());
        }

        Class<? extends ModelObjectInterface> interfaceClass = (Class<? extends ModelObjectInterface>) clazz;

        ModelObjectInterface instance = null;
        if (instance == null) {
            instance = ModelObjectService.create(interfaceClass);
        }

        AttributeContainer container = new AttributeContainer();
        container.findAttributes(interfaceClass);
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String elementName = reader.getNodeName();

            //log.debug("unmarshal:elementName = " + elementName);

            try {
                Attribute attribute = container.getAttribute(elementName);
                if(attribute.isArray()) {
                    ArrayList arrayList = new ArrayList();
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        String classNameForArray = reader.getNodeName();
                        Class arrayClass = Class.forName(classNameForArray);
                        Object value = context.convertAnother(null, arrayClass);
                        arrayList.add(value);
                        //log.debug("classNameForArray("+ classNameForArray +")");
                        reader.moveUp();
                    }
                    if(!arrayList.isEmpty()) {
                        Object innerArray = Array.newInstance(attribute.getAttributeClass(), arrayList.size());
                        for(int i = 0; i < arrayList.size(); i++){
                            Array.set(innerArray, i, arrayList.get(i));
                        }
                        attribute.setAttributeValue(instance,  innerArray);
                    }
                    //log.debug("arrayList.size("+ arrayList.size() +")");
                } else if (attribute != null && !attribute.isArray()) {
                    Class attributeClass = attribute.getAttributeClass();
                    Object value = context.convertAnother(null, attributeClass); //context.convertAnother(reader.getValue(), attributeClass); //XStreamUtility.getInstance().getStream().unmarshal(reader); //
                    attribute.setAttributeValue(instance, value);
                }
            } catch (Exception e) {
                log.error("just logger ;) " + e.getMessage() + " on " + reader.getNodeName(), e);
            }

            reader.moveUp();

        }
        if (instance != null && instance.getObjectID() != null) {
            ModelObjectInterface instMoi = MQL.selectByID(interfaceClass, instance.getObjectID());
            if (instMoi == null) {
                return instance;
            } else  {
                ReflectionUtil.copyNotNulls(instance, instMoi);
                return instMoi;
            }
        }
        return instance;
    }
}