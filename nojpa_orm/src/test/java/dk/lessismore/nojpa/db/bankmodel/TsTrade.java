package dk.lessismore.nojpa.db.bankmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
public interface TsTrade extends ModelObjectInterface {

    Costumer getCustomer();
    void setCustomer(Costumer costumer);
    
    Issuer getIssuer(); 
    void setIssuer(Issuer issuer);
    

    


}
