package main.java.ru.clevertec.check;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.ru.clevertec.check.CustomExceptions.BadRequestException;
import main.java.ru.clevertec.check.OrderService.Order;

import static main.java.ru.clevertec.check.CheckRunner.CONSOLE_ADDITIONAL_INFO;

public class ConsoleOrderParser {

    private static final Pattern PATTERN_ITEM = Pattern.compile("(\\d+)-(\\d+)");
    private static final Pattern PATTERN_DISCOUNT_CARD = Pattern.compile("discountCard=(\\d{4})");
    private static final Pattern PATTERN_BALANCE_CARD = Pattern.compile("balanceDebitCard=(-?\\d+(\\.\\d{1,2})?)");

    public Order parse(String[] args) {
        Map<Integer, Integer> itemIdQuantity = new LinkedHashMap<>();
        BigDecimal balanceDebitCard = null;
        String discountCard = null;

        for (String arg : args) {

            Matcher matcher = PATTERN_ITEM.matcher(arg);

            if (matcher.matches()) {
                int id = Integer.parseInt(matcher.group(1));
                int quantity = Integer.parseInt(matcher.group(2));
                itemIdQuantity.put(id, itemIdQuantity.getOrDefault(id, 0) + quantity);
                continue;
            }

            matcher = PATTERN_DISCOUNT_CARD.matcher(arg);
            if (matcher.matches()) {
                discountCard = matcher.group(1);
                continue;
            }

            matcher = PATTERN_BALANCE_CARD.matcher(arg);
            if (matcher.matches()) {
                balanceDebitCard = new BigDecimal(matcher.group(1));
            }
        }

        if (itemIdQuantity.isEmpty()) {
            throw new BadRequestException("Order is empty!");
        }

        if (balanceDebitCard == null) {
            throw new BadRequestException("Balance Debit Card is required!");
        }

        if (CONSOLE_ADDITIONAL_INFO) {
            System.out.printf("Order successfully parsed.%n" +
                            "Items count: %d%n" +
                            "Discount card: %s%n" +
                            "Balance Debit Card: %.2f%n%n",
                    itemIdQuantity.size(),
                    discountCard,
                    balanceDebitCard);
        }

        return new Order(itemIdQuantity, balanceDebitCard, discountCard);
    }


}
