package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.properties.Default;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.annotations.IndexField;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.maputil.ReusableMap;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(ModelXmlAdapter.class)
public interface Person extends ModelObjectInterface {


    @ReusableMap(mapNames = {"map1", "map2"})
    @Default (value = "MyName")
    @IndexField
    @SearchField
    String getName();
    void setName(String name);

//    String[] getMyName();
//    void setMyName(String[] myAlias);


    @ReusableMap(mapNames = {"map2"})
    boolean getIsSick();
    void setIsSick(boolean isSick);

    int getCountOfCars();
    void setCountOfCars(int countOfCars);

    @SearchField
    float getSomeFloat();
    void setSomeFloat(float countOfCars);

    long getCountOfFriends();
    void setCountOfFriends(long countOfFriends);

    String getDescription();
    @DbStrip (stripItSoft = true)
    void setDescription(String description);

    @SearchField
    Car getCar();
    @SearchField
    void setCar(Car car);

    @ReusableMap(mapNames = {"map3"})
    Cpr getCpr();
    void setCpr(Cpr cpr);

    @SearchField
    PersonStatus getPersonStatus();
    void setPersonStatus(PersonStatus personStatus);

    Address[] getAddresses();
    void setAddresses(Address[] addresses);

    Person[] getChildren();
    void setChildren(Person[] children);
}