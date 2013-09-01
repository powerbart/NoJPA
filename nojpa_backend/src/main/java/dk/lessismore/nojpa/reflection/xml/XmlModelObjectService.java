package dk.lessismore.nojpa.reflection.xml;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.attributes.AttributeContainer;
import dk.lessismore.nojpa.reflection.attributes.Attribute;
import dk.lessismore.nojpa.reflection.ClassReflector;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 12-05-2010
 * Time: 10:38:27
 * To change this template use File | Settings | File Templates.
 */
public class XmlModelObjectService {


    private static final HashMap<String, Field[]> fs = new HashMap<String, Field[]>(40);

    private static Class[] JAVA_TYPES = new Class[]{
            int.class, float.class, double.class, boolean.class, long.class,
            Integer.class, Float.class, Double.class, Boolean.class, Long.class,
            String.class, Calendar.class, Class.class
    };


    public static String[] packagePrefixs = new String[]{"com.bbb.protocol.request", "com.bbb.protocol.response", "com.bbb.protocol.response.responseludo", "com.bbb.protocol.request.requestludo", "com.bbb.protocol.response.responsewebserver", "com.bbb.protocol.request.requestwebserver", "com.bbb.gameutil.communication", "com.bbb.protocol.request.requestrussianroulette"};

    public static Object fromXml(String xmlTop) throws Exception {
        xmlTop = xmlTop.replaceAll("\n|\t| |\r", "");
        //System.out.println("fromXml reads: " + xmlTop);
        int endTag = xmlTop.indexOf(">");
        int posEndTag2 = xmlTop.indexOf("/");
        if(posEndTag2 != -1 && posEndTag2 < endTag){
            endTag = posEndTag2;
        }

        System.out.println("fromXml(): xmlTop = " + xmlTop);
        System.out.println("fromXml(): endTag = " + endTag);

        String clazzName = xmlTop.substring(1, endTag);
        System.out.println("fromXml:parsing "+ clazzName);
        Object o = null;
        if(clazzName.indexOf(".") != -1){
            Class<?> aClass = Class.forName(clazzName);
            if(ModelObjectInterface.class.isAssignableFrom(aClass)){
                //TODO: We have to use ReflectionUtil.copyNotNulls() somewhere !!!
                o = ModelObjectService.create((Class<? extends ModelObjectInterface>) aClass);
            } else {
                try{ o = Class.forName(clazzName).newInstance(); } catch(Exception e){
                    throw new RuntimeException("Cant find class: " + clazzName);
                }
            }
        } else {
            for(int i = 0; packagePrefixs != null && o == null && i < packagePrefixs.length; i++){
                Class<?> aClass = null;
                try{
                    aClass = Class.forName(packagePrefixs[i] + "." + clazzName);
                } catch(Exception e){
                }
                if(aClass != null){
                    if(ModelObjectInterface.class.isAssignableFrom(aClass)){
                        //TODO: We have to use ReflectionUtil.copyNotNulls() somewhere !!!
                        o = ModelObjectService.create((Class<? extends ModelObjectInterface>) aClass);
                    } else {
                        try{ o = Class.forName(clazzName).newInstance(); } catch(Exception e){
                            throw new RuntimeException("(2) Cant find class: " + clazzName);
                        }
                    }
                }
            }
        }
        if(o == null){
            throw new RuntimeException("Cant find class " + clazzName + " ... Add package name or give full package and class name ");
        }
        if(endTag != posEndTag2){
            String s = xmlTop.substring(xmlTop.indexOf(">") + 1, xmlTop.indexOf("</" + clazzName + ">"));
            if(!s.trim().equals("")){
                fromXml(o, s);
            }
        }
        return o;
    }


