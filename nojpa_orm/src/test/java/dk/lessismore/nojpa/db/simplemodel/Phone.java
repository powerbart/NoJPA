package dk.lessismore.nojpa.db.simplemodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import javax.persistence.Column;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public interface Phone extends ModelObjectInterface {

    @Column(length = 1000)
    public String getDescription();
    public void setDescription(String description);

    @Column(length = 100000)
    public String getLongDescription();
    public void setLongDescription(String description);


}
