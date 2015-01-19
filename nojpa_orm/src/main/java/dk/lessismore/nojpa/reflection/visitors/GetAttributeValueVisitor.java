package dk.lessismore.nojpa.reflection.visitors;

import dk.lessismore.nojpa.reflection.attributes.*;

import java.util.*;
/**
 * This is an implementation of the visitor design pattern. Use this visitor
 * when you want to get an attribute value; from an attribute where you
 * have the attribute path name.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class GetAttributeValueVisitor implements AttributeContainerVisitor {

    /**
     * Variable to keep track of how fare we have gone in the recursive process.
     */
    private int nrOfAttributeIdentifiersLeft = 0;

    /**
     * The tokens of the attribute path.4
     */
    private StringTokenizer attributeIdentifierTokens = null;

    /**
     * The object to get the attribute value from-
     */
    private Object objectToGetFrom = null;

    /**
     * The value.
     */
    private Object value = null;

    public void setAttributePathName(String identifier) {
        attributeIdentifierTokens = new StringTokenizer(identifier, ".");
        nrOfAttributeIdentifiersLeft = attributeIdentifierTokens.countTokens();
    }
    public void setObjectToGetFrom(Object objectToGetFrom) {
        this.objectToGetFrom = objectToGetFrom;
    }
    public Object getValue() {
        return value;
    }
    /**
     * This method will be called by the attributecontainer.
     * @param attributeContainer The container to visit.
     * @param prefix The concatenated attribute names which has been visited.
     */
    public void visitContainer(AttributeContainer attributeContainer, String prefix) {
        nrOfAttributeIdentifiersLeft--;
        String attributeIdentifier = attributeIdentifierTokens.nextToken();

        Attribute attribute = attributeContainer.getAttribute(attributeIdentifier);
        if(attribute != null) {
            objectToGetFrom = attribute.getAttributeValue(objectToGetFrom);
            if(objectToGetFrom != null) {
                if(nrOfAttributeIdentifiersLeft == 0) {
                    value = objectToGetFrom;
                    return ; //Finished.
                }
                else
                    attribute.visit(this, prefix);
            }
        }
    }
}