    public static void fromXml(Object objToSetOn, String xmlTop) throws Exception {
        String fieldName = xmlTop.trim().substring(1, xmlTop.indexOf(">"));
        final String endTag = "</" + fieldName + ">";
        final String startTag = "<" + fieldName + ">";
        System.out.println("fromXml()----------------------- GOT ------------------");
        System.out.println("fromXml()" + xmlTop);
        System.out.println("fromXml()----------------------------------------------");
        System.out.println("fromXml() fieldName = " + fieldName);

        if(objToSetOn instanceof ModelObject){

            AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(objToSetOn);
            Attribute attribute = attributeContainer.getAttribute(fieldName);
            Class attributeClass = attribute.getAttributeClass();

            boolean isList = attribute.isArray();
            if(!attributeClass.isArray() && !isList){
                ////System.out.println("fromXml::field.getType() = " + field.getType());
                if(isJavaType(attributeClass)){
                    attribute.setAttributeValue(objToSetOn, readValue(xmlTop.substring(startTag.length(), xmlTop.indexOf(endTag)), attributeClass));
                }  else {
                    attribute.setAttributeValue(objToSetOn, fromXml(xmlTop.substring(startTag.length(), xmlTop.indexOf(endTag))));
                }
            } else {
                String valueOfWholeXmlList = (xmlTop.substring(startTag.length(), xmlTop.indexOf(endTag)));
                System.out.println("valueOfWholeXmlList = " + valueOfWholeXmlList);
                boolean endsWithS = fieldName.endsWith("s");
    //            String startArrayElementStr = "<" + (endsWithS ? fieldName.substring(0, fieldName.length() - 1) : "arrayelement:" + fieldName) + ">";
    //            String endArrayElementStr = "</" + (endsWithS ? fieldName.substring(0, fieldName.length() - 1) : "arrayelement:" + fieldName) + ">";
                int lastStart = -1;
                int lastEnd = 0;
                //System.out.println("field.getGenericType() = " + field.getGenericType());
//                Class<?> componetType = (isList ? getInnerGenericType(field.getGenericType()) : field.getType().getComponentType());
                Class<?> componetType = attribute.getAttributeClass();
                ////System.out.println("componetType = " + componetType);
                List<Object> lst = new ArrayList<Object>();
                boolean moreData = true;
                while( (lastStart = valueOfWholeXmlList.indexOf('<', lastEnd)) != -1 ){
                    //System.out.println("valueOfWholeXmlList = " + valueOfWholeXmlList);
                    lastEnd = valueOfWholeXmlList.indexOf('>', lastStart);
                    String tagName = valueOfWholeXmlList.substring(lastStart + 1, lastEnd);
                    //System.out.println("tagName = " + tagName);
                    if(tagName.endsWith("/")){
                        //System.out.println("---------- SINGLE : " + tagName);

                        String xmlElementValue = valueOfWholeXmlList.substring(lastStart, lastEnd + 1);
                        //System.out.println("xmlElementValue = " + xmlElementValue);
                        lst.add(fromXml(xmlElementValue));
                        lastEnd = lastEnd + 1;

                    } else {
                        String startArrayElementStr = "<" + tagName + ">";
                        String endArrayElementStr = "</" + tagName + ">";

                        lastStart = valueOfWholeXmlList.indexOf(startArrayElementStr, lastStart);
                        lastEnd = valueOfWholeXmlList.indexOf(endArrayElementStr, lastStart) + endArrayElementStr.length();
                        if(attribute.isArray()){
                            String xmlElementValue = valueOfWholeXmlList.substring(lastStart, lastEnd);
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            Class<?> innerType = attribute.getAttributeClass();
                            //System.out.println("---------------- ADDING INNER ARRAY ("+ innerType +")");
                            List<Object> innerList = new ArrayList<Object>();
                            int innerLastStart = -1;
                            int innerLastEnd = 0;
                            boolean isAnJavaType = isJavaType(innerType);
                            String innerStartArrayElementStr = isAnJavaType ? "<|InnerArray|>" : "<" + innerType.getSimpleName() + ">";
                            String innerEndElementStr = isAnJavaType ? "</|InnerArray|>" : "</" + innerType.getSimpleName() + ">";
                            while((innerLastStart = xmlElementValue.indexOf(innerStartArrayElementStr, innerLastStart + 1)) != -1){
                                innerLastEnd = xmlElementValue.indexOf(innerEndElementStr, innerLastStart);
                                if(isAnJavaType){
                                    String innerXmlValue = xmlElementValue.substring(innerLastStart + innerStartArrayElementStr.length(), innerLastEnd);
                                    //System.out.println("innerXmlValue-1 = " + innerXmlValue);
                                    innerList.add(readValue(innerXmlValue, innerType));
                                } else {
                                    String innerXmlValue = xmlElementValue.substring(innerLastStart , innerLastEnd + innerStartArrayElementStr.length() + 1);
                                    //System.out.println("innerXmlValue-2 = " + innerXmlValue);
                                    innerList.add(fromXml(innerXmlValue));
                                }
                            }
                            Object innerArray = Array.newInstance(innerType, innerList.size());
                            for(int i = 0, lstSize = innerList.size(); i < lstSize; i++){
                                Array.set(innerArray, i, innerList.get(i));
                                //field.set(fieldName, array);
                            }
                            attribute.setAttributeValue(objToSetOn,  innerArray);
//                            lst.add(innerArray);

                        } else if (isJavaType(componetType)){
                            String xmlElementValue = valueOfWholeXmlList.substring(lastStart + startArrayElementStr.length(), lastEnd - endArrayElementStr.length());
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            lst.add(readValue(xmlElementValue, componetType));
                            if(isList && !attribute.isArray()){
                                attribute.setAttributeValue(objToSetOn,  lst);
                            } else {
                                Object array = Array.newInstance(componetType, lst.size());
                                for(int i = 0, lstSize = lst.size(); i < lstSize; i++){
                                    Array.set(array, i, lst.get(i));
                                }
                                attribute.setAttributeValue(objToSetOn,  array);
          //                    field.set(objToSetOn,  array);
                            }
                        } else {
                            String xmlElementValue = valueOfWholeXmlList.substring(lastStart, lastEnd);
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            lst.add(fromXml(xmlElementValue));
                            if(isList && !attribute.isArray()){
                                attribute.setAttributeValue(objToSetOn,  lst);
                            } else {
                                Object array = Array.newInstance(componetType, lst.size());
                                for(int i = 0, lstSize = lst.size(); i < lstSize; i++){
                                    Array.set(array, i, lst.get(i));
                                }
                                attribute.setAttributeValue(objToSetOn,  array);
//                    field.set(objToSetOn,  array);
                            }
                        }
                    }
                }
            }
            String newXml = xmlTop.substring(xmlTop.indexOf(endTag) + endTag.length());
            if(newXml.indexOf("<") != -1){
                fromXml(objToSetOn, newXml);
            }




        } else {
            AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(objToSetOn.getClass());
            Attribute attribute = attributeContainer.getAttribute(fieldName);
//            Field field = objToSetOn.getClass().getField(fieldName);
//            boolean isList = attribute.isArray();
            if(!attribute.isArray()){
                ////System.out.println("fromXml::field.getType() = " + field.getType());
                if(isJavaType(attribute.getAttributeClass())){
                    attribute.setAttributeValue(objToSetOn, readValue(xmlTop.substring(startTag.length(), xmlTop.indexOf(endTag)), attribute.getAttributeClass()));
                }  else {
                    attribute.setAttributeValue(objToSetOn, fromXml(xmlTop.substring(startTag.length(), xmlTop.indexOf(endTag))));
                }
            } else {
                String valueOfWholeXmlList = (xmlTop.substring(startTag.length(), xmlTop.indexOf(endTag)));
                boolean endsWithS = fieldName.endsWith("s");
    //            String startArrayElementStr = "<" + (endsWithS ? fieldName.substring(0, fieldName.length() - 1) : "arrayelement:" + fieldName) + ">";
    //            String endArrayElementStr = "</" + (endsWithS ? fieldName.substring(0, fieldName.length() - 1) : "arrayelement:" + fieldName) + ">";
                int lastStart = -1;
                int lastEnd = 0;
                //System.out.println("field.getGenericType() = " + field.getGenericType());
                Class<?> componetType = attribute.getAttributeClass();
                ////System.out.println("componetType = " + componetType);
                List<Object> lst = new ArrayList<Object>();
                boolean moreData = true;
                while( (lastStart = valueOfWholeXmlList.indexOf('<', lastEnd)) != -1 ){
                    //System.out.println("valueOfWholeXmlList = " + valueOfWholeXmlList);
                    lastEnd = valueOfWholeXmlList.indexOf('>', lastStart);
                    String tagName = valueOfWholeXmlList.substring(lastStart + 1, lastEnd);
                    //System.out.println("tagName = " + tagName);
                    if(tagName.endsWith("/")){
                        //System.out.println("---------- SINGLE : " + tagName);

                        String xmlElementValue = valueOfWholeXmlList.substring(lastStart, lastEnd + 1);
                        //System.out.println("xmlElementValue = " + xmlElementValue);
                        lst.add(fromXml(xmlElementValue));
                        lastEnd = lastEnd + 1;

                    } else {
                        String startArrayElementStr = "<" + tagName + ">";
                        String endArrayElementStr = "</" + tagName + ">";

                        lastStart = valueOfWholeXmlList.indexOf(startArrayElementStr, lastStart);
                        lastEnd = valueOfWholeXmlList.indexOf(endArrayElementStr, lastStart) + endArrayElementStr.length();
                        if(componetType.isArray()){
                            String xmlElementValue = valueOfWholeXmlList.substring(lastStart, lastEnd);
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            Class<?> innerType = componetType.getComponentType();
                            //System.out.println("---------------- ADDING INNER ARRAY ("+ innerType +")");
                            List<Object> innerList = new ArrayList<Object>();
                            int innerLastStart = -1;
                            int innerLastEnd = 0;
                            boolean isAnJavaType = isJavaType(innerType);
                            String innerStartArrayElementStr = isAnJavaType ? "<arrayelement:|InnerArray|>" : "<" + innerType.getSimpleName() + ">";
                            String innerEndElementStr = isAnJavaType ? "</arrayelement:|InnerArray|>" : "</" + innerType.getSimpleName() + ">";
                            while((innerLastStart = xmlElementValue.indexOf(innerStartArrayElementStr, innerLastStart + 1)) != -1){
                                innerLastEnd = xmlElementValue.indexOf(innerEndElementStr, innerLastStart);
                                if(isAnJavaType){
                                    String innerXmlValue = xmlElementValue.substring(innerLastStart + innerStartArrayElementStr.length(), innerLastEnd);
                                    //System.out.println("innerXmlValue-1 = " + innerXmlValue);
                                    innerList.add(readValue(innerXmlValue, innerType));
                                } else {
                                    String innerXmlValue = xmlElementValue.substring(innerLastStart , innerLastEnd + innerStartArrayElementStr.length() + 1);
                                    //System.out.println("innerXmlValue-2 = " + innerXmlValue);
                                    innerList.add(fromXml(innerXmlValue));
                                }
                            }
                            Object innerArray = Array.newInstance(innerType, innerList.size());
                            for(int i = 0, lstSize = innerList.size(); i < lstSize; i++){
                                Array.set(innerArray, i, innerList.get(i));
                                //field.set(fieldName, array);
                            }
                            lst.add(innerArray);

                        } else if (isJavaType(componetType)){
                            String xmlElementValue = valueOfWholeXmlList.substring(lastStart + startArrayElementStr.length(), lastEnd - endArrayElementStr.length());
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            lst.add(readValue(xmlElementValue, componetType));
                        } else {
                            String xmlElementValue = valueOfWholeXmlList.substring(lastStart, lastEnd);
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            //System.out.println("xmlElementValue = " + xmlElementValue);
                            lst.add(fromXml(xmlElementValue));
                        }
                    }
                }
                if(false){
//                    field.set(objToSetOn,  lst);
                } else {
                    Object array = Array.newInstance(componetType, lst.size());
                    for(int i = 0, lstSize = lst.size(); i < lstSize; i++){
                        Array.set(array, i, lst.get(i));
                    }
                    attribute.setAttributeValue(objToSetOn,  array);
                }
            }
            String newXml = xmlTop.substring(xmlTop.indexOf(endTag) + endTag.length());
            if(newXml.indexOf("<") != -1){
                fromXml(objToSetOn, newXml);
            }
        }
    }


