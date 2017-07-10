package dk.lessismore.nojpa.reflection.db.attributes;

import dk.lessismore.nojpa.reflection.attributes.Attribute;
import dk.lessismore.nojpa.reflection.attributes.AttributeContainer;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectProxy;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is a wrap around the <tt>AttributeContainer</tt>. It represents an class
 * mapped to a table in a database. The name of the database table which this maps to
 * is allways the Class name with an underscore before like this <tt>_TableName</tt>.
 * The class can analyse the fields in the attributeContainer; and deside which attributes
 * there must be in the table and which data types they should map to. Only attributes
 * which is writable and readable will be accepted.
 * This class can only work with objects that extend the <tt>ModelObject</tt> class. If they
 * dont; the analysation will go wrong. When you have made a new instance of this
 * class please initialize it by calling <tt>setAttributeContainer</tt>.
 *
 * @author LESS-IS-MORE
 * @version 1.0 21-5-2
 */
public class DbAttributeContainer {

    private static final Logger log = LoggerFactory.getLogger(DbAttributeContainer.class);

    /**
     * The table name which this container maps to.
     */
    private String tableName = null;

    private boolean historyEnable = false;
    /**
     * The primary key attribute of the table.
     */
    private DbAttribute primaryKeyAttribute = null;

    /**
     * The attributeContainer which this is a wrap around off.
     */
    private AttributeContainer attributeContainer = null;

    /**
     * The database attributes that should be in the table definition.
     * (key = attributeName, value=DbAttribute)
     */
    private Map<String, DbAttribute> dbAttributes = new HashMap<String, DbAttribute>();

    private String sqlNameQuery = null;

    public DbAttributeContainer() {
    }

    public boolean getHistoryEnable() {
        return historyEnable;
    }

    public void setHistoryEnable(boolean historyEnable) {
        this.historyEnable = historyEnable;
    }

    public String getSqlNameQuery() {
        return sqlNameQuery;
    }

    public void setSqlNameQuery(String sqlNameQuery) {
        this.sqlNameQuery = sqlNameQuery;
    }

    public Map<String, DbAttribute> getDbAttributes() {
        return dbAttributes;
    }


    public DbAttribute getDbAttribute(String attributeName) {
        Map dbAttributes2 = getDbAttributes();
        DbAttribute attribute = (DbAttribute) dbAttributes2.get(attributeName);
        if (attribute == null) {
            log.error("getDbAttributes : returns null .... " + attributeName);
            log.error("Don't think " + attributeName + " is a valid attribute on " + tableName);
            log.debug("getDbAttributes : posible names : ");
            for (Iterator iterator = dbAttributes.values().iterator(); iterator.hasNext();) {
                log.debug("getDbAttributes : name = " + iterator.next());
            }
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for(int i = 0; stackTraceElements != null && i < stackTraceElements.length; i++){
                log.error("StackTrace["+ i +"] of " + attributeName + " on " + tableName + "::" + stackTraceElements[i].getFileName() + "(" + stackTraceElements[i].getClassName() + "):" + stackTraceElements[i].getMethodName() + ":" + stackTraceElements[i].getLineNumber());
            }
            throw new Error("Cant find "+ attributeName + " on " + tableName);
        }
        return attribute;
    }

    /**
     * Call this method to start the analysation of the fields and initialize the
     * container.
     */
    public boolean setAttributeContainer(AttributeContainer attributeContainer) {

        this.attributeContainer = attributeContainer;

        //We check to see if this object is an sub class of the modelObject class.
        //If not we are not able to analyse it.!!
        if (!ModelObjectInterface.class.isAssignableFrom(attributeContainer.getTargetClass())) {
            log.error("setAttributeContainer :: This is no modelclass " + attributeContainer.getTargetClass().getName() + " ... Mayby use " + attributeContainer.getTargetClass().getName() + "Impl ????");
            throw new RuntimeException("AttributeContainer :: This is no modelclass " + attributeContainer.getTargetClass().getName()  + " ... Mayby use " + attributeContainer.getTargetClass().getName() + "Impl ????");
        }

        ModelObject modelObject;
        try {
//            log.debug("setAttributeContainer :: class = " + attributeContainer.getTargetClass());
            modelObject = (ModelObject) ModelObjectProxy.create(attributeContainer.getTargetClass());

            if (modelObject == null) {
                log.error("setAttributeContainer :: modelObject == null");
                return false;
            }
        } catch (Exception e) {
            log.error("setAttributeContainer :: Some exp 1 for " + attributeContainer.getTargetClass().getName() + " " + e.toString());
            e.printStackTrace();
            return false;
        }
        String primaryKeyAttributeName = modelObject.getPrimaryKeyName();

        tableName = "_" + attributeContainer.getClassName();
        for (Iterator ite = attributeContainer.getAttributes().values().iterator(); ite.hasNext();) {
            Attribute attribute = (Attribute) ite.next();
            if (attribute.isReadable() && attribute.isWritable()) {
                //The attribute is writable and readbale; which is nessesary to proceed.
                DbAttribute dbAttribute = new DbAttribute();
                dbAttribute.setTableName(tableName);
                dbAttribute.setAttribute(attribute);
                boolean primaryKey = attribute.getAttributeName().equalsIgnoreCase(primaryKeyAttributeName);
                if (primaryKey && !dbAttribute.isAssociation()) {
                    primaryKeyAttribute = dbAttribute;
                    dbAttribute.setPrimaryKey(true);
                } else {
                    dbAttribute.setPrimaryKey(false);
                }

                getDbAttributes().put(attribute.getInlineAttributeName() != null ? attribute.getInlineAttributeName() : attribute.getAttributeName(), dbAttribute);
            }
        }

        //Check if hte


//        log.debug("setAttributeContainer :: returning true");
        return true;
    }

