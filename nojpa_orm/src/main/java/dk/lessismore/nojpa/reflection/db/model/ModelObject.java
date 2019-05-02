package dk.lessismore.nojpa.reflection.db.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.*;

/**
 * DO NOT USE THIS INTERFACE (except in the ORM layer).
 * It has become an implementation detail, and may change dramatically without notice.
 *
 * This class administrate the base of an model object from the data model. It can registrate when an object becomes
 * dirty (changed) and has a flag indicating if its an new instance with a new primary key. All data model objects
 * should implement this class; and implement the nessesary logic to determine when the object becomes dirty. NB: the
 * object becomes dirty when just one attribute changes. To make it easy to determine when an attribute is changing
 * there have been made a number of generic methods called <tt>makesDirty</tt>. This will automaticly determine if the
 * attribute is change (this is allso the case with arrays). adasd
 *
 * @author LESS-IS-MORE
 * @version 1.0 21-5-2
 */
@XmlAccessorType(XmlAccessType.NONE)
public interface ModelObject extends Serializable, ModelObjectInterface, Comparable {

    public Class<? extends ModelObjectInterface> getInterface();

    public <T extends ModelObjectInterface> T getProxyObject();

    public boolean isCachable();

    public void setCachable(boolean isCachable);

    public boolean isDirty();

    public void setDirty(boolean isDirty);

    public boolean isNew();

    public void setNew(boolean isNew);

    public void removeAllListnersForFieldName(String fieldName);

    public void removeAllListeners();

    public boolean containsAssociationInCache(String fieldName);


    public String toDebugString(String delim);

    public void setSingleAssociationID(String fieldName, String objectID);

    public void makesDirty();

    public void makesDirtyForAssociation(Boolean value1, Boolean value2, String fieldName);

    public void makesDirtyForAssociation(Integer value1, Integer value2, String fieldName);

    public void makesDirtyForAssociation(Double value1, Double value2, String fieldName);

    public void makesDirtyForAssociation(Float value1, Float value2, String fieldName);

    public void makesDirtyForAssociation(Long value1, Long value2, String fieldName);


    /**
     * Determines if we are changing the primary key; and the object should be new.
     */
    public void newPrimaryKey(String value1, String value2);

    /**
     * Get the value of the primary key attribute
     */
    public String getPrimaryKeyValue();

    /**
     * Get the name of the primary key.
     */
    public String getPrimaryKeyName();

    public int compareTo(Object obj);

    /**
     * Implementes the equals - metode.
     */
    public boolean equals(Object obj);

    public boolean equalsRef(ModelObject obj);

    /**
     * Implementes the toString - metode. DO NOT Attemt to overload this !!
     */
    public String toString();


    public Calendar getCreationDate();

    public void setCreationDate(Calendar creationDate);

//    public Calendar getLastModified();
//
//    public void setLastModified(Calendar lastModified);
//
//    public Calendar getLastAccessed();
//
//    public void setLastAccessed(Calendar lastAccessed);
//
//    public Calendar getExpireDate();
//
//    public void setExpireDate(Calendar expireDate);

    public void save();

    public void doneSavingByDbObjectWriter();

    boolean doRemoteCache();

    static enum ArrayIsNullResult { YES_IS_NULL, NO_NOT_NULL, DONT_KNOW };
    ArrayIsNullResult isArrayNull(String fieldName);

    void putInInstanceCache(String key, Object object, int secondsToLive);
    Object getFromInstanceCache(String key);
    void removeFromInstanceCache(String key);

    public boolean delete();

    public boolean deleteWithAssociations(String commaStringOfAttributeNames);

    public boolean deleteWithOutAssociations(String commaStringOfAttributeNames);

    public int hashCode();

    public String getSingleAssociationID(String attributeName);

//    void copyNotNullValuesIntoMe(ModelObjectInterface copyFromInterface);

}
