package dk.lessismore.nojpa.reflection.visitors;

import dk.lessismore.nojpa.reflection.attributes.*;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectProxy;

import java.util.*;
/**
 * This class is an implementation of the visitor design pattern. You can use it
 * to set an attribute value from a attribute path.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class SetAttributeValueVisitor implements AttributeContainerVisitor {

    private int nrOfAttributeIdentifiersLeft = 0;
    private StringTokenizer attributeIdentifierTokens = null;
    private Object objectToSetOn = null;
    private Object valueToSet = null;
    private boolean successfull = false;

    public void setAttributePathName(String attributePathName) {
        attributeIdentifierTokens = new StringTokenizer(attributePathName, ".");
        nrOfAttributeIdentifiersLeft = attributeIdentifierTokens.countTokens();
    }
    public void setObjectToSetOn(Object objectToSetOn) {
        this.objectToSetOn = objectToSetOn;
    }
    public void setValue(Object valueToSet) {
        this.valueToSet = valueToSet;
    }
    public boolean isSuccessfull() {
        return successfull;
    }
    public void visitContainer(AttributeContainer attributeContainer, String prefix) {
        nrOfAttributeIdentifiersLeft--;
        String attributeIdentifier = attributeIdentifierTokens.nextToken();
        if(nrOfAttributeIdentifiersLeft == 0) {

            successfull = attributeContainer.setAttributeValue(objectToSetOn, attributeIdentifier, valueToSet);
            return ; //Finished.
        }
        else {

            //log.debug(attributeIdentifier);
            Attribute attribute = attributeContainer.getAttribute(attributeIdentifier);
            if(attribute != null) {
                Object newObjectToSetOn = attribute.getAttributeValue(objectToSetOn);
                if(newObjectToSetOn == null) {
                     if(!attribute.isArray()) {
                        try {
                            newObjectToSetOn = ModelObjectProxy.create(attribute.getAttributeClass());
                            attribute.setAttributeValue(objectToSetOn, newObjectToSetOn);
                        }catch(Exception e) {
                            newObjectToSetOn = null;
                        }
                     }
                }
                if(newObjectToSetOn != null) {
                    objectToSetOn = newObjectToSetOn;
                    //Recursive call.
                    attribute.visit(this, prefix);
                }
            }
        }
    }
}
