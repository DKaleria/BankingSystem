package by.grgu.reportgenerationservice.service;

import by.grgu.reportgenerationservice.dto.MonthlyReportDTO;
import net.sf.jasperreports.engine.JRException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface ReportService {
    BigDecimal getTotalExpenseForMonth(String username, int month, int year);
    BigDecimal getTotalExpenseForUser(String username);
    BigDecimal getTotalIncomeForMonth(String username, int month, int year);
    BigDecimal getTotalIncomeForUser(String username);
    MonthlyReportDTO generateMonthlyReport(String username, int month, int year);
    void saveMonthlyReport(String username, int month, int year);
    void saveTotalReport(String username);
    String generateTotalExpenseReport(String username, String format) throws JRException;
    String generateMonthlyExpenseReport(String username, int month, int year, String format)
            throws JRException, IOException;
    String generateMonthlyIncomeReport(String username, int month, int year, String format)
            throws JRException, IOException;
    String generateTotalReport(String username, int month, int year, String format)
            throws JRException, IOException;

    String generateIncomeBySourceReport(String username, String format,
                                        int month, int year, String selectedSource)
            throws JRException, IOException;

    List<String> getIncomeSources(String username);
}