    public static String toXml(Object headObject) throws Exception {
        if(headObject == null) return "";
        else {
            if(headObject.getClass().isArray() || Collection.class.isAssignableFrom(headObject.getClass())){
                return arrayToXml(headObject, "|InnerArray|").toString();
            } else {
                if((headObject instanceof ModelObject)){
                    Class headClass = ((ModelObject) headObject).getInterface();
                    AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(headObject);
                    Map<String, Attribute> mapOfAtts = attributeContainer.getAttributes();
                    StringBuilder builder = new StringBuilder();
                    builder.append("<");
                    boolean containPrefix = isKnowedPackage(headClass);
                    builder.append(containPrefix ? headClass.getSimpleName() :  headObject.getClass().getName());

                    if(mapOfAtts != null && mapOfAtts.size() != 0){
                        builder.append(">");

                        for(Iterator<Map.Entry<String, Attribute>> entryIterator = mapOfAtts.entrySet().iterator(); entryIterator.hasNext(); ){
                            Map.Entry<String, Attribute> attributeEntry = entryIterator.next();
                            builder.append(toXml(attributeEntry.getValue(), headObject));
                        }
                        builder.append("\n</");
                        builder.append(containPrefix ? headClass.getSimpleName() :  headObject.getClass().getName());
                        builder.append(">");
                    } else {
                        builder.append(" />");
                    }
                    return builder.toString();
                } else if(!isJavaType(headObject.getClass())){
                    AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(headObject.getClass());
                    Map<String, Attribute> mapOfAtts = attributeContainer.getAttributes();
                    StringBuilder builder = new StringBuilder();
                    builder.append("<");
                    boolean containPrefix = isKnowedPackage(headObject.getClass());
                    builder.append(containPrefix ? headObject.getClass().getSimpleName() :  headObject.getClass().getName());
                    if(mapOfAtts != null && mapOfAtts.size() != 0){
                        builder.append(">");
                        for(Iterator<Map.Entry<String, Attribute>> entryIterator = mapOfAtts.entrySet().iterator(); entryIterator.hasNext(); ){
                            Map.Entry<String, Attribute> attributeEntry = entryIterator.next();
                            builder.append(toXml(attributeEntry.getValue(), headObject));
                        }
                        builder.append("\n</");
                        builder.append(containPrefix ? headObject.getClass().getSimpleName() :  headObject.getClass().getName());
                        builder.append(">");
                    } else {
                        builder.append(" />");
                    }
                    return builder.toString();
                } else if(Calendar.class.isAssignableFrom(headObject.getClass())){
                    return "" + ((Calendar) headObject).getTimeInMillis();
                } else if(headObject instanceof Class){
                    return "a-" + ((Class) headObject).getPackage() + "." + ((Class) headObject).getSimpleName();
                } else {
                    return "" + headObject;
                }
            }
        }
    }


