package main.java.ru.clevertec.check;

import static main.java.ru.clevertec.check.OrderService.Order;

public class CashierCheckService {

    private static final CashierCheckCalculatorService calculatorService = new CashierCheckCalculatorService();

    public CashierCheck getCashierCheckByOrder(Order order){
        return calculatorService.calculate(order);
    }


}
