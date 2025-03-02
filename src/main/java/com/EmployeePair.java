package com;

public class EmployeePair {
    private final String emp1;
    private final String emp2;
    private final String projectId;
    private final int daysWorked;

    public EmployeePair(String emp1, String emp2, String projectId, int daysWorked) {
        this.emp1 = emp1;
        this.emp2 = emp2;
        this.projectId = projectId;
        this.daysWorked = daysWorked;
    }

    public String getEmp1() {
        return emp1;
    }

    public String getEmp2() {
        return emp2;
    }

    public String getProjectId() {
        return projectId;
    }

    public int getDaysWorked() {
        return daysWorked;
    }

    @Override
    public String toString() {
        return "EmployeePair{" +
                "emp1='" + emp1 + '\'' +
                ", emp2='" + emp2 + '\'' +
                ", projectId='" + projectId + '\'' +
                ", daysWorked=" + daysWorked +
                '}';
    }
}
