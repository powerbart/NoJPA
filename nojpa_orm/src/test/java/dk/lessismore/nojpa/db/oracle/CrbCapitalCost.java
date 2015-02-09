package dk.lessismore.nojpa.db.oracle;

import dk.lessismore.nojpa.db.statements.oracle.PackageTarget;

import java.util.Calendar;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
@PackageTarget(name = "crb_capital_cost_pkg", dataSource = "MyDataSource")
public interface CrbCapitalCost {

//get_measures (rs                     IN OUT refcur,
// p_report_date          IN     DATE,
//    p_counterpart_su_key   IN     NUMBER,
//    p_str                  IN     VARCHAR2,
//    p_legal_entity_id      IN     NUMBER)

    Iterator<CcbCapitalCostRow> getMeasures(Calendar reportDate, long counterpartSuKey, String myMsg, long legalID);




}
