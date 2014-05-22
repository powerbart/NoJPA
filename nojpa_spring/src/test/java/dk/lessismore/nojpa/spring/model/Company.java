package dk.lessismore.nojpa.spring.model;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
//@Searchable(impl = Company1Searcher.Indexer.class)
public interface Company extends ModelObjectInterface {

    Person[] getPersons();
    void setPersons(Person[] persons);

    Person getCeo();
    void setCeo(Person ceo);

    String getName();
    void setName(String name);

}
