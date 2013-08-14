package dk.lessismore.reusable_v4.reflection.visitors;

import dk.lessismore.reusable_v4.reflection.attributes.*;
import java.util.*;
/**
 * This is an implementation of the attribute visitor pattern. This may visit all
 * attributes recusivly down, in the class/attribute structure. It makes a list
 * of all the names (with attribute paths).
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class GetAttributeNamesVisitor implements AttributeVisitor {

    /**
     * The attributes names; with path.
     */
    private Vector attributeNames = null;

    /**
     * Should we ignore attributes as arrays.
     */
    private boolean ignoreArrays = true;

    /**
     * Should we ignore all attributes which can not be converted.
     */
    private boolean ignoreNotConvertable = true;

    /**
     * Should we only admit attributes which is writable.
     */
    private boolean onlyWritableAttributes = false;

    /**
     * Should we only admit attribute which is readable.
     */
    private boolean onlyReadableAttributes = false;

    public GetAttributeNamesVisitor() {

    }


    public void setIgnoreArrays(boolean ignoreArrays) {
        this.ignoreArrays = ignoreArrays;
    }
    public void setIgnoreNotConvertable(boolean ignoreNotConvertable) {
        this.ignoreNotConvertable = ignoreNotConvertable;
    }
    public void setOnlyWritableAttributes(boolean onlyWritableAttributes) {
        this.onlyWritableAttributes = onlyWritableAttributes;
    }
    public void setOnlyReadableAttributes(boolean onlyWritableAttributes) {
        this.onlyWritableAttributes = onlyWritableAttributes;
    }

    /**
     * Gets a list of all attribute names (with path); from the bigging class and
     * recusivly down.
     */
    public Vector getAttributeNames() {
        if(attributeNames == null)
            attributeNames = new Vector();
        return attributeNames;
    }

    /**
     * This method will be called by the AttributeContainer if this
     * visitor is allowed to visit the attribute.
     */
    public void visitAttribute(Attribute attribute, String prefix) {
        boolean convertable = attribute.isConvertable();
        boolean add = true;
        if(ignoreArrays && attribute.isArray()) {
            add = false;
        }
        if(ignoreNotConvertable && !convertable)
            add = false;
        if(onlyWritableAttributes)
            add = attribute.isWritable();
        else if(onlyReadableAttributes)
            add = attribute.isReadable();

        if(add)
            getAttributeNames().addElement(prefix);
        if(!convertable) {
            //Recursive call.
            attribute.visit(this, prefix);
        }
    }
}
