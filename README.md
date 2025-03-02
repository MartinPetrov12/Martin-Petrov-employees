# Employee Pair Finder
## Project Overview 
This project identifies the pair of employees who have worked together on common projects for the longest period of time.

# Assumptions
- CSV file will always contain a header
- All dates in a single file have the same date format
- Only first and last part of date can be a year


## Build the Project

To compile the project and build the executable JAR file, run:
```
mvn clean install compile
```
## Running Tests

This project includes JUnit 5 tests. To run all tests:
```
mvn test
```

# Example CSV files
I have provided 2 .csv files to experiment with. They are
located in the resources folder. The second contains some invalid rows, 
which are ignored by the application. 

# Set-up
If the project is build and run, it is going to use the employee_project_data_1.csv file. 
If you would like to change it to the other provided file, or to your own one, 
you would need to specify the `FILE_PATH` variable in Main.java. 

# Project Structure

```
employee-pair-finder/
│── src/
│   ├── main/
│   │   ├── java/com/
│   │   │   ├── EmployeePairFinder.java  # Core logic
│   │   │   ├── EmployeeRecord.java      # Employee data model
│   │   │   ├── EmployeePair.java        # Employee pair result model
│   │   └── resources/
│   │       ├── employee_project_data_1.csv            # Sample CSV file
|   |       ├── employee_project_data_2.csv            # Sample CSV file
│   ├── test/
│   │   ├── java/com/
│   │   │   ├── EmployeePairFinderTest.java # JUnit test cases
│── pom.xml   # Maven build configuration
│── README.md # Project documentation
```




