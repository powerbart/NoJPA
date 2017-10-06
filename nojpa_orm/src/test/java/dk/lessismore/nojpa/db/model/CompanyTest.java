package dk.lessismore.nojpa.db.model;

import dk.lessismore.nojpa.db.SQLStatementExecutor;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.statements.SQLStatement;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 01-06-11
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class CompanyTest {


    @BeforeClass
    public static void setUp() {
        List<SQLStatement> tables = new LinkedList<SQLStatement>();

        tables.addAll(DatabaseCreator.makeTableFromClass(Person.class));
        tables.addAll(DatabaseCreator.makeTableFromClass(Woman.class));
        tables.addAll(DatabaseCreator.makeTableFromClass(Company.class));
        tables.addAll(DatabaseCreator.makeTableFromClass(MrRich.class));


        for(SQLStatement sqlStatement: tables) {
            String sql = sqlStatement.makeStatement();
            SQLStatementExecutor.doUpdate(sql);
        }
    }

//
//    @Test
//    public void someTest2(){
//        Class companyTypeClass = Company.CompanyType.class;
//        Class[] enumConstants = companyTypeClass.getEnumConstants();
//
//    }

    @Test
    public void someBigTest() throws Exception {
        DatabaseCreator.alterDatabase("dk.lessismore.nojpa.db.model");

        Woman woman = ModelObjectService.create(Woman.class);
        woman.setName("Some other name");
        save(woman);
    }

    private static void save(ModelObjectInterface o){
        ModelObjectService.save(o);
    }



    @Test
    public void someTest(){
        {
            Company company = ModelObjectService.create(Company.class);
            company.setName("The_Boss");
            ArrayList<Person> ps = new ArrayList<Person>();
            for (int i = 0; i < 10; i++) {
                Person person = ModelObjectService.create(Person.class);
                ps.add(person);

            }
            company.setEmployees(ps.toArray(new Person[ps.size()]));
            company.setMyT(Company.CompanyType.SALES);
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX " + company.getNumberOfEmployees());

            company.setName("Some other name");
//        company.setMyT(Company.CompanyType.IT);
//        company.setMyT(Company.CompanyType.SALES);
            System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
            System.out.println("ModelObjectService.toDebugString(company) = " + ModelObjectService.toDebugString(company));
            System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
            System.out.println("company.getMyT() = " + company.getMyT());
            System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
            save(company);


            Company mock = MQL.mock(Company.class);
            Company[] array = MQL.select(mock).where(mock.getMyT(), MQL.Comp.EQUAL, Company.CompanyType.SALES).getArray();
            System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD -ARRAY");
            for (int i = 0; array != null && i < array.length; i++) {
                System.out.println(array[i].getMyT() + ".... " + ModelObjectService.toDebugString(array[i]));
            }
            System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD -ARRAY");
        }
        {

            Company mock = MQL.mock(Company.class);
            Company c = MQL.select(mock).where(mock.getMyT(), MQL.Comp.EQUAL, Company.CompanyType.SALES).getFirst();
            for(int i = 0; i < c.getEmployees().length; i++){
                Person person = c.getEmployees()[i];
                person.setAge((person.getAge() + 1) * 2);
            }
            c.setName("Second time");
            save(c);





        }





//        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX");
//        company.setName(company.getName() + " Sebastian");
//        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX " + company.getName());
//
//        save(company);
//        ModelObjectService.delete(company);


    }


}