    public static String toXml(Field field, Object headObject) throws Exception {
        StringBuilder builder = new StringBuilder();
        Class<?> type = field.getType();
        ////System.out.println("type = " + type);
        if(!(type.isArray() || Collection.class.isAssignableFrom(type))){
            final String startTag = "\n\t<" + field.getName() + ">";
            final String endTag = "</" + field.getName() + ">";
            if(field.getType().equals(Calendar.class)){
                builder.append(startTag);
                builder.append(((Calendar)field.get(headObject)).getTimeInMillis());
                builder.append(endTag);
            } else if(field.getType().equals(Class.class)){
                builder.append(startTag);
                builder.append(((Class)field.get(headObject)).getName());
                builder.append(endTag);
            } else if (isJavaType(field.getType())){
                builder.append(startTag);
                builder.append(field.get(headObject));
                builder.append(endTag);
            } else if(field.get(headObject) != null){
                builder.append(startTag);
                builder.append((toXml(field.get(headObject))).replaceAll("\t", "\t\t"));
                builder.append("\n\t");
                builder.append(endTag);
            }
        } else {
            Object o = field.get(headObject);
            String fieldName = field.getName();
            StringBuilder b = arrayToXml(o, fieldName);
            builder.append(b);
        }
        return builder.toString();
    }

