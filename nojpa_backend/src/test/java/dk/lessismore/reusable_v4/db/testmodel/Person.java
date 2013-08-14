package dk.lessismore.reusable_v4.db.testmodel;

import dk.lessismore.reusable_v4.properties.Default;
import dk.lessismore.reusable_v4.reflection.db.annotations.DbStrip;
import dk.lessismore.reusable_v4.reflection.db.annotations.IndexField;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import dk.lessismore.reusable_v4.reflection.maputil.ReusableMap;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(ModelXmlAdapter.class)
public interface Person extends ModelObjectInterface {


    @ReusableMap(mapNames = {"map1", "map2"})
    @Default (value = "MyName")
    @IndexField
    String getName();
    void setName(String name);

//    String[] getMyName();
//    void setMyName(String[] myAlias);


    @ReusableMap(mapNames = {"map2"})
    boolean getIsSick();
    void setIsSick(boolean isSick);

    int getCountOfCars();
    void setCountOfCars(int countOfCars);

    String getDescription();
    @DbStrip (stripItSoft = true)
    void setDescription(String description);

    Car getCar();
    void setCar(Car car);

    @ReusableMap(mapNames = {"map3"})
    Cpr getCpr();
    void setCpr(Cpr cpr);

    Address[] getAddresses();
    void setAddresses(Address[] addresses);

    Person[] getChildren();
    void setChildren(Person[] children);
}