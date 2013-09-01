package dk.lessismore.nojpa.reflection.db;

import dk.lessismore.nojpa.reflection.db.attributes.*;

/**
 * This class can make the name of an association table, which is created when ever we have
 * an multiassociation. It allso contains the names of the source key and target key of this
 * table.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class AssociationTable {


    public static final String SOURCE = "source_id";
    public static final String TARGET = "target_id";
    public static String makeAssociationTableName(DbAttributeContainer dbAttributeContainer, DbAttribute dbAttribute) {
        return dbAttributeContainer.getTableName()+"_"+dbAttribute.getAttributeName();
    }
}
