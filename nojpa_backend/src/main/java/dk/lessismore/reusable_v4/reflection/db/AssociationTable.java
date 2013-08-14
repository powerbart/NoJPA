package dk.lessismore.reusable_v4.reflection.db;

import dk.lessismore.reusable_v4.resources.*;
import dk.lessismore.reusable_v4.reflection.*;
import dk.lessismore.reusable_v4.reflection.db.attributes.*;
import dk.lessismore.reusable_v4.reflection.attributes.*;
import dk.lessismore.reusable_v4.reflection.db.model.*;
import dk.lessismore.reusable_v4.db.statements.*;
import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

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
