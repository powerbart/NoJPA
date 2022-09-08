package dk.lessismore.nojpa.db.model;

import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

public interface DbStripTest extends ModelObjectInterface {

    String getMyString();
    void setMyString(String myString);

    String getNonStripped();
    @DbStrip(stripItHard = false, stripItSoft = true)
    void setNonStripped(String nonStripped);

    String getOneUrl();
    @DbStrip(stripItHard = false, urlEncode = true)
    void setOneUrl(String nonStripped);
}