    public static String toXml(Attribute attribute, Object headObject) throws Exception {
        StringBuilder builder = new StringBuilder();
        Class<?> type = attribute.getAttributeClass();
        ////System.out.println("type = " + type);
        if(!attribute.isArray()){
            final String startTag = "\n\t<" + attribute.getAttributeName() + ">";
            final String endTag = "</" + attribute.getAttributeName() + ">";
            if(type.equals(Calendar.class)){
                if(((Calendar)attribute.getAttributeValue(headObject)) != null){
                    builder.append(startTag);
                    builder.append(((Calendar)attribute.getAttributeValue(headObject)).getTimeInMillis());
                    builder.append(endTag);
                }
            } else if(type.equals(Class.class)){
                builder.append(startTag);
                builder.append(((Class)attribute.getAttributeValue(headObject)).getName());
                builder.append(endTag);
            } else if (isJavaType(type)){
                builder.append(startTag);
                builder.append(attribute.getAttributeValue(headObject));
                builder.append(endTag);
            } else if(attribute.getAttributeValue(headObject) != null){
                builder.append(startTag);
                builder.append((toXml(attribute.getAttributeValue(headObject))).replaceAll("\t", "\t\t"));
                builder.append("\n\t");
                builder.append(endTag);
            }
        } else {
            Object o = attribute.getAttributeValue(headObject);
            String fieldName = attribute.getAttributeName();
            StringBuilder b = arrayToXml(o, fieldName);
            builder.append(b);
        }
        return builder.toString();
    }

