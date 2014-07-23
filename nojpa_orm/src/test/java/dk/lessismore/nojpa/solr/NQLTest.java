package dk.lessismore.nojpa.solr;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.testmodel.Person;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.SolrServiceImpl;
import org.junit.Test;

/**
 * Created by niakoi on 7/23/14.
 */
public class NQLTest {
    @Test
    public void test01() {
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);
        NQL.search(mPerson).searchIsNull(mPerson.getCar()).getList();

        NQL.search(mPerson).searchNotNull(mPerson.getCpr()).getList();
    }
}
