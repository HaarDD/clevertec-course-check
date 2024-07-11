package main.java.ru.clevertec.check;


import java.math.BigDecimal;
import java.util.Map;

public class OrderService {

    private final ConsoleOrderParser orderParser = new ConsoleOrderParser();

    public Order parseFromConsoleArgs(String[] args){
        return orderParser.parse(args);
    }

    public record Order(Map<Integer, Integer> productIdQuantity, BigDecimal balanceDebitCard, String discountCard) {}

}