    private static StringBuilder arrayToXml(Object headObject, String fieldName) throws Exception {
        StringBuilder builder = new StringBuilder();
        if(headObject != null){
          if(headObject.getClass().isArray()){
            boolean endsWithS = fieldName.endsWith("s");
            boolean prettyPrint = !isJavaType(headObject.getClass().getComponentType());
            final String startTag = "\n\t<" + fieldName + ">\n\t\t";
            final String endTag = "\n\t</" + fieldName + ">";
            final String elementStartTag = "<" + (endsWithS ? fieldName.substring(0, fieldName.length() - 1) : "arrayelement:" + fieldName) + ">";
            final String elementEndTag = "</" + (endsWithS ? fieldName.substring(0, fieldName.length() - 1) : "arrayelement:" + fieldName) + ">";
            builder.append(startTag);
            for(int i = 0, length = Array.getLength(headObject); i < length; i++){
                //if(prettyPrint) builder.append('\n');
                boolean isJava = isJavaType(Array.get(headObject, i).getClass());
                if(isJava){
                    builder.append(elementStartTag);
                }
                //if(prettyPrint) builder.append("\n\t");
                builder.append(toXml(Array.get(headObject, i)).replaceAll("\n", "\n\t").replaceAll("\t", "\t\t"));
                //if(prettyPrint) builder.append('\n');
                if(isJava){
                    builder.append(elementEndTag);
                } else {
                    builder.append("\n\t\t");
                }
            }
            builder.append(endTag);
          } else if(Collection.class.isAssignableFrom(headObject.getClass())) {
            //System.out.println(" ----------- ArrayList : " + fieldName + " / " + headObject);
            final String startTag = "\n\t<" + fieldName + ">\n\t\t";
            final String endTag = "\n\t</" + fieldName + ">";
            final String elementStartTag = "<" + "arrayelement:" + fieldName + ">";
            final String elementEndTag = "</" + "arrayelement:" + fieldName + ">";
            builder.append(startTag);
            Iterator iterator = ((Collection) headObject).iterator();
            while(iterator.hasNext()){
              Object o = iterator.next();
              //if(prettyPrint) builder.append('\n');
                boolean isJava = isJavaType(o.getClass());
                if(isJava){
                    builder.append(elementStartTag);
                }
                builder.append(toXml(o).replaceAll("\n", "\n\t").replaceAll("\t", "\t\t"));
                if(isJava){
                    builder.append(elementEndTag);
                } else {
                    builder.append("\n\t\t");
                }
            }
            builder.append(endTag);
          }
        }
        return builder;
    }


