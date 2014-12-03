package dk.lessismore.nojpa.reflection.util;

import dk.lessismore.nojpa.resources.PropertyService;
import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.attributes.AttributeContainer;
import dk.lessismore.nojpa.reflection.attributes.Attribute;
import dk.lessismore.nojpa.reflection.ClassReflector;
//import dk.lessismore.reusable_v4.generichtmladmin.htmlclassannotations.ListViewShortSelectLabel;

import java.util.Vector;
import java.util.List;
import java.lang.reflect.Method;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 20-01-2010
 * Time: 16:32:01
 * To change this template use File | Settings | File Templates.
 */
public class ReflectionUtil {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReflectionUtil.class);


    static String[] model_packages = null;

    static {
        PropertyResources resources = PropertyService.getInstance().getPropertyResources("GenericHtmlAdmin");
        List list = resources.getList("model_packages");
        if(list != null){
            model_packages = (String[]) list.toArray(new String[list.size()]);
        }
    }


    public static Class loadClass(String simpleClassName){
        for(int i = 0; model_packages != null && i < model_packages.length; i++){
            String tryName = model_packages[i] + "." + simpleClassName;
            try {
                return Class.forName(tryName);
            } catch (ClassNotFoundException e) {
                log.info("Cant find : " + tryName);
            }
        }
        log.error("loadClass: Return null .... Cant find class " + simpleClassName + " within " + model_packages);
        return null;
    }

//
//    public static String getShortlabel(ModelObjectInterface object){
////        System.out.println("---------------------- getShortlabel:starts");
//        if(object == null) return "[-]";
//
//        AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(object);
//        ListViewShortSelectLabel listViewShortSelectLabel = (ListViewShortSelectLabel) attributeContainer.getTargetClass().getAnnotation(ListViewShortSelectLabel.class);
//        String fill = listViewShortSelectLabel == null ? "objectID (creationDate)" : listViewShortSelectLabel.label();
//
//
//        String result = "" + fill;
//        StringTokenizer toks = new StringTokenizer(fill, "$ ()[]{},.:;-_'+#\"%&/=-\\");
//        while(toks.hasMoreTokens()){
//            String aName = toks.nextToken();
//            Attribute attribute = attributeContainer.getAttributes().get(aName);
//            if(attribute == null){
//                log.error("Cant find att:'"+ aName +"' within " + attributeContainer);
//            }
////            String value = ""+  attribute.getAttributeValuePrettyPrint(object);
////TODO:            result = result.replaceAll(aName, value);
//        }
////        System.out.println("---------------------- getShortlabel:ends");
//        return result;
//    }

    public static void copyNotNulls(Object fromObject, Object targetObject, List<String> ignoreAttributes){
        AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(targetObject);

        Method[] oriMets = fromObject.getClass().getMethods();
        for(int i = 0; i < oriMets.length; i++){
            Method met = oriMets[i];
            String fieldName = met.getName().substring(3);
            fieldName = fieldName.substring(0, 1).toLowerCase()+fieldName.substring(1);
            if(met.getName().startsWith("get") && met.getParameterTypes().length == 0 && (ignoreAttributes == null || !ignoreAttributes.contains(fieldName))){
                try {
                    String setname = "set" + met.getName().substring(3);
                    //log.debug("Trying : " + setname + " /" + met.getReturnType().getSimpleName());
                    Method newCopyMet = null;
                    try {
                        newCopyMet = targetObject.getClass().getMethod(setname, met.getReturnType());
                    } catch(NoSuchMethodException ex){

                    }
                    if(newCopyMet != null){
                        Object value = met.invoke(fromObject);
                        //log.debug("Calling " + met.getName() + " with ("+ value +")");
                        if(value != null){
                            //TODO: Arrays ???
                            if(ModelObjectInterface.class.isAssignableFrom( met.getReturnType())){
                                //TODO: Check if this assoiations exists first, before making a new instance
                                //log.debug("Check_for_old " + ((ModelObject) targetObject).getInterface() + "." + met.getName());
                                Object objectInterface = null;
                                try {
                                    Attribute attribute = attributeContainer.getAttribute( fieldName );
                                    objectInterface = attribute.getAttributeValue(targetObject);
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                                if(objectInterface == null){
                                    //log.debug("creating a new " + met.getReturnType() + " for " + met.getName());
                                    objectInterface = ModelObjectService.create((Class<? extends ModelObjectInterface>) met.getReturnType());
                                } else {
                                    //log.debug("using existing object for  " + met.getReturnType() + " for " + met.getName());
                                }
                                copyNotNulls(value, objectInterface);
                                //log.debug("" + met.getReturnType());
                                //log.debug("" + value);
                                //log.debug("");
                                //log.debug("SETTING(M) "+ met.getName() +" ... value = " + value);
                                newCopyMet.invoke(targetObject, objectInterface);
                            } else if(value instanceof String) {
                                if(!value.equals("null")){
                                    newCopyMet.invoke(targetObject, value);
                                } else {
                                    //log.debug("NOT coping  for string ... value = " + value);
                                }
                            } else {
                                //log.debug("SETTING(O) "+ met.getName() +" ... value = " + value);
                                newCopyMet.invoke(targetObject, value);
                            }
                        } else {
                            //log.debug("NOT coping "+ met.getName() +" ... value = " + value);
                        }
                    } else {
                        //log.debug("newCopyMet = " + newCopyMet);
                    }
                } catch (Exception e) {
                    log.error("Some error " + e, e);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
        //log.debug("copyNotNulls:DONE for targetObject("+ targetObject +")");
    }


    public static void copyNotNulls(Object fromObject, Object targetObject) {
        copyNotNulls(fromObject, targetObject, null);
    }







}
