package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.properties.Default;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.annotations.IndexField;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Calendar;
import java.util.Locale;

@XmlJavaTypeAdapter(ModelXmlAdapter.class)
public interface Person extends ModelObjectInterface {


    public enum SEX { MALE, FEMALE, NEW_SHIT }


    SEX getSex();
    void setSex(SEX sex);

    @Default (value = "MyName")
    @IndexField
    @SearchField()
    String getName();
    void setName(String name);

    @SearchField()
    String getUrl();
    @DbStrip(stripItHard = false, stripItSoft = false)
    void setUrl(String url);

//    String[] getMyName();
//    void setMyName(String[] myAlias);



    Calendar getLastModified();
    void setLastModified(Calendar lastModified);


    @SearchField(dynamicSolrPostName = "_da_TXT", translate = true, searchReverse = true, reverseBoostFactor = 0.3f, boostFactor = 4.3f)
    String getFun();
    void setFun(String fun, Locale locale);

    @SearchField
    String getDescription();
    void setDescription(String description);

    @SearchField
    Calendar getBirthDate();
    void setBirthDate(Calendar birthDate);

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

    @SearchField
    Address[] getAddresses();
    void setAddresses(Address[] addresses);

    Person[] getChildren();
    void setChildren(Person[] children);
}