package dk.lessismore.nojpa.serialization.model;

public class EmployeeImpl {

    private String name;
    private DepartmentImpl department;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DepartmentImpl getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentImpl department) {
        this.department = department;
    }
}
