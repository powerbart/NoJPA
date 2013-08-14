package dk.lessismore.reusable_v4.reflection.attributes;

import java.util.*;
import java.lang.reflect.*;
import java.lang.annotation.Annotation;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObject;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import dk.lessismore.reusable_v4.reflection.util.*;
import dk.lessismore.reusable_v4.reflection.visitors.*;
import dk.lessismore.reusable_v4.utils.Pair;
import dk.lessismore.reusable_v4.utils.GenericComparator;
import org.apache.log4j.Logger;

/**
 * This class can reflect a generic class; and identify the attributes which the class has. It
 * allso works as an container for the relfected fields; so that you can use this class to
 * access the fields. This class can only identify attributes if they have either a get or a set
 * method; or if the attribute is a public field. If a field is not a standard field (string, boolean
 * int etc.) this class will make a recursive call; which makes an new AttributeContainer. You
 * should therefor be carefull about which classes you analyse because you can start a really big
 * analyse process; which may take some time.
 * <br>NB: this works allso for inheritance.
 * <br>When you have made an instance of this class please call the method <tt>findAttributes</tt>;
 * which starts the analysation of the class.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class AttributeContainer {

    private static final org.apache.log4j.Logger log = Logger.getLogger(AttributeContainer.class);


    /**
     * Should we allow static attributes.
     */
    private static boolean _staticAttributesAllowed = false;

    /**
     * Should we look for attributes in set and get methods.
     */
    private static boolean _findAttributesInMethods = true;

    /**
     * Should we look for attributes in public fields.
     */
    private static boolean _findAttributesInFields = true;

    /**
     * The class which the attribute in this container belongs to.
     */
    private Class _targetClass = null;

    /**
     * The attributes of the target class. key=attributeName, value=Attribute
     */
    private Map<String, Attribute> _attributes = new HashMap<String, Attribute>();

    public AttributeContainer() { }

    public Class getTargetClass() {
        return _targetClass;
    }

    /**
     * Gets the name of the class which this container is build on. This name does not
     * include the classpath; but only the name ex. "AttributeContainer".
     */
    public String getClassName() {
        return ClassAnalyser.getClassName(getTargetClass());
    }
    /**
     * This method starts the process that identifies the attributes of the class.
     */
    public void findAttributes(ModelObjectInterface target) {
        findAttributes(((ModelObject)target).getInterface());
    }

    /**
     * This method starts the process that identifies the attributes of the class.
     */
    public void findAttributes(Class targetClass) {
        findAttributes(targetClass, _staticAttributesAllowed, _findAttributesInMethods, _findAttributesInFields);
    }

    /**
     * This method starts the process that identifies the attributes of the class.
     */
    public void findAttributes(Class targetClass, boolean staticAttributesAllowed, boolean findAttributesInMethods, boolean findAttributesInFields) {
        _targetClass = targetClass;
        if(findAttributesInMethods)
            findAttributesFromMethods(staticAttributesAllowed);
        if(findAttributesInFields)
            findAttributesFromFields(staticAttributesAllowed);
    }

    /**
     * This method analyses the class, and findes the attributes in it; from the
     * get and set methods in the class.
     */
    protected void findAttributesFromMethods(boolean staticAttributesAllowed) {
        //Get public methods.
        Method[] methods = getTargetClass().getMethods();
	//log.debug("findAttributesFromMethods:1");
        //Handle get Methods.
        for(int i = 0; i < methods.length; i++) {
	        //log.debug("findAttributesFromMethods:2 ... " + methods[i].getDeclaringClass());
            //log.debug(getTargetClass().getName() + ": Current Method: " + methods[i].getName());
            if(staticAttributesAllowed || ClassAnalyser.isMethodStatic(methods[i])) {
                Method method = methods[i];
                if(ClassAnalyser.isValidGetMethod(method)) {
		    //log.debug("findAttributesFromMethods:3");
                    String attributeName = ClassAnalyser.getAttributeNameFromMethod(method);
                    if(attributeName.isEmpty())
                        continue;
		    //log.debug("findAttributesFromMethods:4");
                    Attribute attribute = (Attribute)getAttributes().get(attributeName);
                    if(attribute == null ) {
                        //This attribute has not been made before. We make it.
                        MethodAttribute methodAttribute = new MethodAttribute();
                        methodAttribute.setDeclaringClass( methods[i].getDeclaringClass() );
                        methodAttribute.setGetMethod(method);
                        getAttributes().put(methodAttribute.getAttributeName(), methodAttribute);
			//log.debug("findAttributesFromMethods:5");
                    }
                    else if(attribute instanceof MethodAttribute) {
                        MethodAttribute methodAttribute = (MethodAttribute)attribute;
                        methodAttribute.setGetMethod(method);
                    }
                }
            }
        }
        //Handle set methods.
        for(int i = 0; i < methods.length; i++) {
	    //log.debug("findAttributesFromMethods:6");
            if(_staticAttributesAllowed || !Modifier.isStatic(methods[i].getModifiers())) {
                Method method = methods[i];
                if(ClassAnalyser.isValidSetMethod(method)) {
                    String attributeName = ClassAnalyser.getAttributeNameFromMethod(method);
                    if(attributeName.isEmpty())
                        continue;

                    Attribute attribute = (Attribute)getAttributes().get(attributeName);
                    if(attribute == null ) {
                        //We have not encountered this attribute before
                        MethodAttribute methodAttribute = new MethodAttribute(getTargetClass());
                        methodAttribute.setSetMethod(method);
                        //log.debug("method = " + method.getName());
                        getAttributes().put(methodAttribute.getAttributeName(), methodAttribute);
                    }
                    else if(attribute instanceof MethodAttribute) {
                        //We have encountered this attribute before (maybe as an get method).
                        MethodAttribute methodAttribute = (MethodAttribute)attribute;
                        Method oldSetMethod = methodAttribute.getSetMethod();
                        if(oldSetMethod != null) {
                            //If we allready have an set method we check to see if its
                            //argument class type match that of the get method
                            //If so its a better match and should be used instead.
                            Class methodParameterClass = method.getParameterTypes()[0];
                            Class getMethodReturnClass = methodAttribute.getGetMethod().getReturnType();
                            if(methodParameterClass.getName().equals(getMethodReturnClass.getName()))
                                methodAttribute.setSetMethod(method);
                        }
                        else
                            methodAttribute.setSetMethod(method);
                    }
                }
            }
        }
	//log.debug("findAttributesFromMethods:7");
        //Remove the method attributes which has different class types for the set and get method.
        final Collection<Attribute> attributeCollection = getAttributes().values();
        Attribute[] atts = attributeCollection.toArray(new Attribute[attributeCollection.size()]);
        for (int i = 0; i < atts.length; i++) {
	    //log.debug("findAttributesFromMethods:8 - loop");
            Attribute attribute = atts[i];
            if(attribute instanceof MethodAttribute) {
                MethodAttribute methodAttribute = (MethodAttribute)attribute;
                Method getMethod = methodAttribute.getGetMethod();
                Method setMethod = methodAttribute.getSetMethod();
                if(getMethod != null && setMethod != null) {
                    if(!getMethod.getReturnType().getName().equals(setMethod.getParameterTypes()[0].getName())) {
                        if(!getMethod.getReturnType().isAssignableFrom(setMethod.getParameterTypes()[0])) {
                            //The set and get method is reflecting a attribute which has
                            //different class types. This is not allowed; because it
                            //will cause some confusion; on which type the attribute is.
                            getAttributes().remove(methodAttribute.getAttributeName());
                            String message = "findAttributesFromMethods: WARNING: " + methodAttribute.getAttributeName() + " will not become a db-attribute, because type not match";
                            log.warn(message, new Exception(message));

                        }
                    }
                } else {
                    getAttributes().remove(methodAttribute.getAttributeName());
                }
            }
        }
    }



    /**
     * This method will analyse the target class; and finde the attribute from the
     * public fields.
     */
    protected void findAttributesFromFields(boolean staticAttributesAllowed) {

        //Get all the public fields of the target class.
        Field[] fields = getTargetClass().getFields();

        for(int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if(staticAttributesAllowed || ClassAnalyser.isFieldStatic(field)) {
                String attributeName = ClassAnalyser.getAttributeNameFromField(field);
                if(attributeName.isEmpty())
                    continue;

                Attribute attribute = (Attribute)getAttributes().get(attributeName);
                if(attribute == null) {
                    //We have not encountered the attribute before. We create it.
                    FieldAttribute fieldAttribute = new FieldAttribute(getTargetClass());
                    fieldAttribute.setDeclaringClass( field.getDeclaringClass() );
                    fieldAttribute.setField(field);
                    getAttributes().put(fieldAttribute.getAttributeName(), fieldAttribute);
                }
                else if(attribute instanceof MethodAttribute) {
                    //The attribute has been encountered before; but through methods..
                    //If attribute is uncomplete; missing get or set method; we overwrite it
                    //with this new attribute with field accessor.
                    MethodAttribute methodAttribute = (MethodAttribute)attribute;
                    if(methodAttribute.getSetMethod() == null || methodAttribute.getGetMethod() == null) {
                        FieldAttribute fieldAttribute = new FieldAttribute();
                        fieldAttribute.setField(field);
                        getAttributes().put(fieldAttribute.getAttributeName(), fieldAttribute);
                    }
                }
            }
        }
    }

    /**
     * Gets the attribute hashtable (key=attributeName, value=Attribute).
     */
    public Map<String, Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Gets an attribute from the container with the given name.
     * @return The attribute which has the attributeName. If is not found
     * null will be returned.
     */
    public Attribute getAttribute(String attributeName) {
        return (Attribute)getAttributes().get(attributeName);
    }

    /**
     * This is the main entrance to visit the container; with an AttributeContainerVisitor.
     */
    public void visit(AttributeContainerVisitor visitor) {
        visit(visitor, "");
    }
    public void visit(AttributeContainerVisitor visitor, String prefix) {
        visitor.visitContainer(this, prefix );
    }
    public void visit(AttributeVisitor visitor) {
        visit(visitor, "");
    }

    /**
     * We visit all of the attributes in the container.
     */
    public void visit(AttributeVisitor visitor, String prefix) {
        for (Iterator iterator = getAttributes().values().iterator(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            //if(!attribute.getAttributeClass().getName().equals(getTargetClass().getName())) {
                //This is the loop prevention. If we have encountered the attribute name before
                //this will be present in the prefix.
                if(prefix.indexOf("."+attribute.getAttributeName()) == -1)
                    visitor.visitAttribute(attribute, Attribute.makePrefix(prefix, attribute.getAttributeName()));
            //}
        }
    }

    /**
     * This method can set an attribute value at a given attribute. The attribute must be
     * present and must be writable.
     */
    public boolean setAttributeValue(Object objectToSetOn, String attributeName, Object value) {

        Attribute attribute = getAttribute(attributeName);
        if(attribute.getAttributeClass().isEnum()){
            if(value == null){
                attribute.setAttributeValuePlain(objectToSetOn, null);
            } else {
                Object[] enumConstants = attribute.getAttributeClass().getEnumConstants();
                for(int i = 0; enumConstants != null && i < enumConstants.length; i++){
                    if(enumConstants[i].toString().equals("" + value)){
                        attribute.setAttributeValuePlain(objectToSetOn, enumConstants[i]);
                        return true;
                    }
                }
            }
        }


        return attribute.setAttributeValue(objectToSetOn, value);
    }

    /**
     * This attribute sets an value at an attribute. The method analyses the attributePathName; and
     * determines if this is the last stop on the line. If not; we call recursive down to
     * the next attributeContainer. You can therefor give an attributePathName which is like
     * this <tt>globe.position.x</tt>. This will create 2 recursive calls until we reach the
     * destination and can set the x value.
     */
    public boolean setAttributeValueFromPathName(Object objectToSetOn, String attributePathName, Object value) {
        SetAttributeValueVisitor visitor = new SetAttributeValueVisitor();
        visitor.setAttributePathName(attributePathName);
        visitor.setObjectToSetOn(objectToSetOn);
        visitor.setValue(value);
        this.visit(visitor);
        return visitor.isSuccessfull();
    }


    public Object getAttributeValue(Object objectToGetFrom, String attributeName) {
        Attribute attribute = getAttribute(attributeName);
        return attribute.getAttributeValue(objectToGetFrom);
    }

    /**
     * This method gets an attribute with the desired attributepath name.
     */
    public Attribute getAttributeFromPathName(Object objectToGetFrom, String attributePathName) {
        GetAttributeVisitor visitor = new GetAttributeVisitor();
        visitor.setAttributePathName(attributePathName);
        visitor.setObject(objectToGetFrom);
        this.visit(visitor);
        return visitor.getAttribute();
    }
    public Object getAttributeValueFromPathName(Object objectToGetFrom, String attributePathName) {
        GetAttributeValueVisitor visitor = new GetAttributeValueVisitor();
        visitor.setObjectToGetFrom(objectToGetFrom);
        visitor.setAttributePathName(attributePathName);
        this.visit(visitor);
        return visitor.getValue();
    }
    public String toString() {

        String attributes = "";
        for (Iterator iterator = getAttributes().values().iterator(); iterator.hasNext();) {
           Attribute attribute = (Attribute)iterator.next();
            attributes += attribute+"\n";
        }
        return attributes;
    }

    /**
     * This method will get a list of all of the attribute names recusivly down from this
     * point. The names will be the attributePath Names.
     */
    public Vector getAttributeNamesRecursive() {
        GetAttributeNamesVisitor nameVisitor = new GetAttributeNamesVisitor();
        nameVisitor.setIgnoreNotConvertable(false);
        this.visit(nameVisitor);
        return nameVisitor.getAttributeNames();
    }

    public <T extends Annotation> Attribute[] getAttributesWithAnnotaions(Class<T> annotationsClass, String dotNotationAttNames) {
        Map<String, Attribute> map = getAttributes();
        ArrayList<AttPair> pairs = new ArrayList<AttPair>(map.size());
        for(Iterator<Attribute> attributeIterator = map.values().iterator(); attributeIterator.hasNext(); ){
            Attribute attribute = attributeIterator.next();
            T t = attribute.getAnnotation(annotationsClass);
            if(t != null){
                pairs.add(new AttPair(t , attribute));
            }
        }
        Collections.sort(pairs, new GenericComparator(AttPair.class, "first." + dotNotationAttNames));
        ArrayList<Attribute> toReturn = new ArrayList<Attribute>();
        for(int i = 0; i < pairs.size(); i++){
            toReturn.add(pairs.get(i).getSecond());
        }
        return toReturn.toArray(new Attribute[toReturn.size()]);
    }


    public <T extends Annotation> List<Attribute> getAttributesWithAnnotation(Class<T> annotationsClass) {
        Collection<Attribute> allAttributes = getAttributes().values();
        List<Attribute> selectedAttributes = new ArrayList<Attribute>();
        for (Attribute attribute : allAttributes) {
            T t = attribute.getAnnotation(annotationsClass);
            if (t != null) {
                selectedAttributes.add(attribute);
            }
        }
        return selectedAttributes;
    }
    
    public <T extends Annotation> List<Attribute> getAttributesWithAnnotation(final Class<T> annotationsClass, final Comparator<T> comparator) {
        List<Attribute> attributes = getAttributesWithAnnotation(annotationsClass);
        Comparator<Attribute> attributeComparator = new Comparator<Attribute>() {
            @Override
            public int compare(Attribute attribute1, Attribute attribute2) {
                T viewOrderAnnotation1 = attribute1.getAnnotation(annotationsClass);
                T viewOrderAnnotation2 = attribute2.getAnnotation(annotationsClass);
                return comparator.compare(viewOrderAnnotation1, viewOrderAnnotation2);
            }
        };
        Collections.sort(attributes, attributeComparator);
        return attributes;
    }

    public <T extends Annotation> List<Attribute> getAttributesWithoutAnnotation(Class<T> annotationsClass) {
        Collection<Attribute> allAttributes = getAttributes().values();
        List<Attribute> selectedAttributes = new ArrayList<Attribute>();
        for (Attribute attribute : allAttributes) {
            T t = attribute.getAnnotation(annotationsClass);
            if (t == null) {
                selectedAttributes.add(attribute);
            }
        }
        return selectedAttributes;
    }

    public static class AttPair extends Pair<Annotation, Attribute> {
        public AttPair(Annotation a, Attribute att){
            super(a, att);
        }

        @Override
        public Annotation getFirst() {
            return super.getFirst();    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public void setFirst(Annotation first) {
            super.setFirst(first);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public Attribute getSecond() {
            return super.getSecond();    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public void setSecond(Attribute second) {
            super.setSecond(second);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }


}