    private static Object readValue(String someXml, Class componentType) throws Exception {
        if(componentType.isPrimitive()){
            if(componentType.equals(int.class)){
                return Integer.parseInt(someXml);
            } else if(componentType.equals(float.class)){
                return Float.parseFloat(someXml);
            } else if(componentType.equals(double.class)){
                return Double.parseDouble(someXml);
            } else if(componentType.equals(long.class)){
                return Long.parseLong(someXml);
            } else if(componentType.equals(boolean.class)){
                return Boolean.parseBoolean(someXml);
            } else {
                throw new Exception("Not implmenteted ... yet ...! ");
            }
        } else if(componentType.equals(Calendar.class)){
            Calendar c = Calendar.getInstance();
            long l = Long.parseLong(someXml);
            c.setTimeInMillis(l);
            return c;
        } else if(componentType.equals(Class.class)){
            Class c = Class.forName(someXml);
            return c;
        } else {
            Constructor constructor = componentType.getConstructor(String.class);
            Object o = constructor.newInstance(someXml);
            return o;
        }
    }


    public static boolean isJavaType(Class clazz){
        for(int i = 0; i < JAVA_TYPES.length; i++){
            if(JAVA_TYPES[i] == clazz){
                return true;
            } else {
                ////System.out.println("isJavaType:: No match on ("+ JAVA_TYPES[i] +" ~ "+ clazz +")");
            }
        }
        return false;
    }

  public static boolean isKnowedPackage(Class clazz){
      for(int i = 0; i < packagePrefixs.length; i++){
//          System.out.println("clazz = " + clazz);
//          System.out.println("clazz.getPackage(). = " + clazz.getPackage());
//          System.out.println("clazz.getPackage().getName() = " + clazz.getPackage().getName());
          if(packagePrefixs[i].equals(clazz.getPackage().getName())){
              return true;
          } else {
              ////System.out.println("isJavaType:: No match on ("+ JAVA_TYPES[i] +" ~ "+ clazz +")");
          }
      }
      return false;
  }
    private static Field[] getFields(Object headObject)  {
        synchronized(fs){
            Field[] arrayToReturn = fs.get(headObject.getClass().getName());
            if(arrayToReturn == null){
                final Field[] fields = headObject.getClass().getFields();
                ArrayList<Field> toReturn = new ArrayList<Field>();
                for(int i = 0; i < fields.length; i++){
                    if(fields[i].getModifiers() != Modifier.FINAL && fields[i].getModifiers() != Modifier.STATIC && fields[i].getModifiers() == Modifier.PUBLIC){
                        toReturn.add(fields[i]);
                    }
                }
                arrayToReturn = toReturn.toArray(new Field[toReturn.size()]);
                fs.put(headObject.getClass().getName(), arrayToReturn);
            }
            return arrayToReturn;
        }
    }

