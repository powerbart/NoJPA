package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.properties.Default;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.annotations.IndexField;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.maputil.ReusableMap;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Calendar;
import java.util.Locale;

@XmlJavaTypeAdapter(ModelXmlAdapter.class)
public interface Person extends ModelObjectInterface {


    @ReusableMap(mapNames = {"map1", "map2"})
    @Default (value = "MyName")
    @IndexField
    @SearchField()
    String getName();
    void setName(String name);

//    String[] getMyName();
//    void setMyName(String[] myAlias);



//    @SearchField(translate = true, searchReverse = true, reverseBoostFactor = 0.3f, boostFactor = 4.3f)
    String getFun();
    void setFun(String fun);
    void setFun(String fun, Locale locale);

    @SearchField
    String getDescription();
    void setDescription(String description);

    @SearchField
    Calendar getBirthDate();
    void setBirthDate(Calendar birthDate);

    @ReusableMap(mapNames = {"map2"})
    @SearchField
    boolean getIsSick();
    void setIsSick(boolean isSick);

    int getCountOfCars();
    void setCountOfCars(int countOfCars);

    @SearchField
    float getSomeFloat();
    void setSomeFloat(float countOfCars);

    @SearchField
    double getSomeDouble();
    void setSomeDouble(double someDouble);

    @SearchField
    long getCountOfFriends();
    void setCountOfFriends(long countOfFriends);


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

    @SearchField
    Person getGirlFriend();
    void setGirlFriend(Person girlFriend);

//    @SearchField
//    PersonStatus[] getHistoryStatus();
//    void setHistoryStatus(PersonStatus[] personStatus);

    Address[] getAddresses();
    void setAddresses(Address[] addresses);

    Person[] getChildren();
    void setChildren(Person[] children);
}