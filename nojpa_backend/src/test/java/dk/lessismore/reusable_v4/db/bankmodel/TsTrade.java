package dk.lessismore.reusable_v4.db.bankmodel;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public interface TsTrade extends ModelObjectInterface {

    Costumer getCustomer();
    void setCustomer(Costumer costumer);
    
    Issuer getIssuer(); 
    void setIssuer(Issuer issuer);
    

    


}
