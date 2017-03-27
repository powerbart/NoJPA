package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.annotations.DbInline;
import dk.lessismore.nojpa.reflection.db.annotations.IndexClass;
import dk.lessismore.nojpa.reflection.db.annotations.IndexField;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

@IndexClass("street,zip")
public interface Address extends ModelObjectInterface {

    @SearchField
    String getStreet();
    void setStreet(String street);

    String getCity();
    void setCity(String city);

    String getArea();
    void setArea(String area);

    @SearchField
    @IndexField("zip_index_custom_name")
    int getZip();
    void setZip(int zip);


    Phone getA();
    void setA(Phone phone);

    Phone getB();
    void setB(Phone phone);



}