    public static Class getInnerGenericType(Type type) throws ClassNotFoundException {
        String s = "" + type;
        //System.out.println("s = " + s);
        String toReturn = s.substring(s.indexOf('<') + 1, s.indexOf('>'));
        return Class.forName(toReturn);
    }

//    public static void main(String[] args) throws Exception {
//
//
//
//
//        SebsXml.packagePrefixs = new String[]{"net.infopaq.keyhole.keyworker", "net.infopaq.keyhole.model"};
////        KeyholeScanRequest keyholeScanRequest = ModelObjectService.create(KeyholeScanRequest.class);
////
////        keyholeScanRequest.setDateOfProduction(Calendar.getInstance());
////        keyholeScanRequest.setFilename("/asdasd");
////        keyholeScanRequest.setMediaID("1234");
////        keyholeScanRequest.setMediaName("Die Welt");
////        keyholeScanRequest.setMissingPagesWithComma("1,2,3");
////        keyholeScanRequest.setPageFormat("A4");
////        keyholeScanRequest.setScanMethod("FULL");
////        keyholeScanRequest.setScanNumber(1);
////        keyholeScanRequest.setUltimaRequestID("6666");
////
////
////
////        System.out.println("((ModelObject) keyholeScanRequest).getInterface() = " + ((ModelObject) keyholeScanRequest).getInterface());
////        System.out.println("(keyholeScanRequest instanseof ModelObject) = " + (keyholeScanRequest instanceof ModelObject));
//
//        System.out.println("ModelObject.class.isAssignableFrom(KeyholeArticle.class) = " + ModelObjectInterface.class.isAssignableFrom(KeyholeArticle.class));
//        System.out.println("ModelObject.class.isAssignableFrom(KeyholeArticle.class) = " + KeyholeArticle.class.isAssignableFrom(ModelObjectInterface.class));
//
//        KeyholeArticle article = ModelObjectService.create(KeyholeArticle.class);
//        article.setCreationDate(Calendar.getInstance());
//        article.setHeader("MyHeader");
//        article.setWordCount(666);
//        Position[] cp = new Position[2];
//        cp[0] = ModelObjectService.create(Position.class);
//        cp[0].setX(1);
//        cp[0].setY(1);
//        cp[1] = ModelObjectService.create(Position.class);
//        cp[1].setX(2);
//        cp[1].setY(2);
//        article.setCutPositions(cp);
//
//
//
//        String xml1 = SebsXml.toXml(article);
//        System.out.println("-------------------------------------------");
//        System.out.println("xml1 = " + xml1);
//        System.out.println("-------------------------------------------");
//        Object o = SebsXml.fromXml( xml1 );
//        String xml2 = SebsXml.toXml( o );
//
//
//        System.out.println("-------------------------------------------");
//        System.out.println("xml1 = " + xml1);
//        System.out.println("-------------------------------------------");
//        System.out.println("xml2 = " + xml2);
//        System.out.println("-------------------------------------------");
//
//
//
////        Xmls.packagePrefixs = new String[]{"dk.letpension.raadgivning.mapping", "dk.letpension.raadgivning.mapping.validation"};
////        String xml = Xmls.toXml(new Zoo());
////        //System.out.println("xml = \n" + xml);
////
////        Object o = Xmls.fromXml(xml);
////        String xml2 = Xmls.toXml(o);
////        //System.out.println("----------------------------------------------------------------------------------");
////        //System.out.println("" + xml2);
////        //System.out.println("----------------------------------------------------------------------------------");
////        //System.out.println("\n\n" + xml.equals(xml2));
//
//
//    }
}
