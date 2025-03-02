package com;

public class Main {

    // The path to the csv file.
    private final static String FILE_PATH = "src/main/resources/employee_project_data_1.csv";

    /**
     * {@link EmployeePairFinder} class is created and
     * findLongestWorkingEmployeePair method is called with the
     * file path. The results are going to be printed out.
     */
    public static void main(String[] args) {
        EmployeePairFinder employeePairFinder = new EmployeePairFinder();
        employeePairFinder.findLongestWorkingEmployeePair(FILE_PATH);
    }
}