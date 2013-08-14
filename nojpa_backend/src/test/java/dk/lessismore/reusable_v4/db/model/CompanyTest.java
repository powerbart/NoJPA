package dk.lessismore.reusable_v4.db.model;

import dk.lessismore.reusable_v4.db.methodquery.MQ;
import dk.lessismore.reusable_v4.reflection.db.DatabaseCreator;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectService;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 01-06-11
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class CompanyTest {
//
//    @Test
//    public void someTest2(){
//        Class companyTypeClass = Company.CompanyType.class;
//        Class[] enumConstants = companyTypeClass.getEnumConstants();
//
//    }

    @Test
    public void someBigTest() throws Exception {
        DatabaseCreator.alterDatabase("dk.lessismore.reusable_v4.db.model");

        Woman woman = ModelObjectService.create(Woman.class);
        woman.setName("Some other name");
        ModelObjectService.save(woman);
    }


    @Test
    public void someTest(){
        Company company = ModelObjectService.create(Company.class);
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX " + company.getName());

        company.setName("Some other name");
        company.setMyT(Company.CompanyType.IT);
        company.setMyT(Company.CompanyType.SALES);
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        System.out.println("ModelObjectService.toDebugString(company) = " + ModelObjectService.toDebugString(company));
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        System.out.println("company.getMyT() = " + company.getMyT());
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        ModelObjectService.save(company);


        Company mock = MQ.mock(Company.class);
        Company[] array = MQ.select(mock).where(mock.getMyT(), MQ.Comp.EQUAL, Company.CompanyType.SALES).getArray();
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD -ARRAY");
        for(int i = 0; array != null && i < array.length; i++){
            System.out.println(array[i].getMyT()  + ".... " + ModelObjectService.toDebugString(array[i]));
        }
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD -ARRAY");


//        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX");
//        company.setName(company.getName() + " Sebastian");
//        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX " + company.getName());
//
//        ModelObjectService.save(company);
//        ModelObjectService.delete(company);


    }


}
