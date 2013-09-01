package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import javax.persistence.Column;
import java.util.Calendar;

public interface Company extends ModelObjectInterface {

//    String getName();
//    void setName(String name);
//
//    Person[] getEmployees();
//    void setEmployees(Person[] employees);
//
//    Address getBillingAddress();
//    void setBillingAddress(Address add);


    public String getName();
    public void setName(String name);

    @Column(length = 1000)
    public String getDescription();
    public void setDescription(String description);

    public Person[] getEmployees();
    public void setEmployees(Person[] employees);

    public long getMyLong();
    public void setMyLong(long myLong);

    public int getMyInt();
    public void setMyInt(int myInt);

    public boolean getMyBoolean();
    public void setMyBoolean(boolean myBoolean);

    public float getMyFloat();
    public void setMyFloat(float myFloat);

    public Calendar getMyCalendar();
    public void setMyCalendar(Calendar myCalendar);

    public Person getContactPerson();
    public void setContactPerson(Person contactPerson);

    public Person getCeo();
    public void setCeo(Person ceo);

    public Person getCfo();
    public void setCfo(Person cfo);


}