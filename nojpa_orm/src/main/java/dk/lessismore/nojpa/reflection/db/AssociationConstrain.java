package dk.lessismore.nojpa.reflection.db;

import java.util.*;

/**
 * This class can determine if the database reflection library may load, save or delete an given association.
 * You can specify which associations should be allowed; or not allowed. Its allso
 * possible to set it to allow all associations; or to not allow all associations.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class AssociationConstrain {

    /**
     * Should we allow all associations.
     */
    public static final int ALLOW_ALL_ASSOCIATIONS = 0;

    /**
     * Should we not allow any associations.
     */
    public static final int DONT_ALLOW_ANY_ASSOCIATIONS = 1;

    /**
     * Should we only allow the associations which is registred at this class in the list.
     */
    public static final int ALLOW_ASSOCIATIONS_IN_LIST = 2;

    /**
     * Should we not allow any of the associations which is registred at this class.
     */
    public static final int DONT_ALLOW_ASSOCIATIONS_IN_LIST = 3;

    /**
     * The constrain type to allow. Default set to ALLOW_ALL_ASSOCIATIONS.
     */
    private int associationConstrainType = ALLOW_ALL_ASSOCIATIONS;

    /**
     * List containg names of the associations which is either allowed or not allowed.
     * A association name consist of the hole attributepath!
     */
    private List associations = null;

    public AssociationConstrain() {

    }

    public List getAssociations() {
        if(associations == null)
            associations = new LinkedList();
        return associations;
    }

    public void addAssociation(String attributePath) {
        getAssociations().add(attributePath);
    }

    public void setAllowAllAssociations() {
        associationConstrainType = ALLOW_ALL_ASSOCIATIONS;
    }
    public void setDontAllowAnyAssociations() {
        associationConstrainType = DONT_ALLOW_ANY_ASSOCIATIONS;
    }
    public void setAssociationConstrainType(int type) {
        associationConstrainType = type;
    }
    public boolean isNotAllowedAssociation(String attributePath) {
        return !attributePath.equals("") && !isAllowedAssociation(attributePath);
    }
    /**
     * This method determines wether this association with the given attribute path
     * is allowed.
     */
    public boolean isAllowedAssociation(String attributePath) {

        switch(associationConstrainType) {
            case ALLOW_ALL_ASSOCIATIONS:
                return true;
            case DONT_ALLOW_ANY_ASSOCIATIONS:
                return false;
            case ALLOW_ASSOCIATIONS_IN_LIST:
                return getAssociations().contains(attributePath);
            case DONT_ALLOW_ASSOCIATIONS_IN_LIST:
                return !getAssociations().contains(attributePath);
            default:
                return true;
        }
    }
    public static String addAttributeToPath(String attributePath, String attributeName) {
        if(attributePath == null || attributePath.isEmpty())
            return attributeName;
        else
            return attributePath+"."+attributeName;
    }
}
