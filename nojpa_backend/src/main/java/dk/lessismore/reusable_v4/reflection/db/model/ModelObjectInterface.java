package dk.lessismore.reusable_v4.reflection.db.model;


import javax.persistence.Column;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Calendar;

public interface ModelObjectInterface {
    boolean isCachable();
    boolean isDirty();
    boolean isNew();

    @Column (length = 32)
    public String getObjectID();
    public void setObjectID(String value); // TODO: Consider removing this method

    Calendar getCreationDate();
    void setCreationDate(Calendar creationDate); // The set methods is needed for for the corresp 

//    @AdminHide
//    @XmlTransient
//    @GenericHtmlAdminForMethod(editViewRowNumber = 3, ignoreThisAttribute = true)
//    Calendar getLastModified();
//    void setLastModified(Calendar lastModified);
//
//    @AdminHide
//    @XmlTransient
//    @GenericHtmlAdminForMethod(ignoreThisAttribute = true)
//    Calendar getLastAccessed();
//    void setLastAccessed(Calendar lastAccessed);
//
//    @AdminHide
//    @XmlTransient
//    @GenericHtmlAdminForMethod(ignoreThisAttribute = true)
//    Calendar getExpireDate();
//    void setExpireDate(Calendar expireDate);

//    void copyNotNullValuesIntoMe(ModelObjectInterface copyFromInterface);
    
}
