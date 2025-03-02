
import com.EmployeePair;
import com.EmployeePairFinder;
import com.EmployeeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmployeePairFinderTest {

    private EmployeePairFinder finder;
    private File testCsvFile;

    /**
     * Sets up the EmployeePairFinder instance and creates a temporary CSV file for testing.
     */
    @BeforeEach
    void setUp() throws Exception {
        finder = new EmployeePairFinder();
        testCsvFile = createTestCsvFile();
    }

    /**
     * Tests if the CSV file is correctly read into raw string data.
     */
    @Test
    void testReadCsvAsRawStrings() {
        List<List<String>> rawData = finder.readCsvAsRawStrings(testCsvFile.getAbsolutePath());

        assertNotNull(rawData);
        assertFalse(rawData.isEmpty());
    }

    /**
     * Tests if the method correctly filters out invalid date formats.
     */
    @Test
    void testValidateDateFields() {
        List<List<String>> rawData = finder.readCsvAsRawStrings(testCsvFile.getAbsolutePath());
        List<List<String>> validData = finder.validateDateFields(rawData);

        assertNotNull(validData);
        assertTrue(validData.size() < rawData.size()); // Some invalid rows should be filtered out
    }

    /**
     * Tests if the correct date format is inferred.
     */
    @Test
    void testInferDateFormat() {
        List<List<String>> rawData = finder.readCsvAsRawStrings(testCsvFile.getAbsolutePath());
        List<List<String>> validData = finder.validateDateFields(rawData);
        String inferredFormat = finder.inferDateFormat(validData);

        assertNotNull(inferredFormat);
        assertEquals("yyyy-MM-dd", inferredFormat);
    }

    /**
     * Tests if raw CSV data is correctly mapped to EmployeeRecord objects.
     */
    @Test
    void testMapToEmployeeRecords() {
        List<List<String>> rawData = finder.readCsvAsRawStrings(testCsvFile.getAbsolutePath());
        List<List<String>> validData = finder.validateDateFields(rawData);
        String inferredFormat = finder.inferDateFormat(validData);
        Map<String, List<EmployeeRecord>> employeeRecords = finder.mapToEmployeeRecords(validData, inferredFormat);

        assertNotNull(employeeRecords);
        assertFalse(employeeRecords.isEmpty());

        // Checking at least one sample parsed date
        EmployeeRecord sampleRecord = employeeRecords.values().iterator().next().getFirst();
        assertEquals(LocalDate.of(2013, 11, 1), sampleRecord.getDateFrom());
    }

    /**
     * Tests if the program correctly finds the longest-working employee pair.
     */
    @Test
    void testFindPairs() {
        List<List<String>> rawData = finder.readCsvAsRawStrings(testCsvFile.getAbsolutePath());
        List<List<String>> validData = finder.validateDateFields(rawData);
        String inferredFormat = finder.inferDateFormat(validData);
        Map<String, List<EmployeeRecord>> employeeRecords = finder.mapToEmployeeRecords(validData, inferredFormat);
        List<EmployeePair> employeePairs = finder.findPairs(employeeRecords);

        assertNotNull(employeePairs);
        assertFalse(employeePairs.isEmpty());

        // Ensuring the longest-working pair has the highest number of days worked together
        int maxDaysWorked = employeePairs.stream()
                .mapToInt(EmployeePair::getDaysWorked)
                .max()
                .orElse(0);

        assertTrue(maxDaysWorked > 0);
    }

    /**
     * Creates a temporary CSV file with sample test data.
     *
     * @return A temporary File object pointing to the CSV file.
     */
    private File createTestCsvFile() throws Exception {
        Path tempFile = Files.createTempFile("test_employees", ".csv");
        File file = tempFile.toFile();
        file.deleteOnExit();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(
                    "EmpID,ProjectID,DateFrom,DateTo\n" +
                            "143,12,2013-11-01,2014-01-05\n" +
                            "218,10,2012-05-16,NULL\n" +
                            "143,10,2009-01-01,2011-04-27\n" +
                            "432,12,2015-03-25,2016-07-19\n" +
                            "218,12,2014-02-01,2015-06-30\n" +
                            "512,14,2013-10-05,2015-05-12\n" +
                            "219,14,2013-05-10,2014-10-20\n" +
                            "432,10,2011-06-30,2013-12-15\n" +
                            "143,12,2016-04-10,NULL\n" +
                            "512,10,2011-07-01,INVALID_DATE\n" + // Invalid Date
                            "219,12,INVALID,2016-11-30\n" + // Invalid Date
                            "143,14,2012-09-18,2014-12-21\n" +
                            "218,14,2014-11-30,2016-05-10\n" +
                            "512,12,2015-02-15,NULL\n" +
                            "432,14,2013-03-05,2014-09-15\n"
            );
        }
        return file;
    }
}
