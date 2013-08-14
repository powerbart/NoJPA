package future_test.model;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
//@Searchable(impl = Company1Searcher.Indexer.class)
public interface Company {

    Person[] getPersons();
    void setPersons();

    String getName();
    void setName();



}
