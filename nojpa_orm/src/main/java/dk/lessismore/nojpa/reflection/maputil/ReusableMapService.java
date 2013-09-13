package dk.lessismore.nojpa.reflection.maputil;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.attributes.AttributeContainer;
import dk.lessismore.nojpa.reflection.attributes.Attribute;
import dk.lessismore.nojpa.reflection.ClassReflector;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 19-08-2010
 * Time: 15:40:56
 * To change this template use File | Settings | File Templates.
 */
public class ReusableMapService {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReusableMapService.class);

    public static Map<String, String> getMap(ModelObjectInterface modelObject, String mapName, boolean includeObjectID){
        return getMap(modelObject, new String[]{mapName}, includeObjectID);
    }

    public static Map<String, String> getMap(ModelObjectInterface modelObject, String[] mapNames, boolean includeObjectID){
        if(modelObject == null || mapNames == null){
            log.error("Was called with null !!! getMap( modelObject="+ modelObject+", mapNames="+ mapNames +")");
            return null;
        }


        final Class<? extends ModelObjectInterface> myClass = ((ModelObject) modelObject).getInterface();
        System.out.println("Will lookup class : " + myClass.getSimpleName());
        AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(myClass);
        final List<Attribute> attList = attributeContainer.getAttributesWithAnnotation(ReusableMap.class);
        Map<String, String> toReturn = new HashMap<String, String>();
        for(final Iterator<Attribute> attributeIterator = attList.iterator(); attributeIterator.hasNext(); ){
            final Attribute attribute = attributeIterator.next();
            final ReusableMap annotation = attribute.getAnnotation(ReusableMap.class);
            boolean haveMapNameInAnnotation = false;
            for(int i = 0; !haveMapNameInAnnotation && annotation != null && annotation.mapNames() != null && i < annotation.mapNames().length; i++){
                for(int j = 0; !haveMapNameInAnnotation && j < mapNames.length; j++){
                    if(annotation.mapNames()[i].equals(mapNames[j])){
                        System.out.println("");
                        haveMapNameInAnnotation = true;
                    }
                }
            }
            if(haveMapNameInAnnotation){
                final String name = attribute.getAttributeName();
                final String value = "" + attribute.getAttributeValue(modelObject);
                System.out.println("toReturn.put("+ name +", "+ value +")");
                toReturn.put(name, ReusableMapValueConverter.convertToString(value));
            }
        }
        if(includeObjectID){
            toReturn.put("objectID", modelObject.getObjectID());
        }
        return toReturn;
    }
    
    public static <R extends ModelObjectInterface> R createOrUpdate(Class<R> sourceClass, Map<String, String> attNameAndAttValue){
        String objectID = attNameAndAttValue.get("objectID");
        R toReturn = null;
        if(objectID == null){
            toReturn = ModelObjectService.create(sourceClass);
        } else {
            toReturn = MQL.selectByID(sourceClass, objectID);
        }

        AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(sourceClass);
        for(final Iterator<Map.Entry<String, String>> entryIterator = attNameAndAttValue.entrySet().iterator(); entryIterator.hasNext(); ){
            final Map.Entry<String, String> stringStringEntry = entryIterator.next();
            final Attribute attribute = attributeContainer.getAttribute(stringStringEntry.getKey());
            if(attribute != null){
                log.debug("createOrUpdate:Setting attribute("+ stringStringEntry.getKey() + ") to value("+ stringStringEntry.getValue() +") on class " + sourceClass.getSimpleName());
                attribute.setAttributeValue(toReturn, ReusableMapValueConverter.convertFromString(attribute.getAttributeClass(), stringStringEntry.getValue()) );
            } else {
                log.error("createOrUpdate:Dont know "+ stringStringEntry.getKey() + " on class " + sourceClass.getSimpleName());
            }
        }
        return toReturn;
    }

    
}
