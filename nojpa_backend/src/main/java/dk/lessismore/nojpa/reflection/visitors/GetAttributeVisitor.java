package dk.lessismore.nojpa.reflection.visitors;

import dk.lessismore.nojpa.reflection.attributes.*;

import java.util.*;
/**
 * This is a implementation of the visitor design pattern. It can get a copy of the
 * desired attribute form an attribute path name.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class GetAttributeVisitor implements AttributeContainerVisitor {

    private int nrOfAttributeIdentifiersLeft = 0;
    private StringTokenizer attributeIdentifierTokens = null;
    private Object object = null;
    private Attribute _attribute = null;

    public GetAttributeVisitor() {}

    public void setAttributePathName(String identifier) {
        attributeIdentifierTokens = new StringTokenizer(identifier, ".");
        nrOfAttributeIdentifiersLeft = attributeIdentifierTokens.countTokens();
    }
    public void setObject(Object object) {
        this.object = object;
    }
    public Attribute getAttribute() {
        return _attribute;
    }
    public void visitContainer(AttributeContainer attributeContainer, String prefix) {
        nrOfAttributeIdentifiersLeft--;
        String attributeIdentifier = attributeIdentifierTokens.nextToken();
        Attribute attribute = attributeContainer.getAttribute(attributeIdentifier);

        if(attribute != null) {
            if(nrOfAttributeIdentifiersLeft == 0) {
                _attribute = attribute;
                return ; //Finished;
            }
            else {
                object = attribute.getAttributeValue(object);
                attribute.visit(this, prefix);
            }
        }
    }


}
