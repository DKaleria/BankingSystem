package by.grgu.reportgenerationservice.service;

import by.grgu.reportgenerationservice.dto.MonthlyReportDTO;
import net.sf.jasperreports.engine.JRException;

import java.math.BigDecimal;

public interface ReportService {
    BigDecimal getTotalExpenseForMonth(String username, int month, int year);
    BigDecimal getTotalExpenseForUser(String username);
    BigDecimal getTotalIncomeForMonth(String username, int month, int year);
    BigDecimal getTotalIncomeForUser(String username);
    MonthlyReportDTO generateMonthlyReport(String username, int month, int year);
    void saveMonthlyReport(String username, int month, int year);
    void saveTotalReport(String username);
    void generateTotalExpenseReport(String username) throws JRException;
}