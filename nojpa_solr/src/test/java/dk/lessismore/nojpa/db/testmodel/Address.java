package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.annotations.IndexField;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

public interface Address extends ModelObjectInterface {

    @SearchField
    String getStreet();
    void setStreet(String street);

    String getCity();
    void setCity(String city);

    String getArea();
    void setArea(String area);

    @SearchField
    @IndexField
    int getZip();
    void setZip(int zip);
}