package dk.lessismore.nojpa.db.model;

import javax.persistence.Column;

public interface Woman extends Person {

    @Column(length = 2000)
    public String getFavouriteColor();
    public void setFavouriteColor(String favouriteColor);

    @Column(length = 5000)
    public String getRawHtml();
    public void setRawHtml(String rawHtml);



}
