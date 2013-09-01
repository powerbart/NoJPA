package dk.lessismore.nojpa.reflection.visitors;

import dk.lessismore.nojpa.reflection.attributes.*;

/**
 * This interface is a part of the visitor design pattern implemented in the
 * <tt>AttributeContainer</tt>. You should implement this interface if you
 * want to visit all attributes in a attribute container and recursivly down.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public interface AttributeVisitor {

    /**
     * @param attribute The attribute to visit.
     * @param prefix The concatenated string of allready visited attribute names. (punktured list)
     */
    public void visitAttribute(Attribute attribute, String prefix);
}
