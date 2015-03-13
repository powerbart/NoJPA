package dk.lessismore.nojpa.reflection.db.model;


import dk.lessismore.nojpa.reflection.db.annotations.SearchField;

import javax.persistence.Column;
import java.util.Calendar;

public interface ModelObjectInterface {
    boolean isCachable();
    boolean isDirty();
    boolean isNew();

    @Column (length = 32)
    public String getObjectID();
    public void setObjectID(String value); // TODO: Consider removing this method

    @SearchField
    Calendar getCreationDate();
    void setCreationDate(Calendar creationDate); // The set methods is needed for for the corresp

    Class<? extends ModelObjectInterface> getInterface();


//    Calendar getLastModified();
//    void setLastModified(Calendar lastModified);
//

//    Calendar getLastAccessed();
//    void setLastAccessed(Calendar lastAccessed);
//

//    Calendar getExpireDate();
//    void setExpireDate(Calendar expireDate);


}
