package dk.lessismore.reusable_v4.reflection.visitors;

import dk.lessismore.reusable_v4.reflection.attributes.*;
import dk.lessismore.reusable_v4.reflection.*;

/**
 * This is a interface that is used in the visitor design pattern which has been implementet
 * in the <tt>AttributeContainer</tt> class. You should implement this interface if you
 * want to make an visitor which can visit each attributeContainer recursivly.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public interface AttributeContainerVisitor {

    /**
     * Visits an attribute container.
     * @param attributeContainer The attributeContainer to visit.
     * @param prefix The concatenated string of allready visited attribute names. (punktured list)
     */
    public void visitContainer(AttributeContainer attributeContainer, String prefix);
}
