package main.java.ru.clevertec.check;

import main.java.ru.clevertec.check.OrderService.Order;

public class CheckRunner {

    private static final String PATH_RESULT = "result.csv";

    public static final Boolean CONSOLE_ADDITIONAL_INFO = true;

    public static void main(String[] args) {

        OrderService orderService = new OrderService();
        CashierCheckService cashierCheckService = new CashierCheckService();

        try {
            Order order = orderService.parseFromConsoleArgs(args);
            CashierCheck cashierCheck = cashierCheckService.getCashierCheckByOrder(order);

            CSVWriter.writeToCSV(cashierCheck, PATH_RESULT);
        } catch (Exception e) {
            handleException(e);
        }

    }

    private static void handleException(Exception e) {
        try {
            CSVWriter.writeToCSV(e, PATH_RESULT);
        } catch (Exception ex) {
            System.err.println("Failed to write exception to CSV: " + ex.getMessage());
        }
    }

}