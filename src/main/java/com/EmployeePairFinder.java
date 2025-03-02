package com;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmployeePairFinder {

    // Regex to check if a date consists of three numeric parts separated by '-'
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{1,4}-\\d{1,2}-\\d{1,4}$");

    public EmployeePairFinder() {}

    /**
     * Finds the pair of employees who have worked together on the same projects for the longest period.
     * The method follows the following steps:
     *     1. Reads the CSV file and stores raw data in a collection of strings.
     *     2. Validated the fields and removes the ones with invalid dates
     *     2. Infers the date format dynamically from the data.
     *     3. Converts the raw data into a list of {@link EmployeeRecord} objects.
     *     4. Identifies employee pairs {@link EmployeePair} who have worked on common projects.
     *     5. Prints the results, displaying employee pairs and their total days worked together.
     */
    public void findLongestWorkingEmployeePair(String filePath) {
        List<List<String>> rawData = readCsvAsRawStrings(filePath);

        rawData = validateDateFields(rawData);

        String inferredFormat = inferDateFormat(rawData);
        System.out.println("Inferred Date Format: " + inferredFormat);

        Map<String, List<EmployeeRecord>> employeeRecords = mapToEmployeeRecords(rawData, inferredFormat);

        List<EmployeePair> employeePairs = findPairs(employeeRecords);

        employeePairs.forEach(System.out::println);
    }

    /**
     * Reads the CSV file into a List of Lists of Strings, keeping all data as raw text.
     * The first row of the CSV file is skipped, as it contains no actual data.
     * From there, each row in the CSV file is stored as a List of strings
     * into the rawData list.
     *
     * @param filePath Path to the CSV file.
     * @return A list of lists containing CSV rows as raw strings.
     */
    public List<List<String>> readCsvAsRawStrings(String filePath) {
        List<List<String>> rawData = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            reader.readNext(); // Skip header

            while ((line = reader.readNext()) != null) {
                if(line.length != 4) {
                    System.out.println("Skipping row due to incorrect column count: " + "[" + String.join(",", line) + "]");
                } else rawData.add(List.of(line));
            }
        } catch (Exception e) {
            System.out.println("Exception occurred during parsing CSV file: " + e.getMessage());
        }
        return rawData;
    }

    /**
     * Validates the date fields to ensure they follow a valid structure before inferring the format.
     * This checks if the date has three numeric parts separated by '-'
     *
     * @param rawData List of raw CSV data.
     * @return A filtered list containing only rows with valid date formats.
     */
    public List<List<String>> validateDateFields(List<List<String>> rawData) {
        return rawData.stream().filter(row -> {
            if (!isValidDate(row.get(2))
                    || (row.get(3).isEmpty() || (!row.get(3).equalsIgnoreCase("NULL") && !isValidDate(row.get(3))))) {
                System.out.println("Skipping invalid date format in row: " + "[" + String.join(",", row) + "]");
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Checks if a date string follows a valid format (three numeric parts separated '-').
     *
     * @param dateStr The date string.
     * @return True if the date is valid, false otherwise.
     */
    private boolean isValidDate(String dateStr) {
        return DATE_PATTERN.matcher(dateStr).matches();
    }

    /**
     * Infers the date format from the raw CSV data.
     * Loops through all entries and finds the first date where there
     * is a number bigger than 12, that is not a year. If no such date is found,
     * either yyyy-MM-dd or dd-MM-yyyy is returned, depending on the
     * startsWithYear boolean.
     * The startsWithYear boolean in set to true if the first valid date
     * starts with a year, and false otherwise.
     *
     * Then, each part of the date is parsed as a number.
     * It is checked whether the first or the third part is
     * for the year.
     *
     * From there, if one of the two other parts is
     * bigger than 12, then it can be safely concluded that it's
     * for the days, and by knowing which part is for the year,
     * the date format can be inferred and returned.
     *
     * @param rawData List of lists containing raw CSV data.
     * @return The inferred date format.
     */
    public String inferDateFormat(List<List<String>> rawData) {
        Boolean startsWithYear = null;

        for (List<String> row : rawData) {

            String[] dates = {row.get(2), row.get(3)}; // DateFrom and DateTo

            for (String dateStr : dates) {
                if (dateStr.isEmpty() || dateStr.equalsIgnoreCase("NULL")) continue;

                String[] parts = dateStr.split("[-/]");
                if (parts.length != 3) continue;

                try {
                    int part1 = Integer.parseInt(parts[0]);
                    int part2 = Integer.parseInt(parts[1]);
                    int part3 = Integer.parseInt(parts[2]);

                    boolean part1IsYear = (part1 >= 1900 && part1 <= 2100);
                    boolean part3IsYear = (part3 >= 1900 && part3 <= 2100);

                    if(startsWithYear == null)
                        startsWithYear = part1IsYear;

                    if(part1IsYear) {
                        if(part2 > 12)
                            return "yyyy-dd-MM";
                        else if(part3 > 12)
                            return "yyyy-MM-dd";
                    } else if(part3IsYear) {
                        if(part1 > 12)
                            return "dd-MM-yyyy";
                        else if(part2 > 12)
                            return "MM-dd-yyyy";
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore non-numeric parsing issues
                }
            }
        }

        return Boolean.TRUE.equals(startsWithYear) ?  "yyyy-MM-dd" : "dd-MM-yyyy";
    }

    /**
     * Maps the raw CSV data into a List of EmployeeRecords.
     * <p>
     * Each list contains 4 strings. One for the employeeId,
     * one for the projectId, dateFrom and dateTo.
     * The dates are parsed using the inferred dateformat.
     * If dateTo is empty or "null", it gets the value of the
     * current day.
     *
     * @param rawData    List of lists containing raw CSV data.
     * @param dateFormat The inferred date format to use.
     * @return A list of EmployeeRecord objects.
     */
    public Map<String, List<EmployeeRecord>> mapToEmployeeRecords(List<List<String>> rawData, String dateFormat) {
        Map<String, List<EmployeeRecord>> projectIdToEmployeeRecords = new HashMap<>();

        rawData.stream().forEach(row -> {
            try {
                String empId = row.get(0);
                String projectId = row.get(1);

                LocalDate dateFrom = parseDate(row.get(2), dateFormat);
                LocalDate dateTo;
                if(row.get(3).isEmpty() || row.get(3).equalsIgnoreCase("NULL"))
                    dateTo = LocalDate.now();
                else
                    dateTo = parseDate(row.get(3), dateFormat);

                projectIdToEmployeeRecords
                        .computeIfAbsent(projectId, x -> new ArrayList<>())
                        .add(new EmployeeRecord(empId, projectId, dateFrom, dateTo));
            } catch (Exception ignored) {
            }
        });

        return projectIdToEmployeeRecords;
    }

    /**
     * Parses a date string using the inferred format.
     *
     * @param dateStr The date string.
     * @param inferredFormat The format inferred from CSV data.
     * @return A LocalDate object.
     */
    private LocalDate parseDate(String dateStr, String inferredFormat) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(inferredFormat));
    }

    /**
     * Finds pairs of employees who worked together on the same project and calculates their overlap period.
     *
     * @param employeeRecordsMap Map of parsed employee records.
     * @return A list of employee pairs with the project ID and days worked together.
     */
    public List<EmployeePair> findPairs(Map<String, List<EmployeeRecord>> employeeRecordsMap) {
        List<EmployeePair> pairs = new ArrayList<>();

        long longestTimeWorking = Integer.MIN_VALUE;

        for(String projectId: employeeRecordsMap.keySet()) {
            List<EmployeeRecord> currProjectRecords = employeeRecordsMap.get(projectId);
            for (int i = 0; i < currProjectRecords.size(); i++) {
                for (int j = i + 1; j < currProjectRecords.size(); j++) {
                    EmployeeRecord emp1 = currProjectRecords.get(i);
                    EmployeeRecord emp2 = currProjectRecords.get(j);

                    if (Objects.equals(emp1.projectId, emp2.projectId)) {
                        LocalDate overlapStart = emp1.dateFrom.isAfter(emp2.dateFrom) ? emp1.dateFrom : emp2.dateFrom;
                        LocalDate overlapEnd = emp1.dateTo.isBefore(emp2.dateTo) ? emp1.dateTo : emp2.dateTo;

                        if (!overlapStart.isAfter(overlapEnd)) {
                            long daysWorked = overlapEnd.toEpochDay() - overlapStart.toEpochDay();
                            if(daysWorked > longestTimeWorking) {
                                longestTimeWorking = daysWorked;
                                pairs.clear();
                                pairs.add(new EmployeePair(emp1.getEmpId(), emp2.getEmpId(), emp1.getProjectId(), (int) daysWorked));
                            } else if(daysWorked == longestTimeWorking) {
                                pairs.add(new EmployeePair(emp1.getEmpId(), emp2.getEmpId(), emp1.getProjectId(), (int) daysWorked));
                            }
                        }
                    }
                }
            }
        }

        return pairs;
    }


}