    public String getTableName() {
        return tableName;
    }

    public String getClassName() {
        return attributeContainer.getClassName();
    }

    public DbAttribute getPrimaryKeyAttribute() {
        return primaryKeyAttribute;
    }

    public AttributeContainer getAttributeContainer() {
        return attributeContainer;
    }

    public boolean setAttributeValue(Object objectToSetOn, DbAttribute attribute, Object value) {
        if(attribute.getInlineAttributeName() != null){
            Object parentValue = attributeContainer.getAttributeValue(objectToSetOn, attribute.getInlineParentName());
            DbAttributeContainer parentDbAttributeContainer = DbClassReflector.getDbAttributeContainer(attribute.getInlineParentClass());
            if(parentValue == null){
                parentValue = ModelObjectService.create(attribute.getInlineParentClass());
                attributeContainer.setAttributeValue(objectToSetOn, attribute.getInlineParentName(), parentValue);
            }
            log.debug("SETTING: parentValue("+ parentValue +")/"+ attribute.getInlineParentClass() +" att("+ attribute.getInlineParentName() +") -> " + value);
            return parentDbAttributeContainer.setAttributeValue(parentValue, attribute.getInlineChildName(), value);

        } else {
            return attributeContainer.setAttributeValue(objectToSetOn, attribute.getAttributeName(), value);
        }
    }

    public boolean setAttributeValue(Object objectToSetOn, String attributeName, Object value) {
        return attributeContainer.setAttributeValue(objectToSetOn, attributeName, value);
    }

    public Object getAttributeValue(Object objectToGetFrom, DbAttribute attribute) {
        //log.debug("getAttributeValue:X1 attribute.getClassName(" + attribute.getClassName() + "), attribute.getAttribute(" + attribute.getAttribute() + "), attribute.getInlineAttributeName(" + attribute.getInlineAttributeName() + ")");
        if(attribute.getInlineAttributeName() != null){
            //log.debug("getAttributeValue:X2");
            Object parentValue = attributeContainer.getAttributeValue(objectToGetFrom, attribute.getInlineParentName());
            //log.debug("getAttributeValue:X3");
            if(parentValue != null){
                //log.debug("getAttributeValue:X4");
                DbAttributeContainer parentDbAttributeContainer = DbClassReflector.getDbAttributeContainer(((ModelObject)parentValue).getInterface());
                //log.debug("getAttributeValue:X5");
                Object attributeValue = parentDbAttributeContainer.getAttributeValue(parentValue, attribute.getInlineChildName());
                //log.debug("getAttributeValue:X6");
                return attributeValue;
            }
            return null;
        } else {
            //log.debug("getAttributeValue:X7");
            Object attributeValue = attributeContainer.getAttributeValue(objectToGetFrom, attribute.getAttributeName());
            //log.debug("getAttributeValue:X8");
            return attributeValue;
        }
    }

    public Object getAttributeValue(Object objectToGetFrom, String attributeName) {
        return attributeContainer.getAttributeValue(objectToGetFrom, attributeName);
    }

    public String getPrimaryKeyValue(ModelObject objectToGetFrom) {
        return objectToGetFrom.getPrimaryKeyValue();
    }

    public int getNrOfDbAttributes() {
        if (getDbAttributes() != null) {
            return getDbAttributes().size();
        } else {
            return 0;
        }
    }

    public String toString() {

        String attributes = "Table:" + getTableName() + "\n";
        for (Iterator iterator = getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute attribute = (DbAttribute) iterator.next();
            attributes += attribute + "\n";
        }
        return attributes;
    }

    public boolean containsLob() {
        return attributeContainer.containsLob();
    }
}
