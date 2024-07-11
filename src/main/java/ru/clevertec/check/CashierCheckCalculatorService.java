package main.java.ru.clevertec.check;

import main.java.ru.clevertec.check.CashierCheck.BasketPosition;
import main.java.ru.clevertec.check.CashierCheck.DateTime;
import main.java.ru.clevertec.check.CashierCheck.DiscountInfo;
import main.java.ru.clevertec.check.CashierCheck.Total;
import main.java.ru.clevertec.check.CustomExceptions.BadRequestException;
import main.java.ru.clevertec.check.CustomExceptions.NotEnoughMoneyException;
import main.java.ru.clevertec.check.DiscountCardsService.DiscountCard;
import main.java.ru.clevertec.check.ProductsService.Product;
import main.java.ru.clevertec.check.OrderService.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static main.java.ru.clevertec.check.CheckRunner.CONSOLE_ADDITIONAL_INFO;

public class CashierCheckCalculatorService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private final static Integer WHOLESALE_DEFAULT_MIN_QUANTITY = 5;
    private final static Integer WHOLESALE_DEFAULT_DISCOUNT_PERCENTAGE = 10;
    private final static Integer DEFAULT_DISCOUNT_PERCENTAGE = 2;
    private final static Integer BIG_DECIMAL_SCALE_DEFAULT = 2;

    private final ProductsService productsService = new ProductsService();
    private final DiscountCardsService discountCardsService = new DiscountCardsService();

    public CashierCheck calculate(Order order) {

        DateTime dateTime = new DateTime(LocalDate.now(), LocalTime.now());
        List<BasketPosition> basketPositionList = new ArrayList<>();

        DiscountCard discountCard = discountCardsService.getByNumber(order.discountCard());

        BigDecimal balanceDebitCard = order.balanceDebitCard().setScale(BIG_DECIMAL_SCALE_DEFAULT, RoundingMode.HALF_UP);

        Boolean isDiscountCardSpecified = !(order.discountCard() == null || order.discountCard().isBlank() || order.discountCard().isEmpty());

        BigDecimal totalPrice = ZERO;
        BigDecimal totalDiscount = ZERO;
        BigDecimal totalPriceWithDiscount = ZERO;

        for (Map.Entry<Integer, Integer> entry : order.productIdQuantity().entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();

            Product product = productsService.getById(productId);
            BigDecimal productPrice = product.getPrice();

            if (quantity > product.getQuantityInStock()) {
                throw new BadRequestException("Quantity is too much!");
            }

            BigDecimal productDiscount = calculateProductDiscount(productPrice, quantity, product.isWholesale(), discountCard, isDiscountCardSpecified);
            BigDecimal discountAmount = productDiscount.multiply(BigDecimal.valueOf(quantity)).setScale(BIG_DECIMAL_SCALE_DEFAULT, RoundingMode.HALF_UP);
            BigDecimal productTotalPrice = productPrice.multiply(BigDecimal.valueOf(quantity)).setScale(BIG_DECIMAL_SCALE_DEFAULT, RoundingMode.HALF_UP);

            totalDiscount = totalDiscount.add(discountAmount).setScale(BIG_DECIMAL_SCALE_DEFAULT, RoundingMode.HALF_UP);
            totalPrice = totalPrice.add(productTotalPrice).setScale(BIG_DECIMAL_SCALE_DEFAULT, RoundingMode.HALF_UP);
            totalPriceWithDiscount = totalPrice.subtract(totalDiscount).setScale(BIG_DECIMAL_SCALE_DEFAULT, RoundingMode.HALF_UP);

            if (balanceDebitCard.compareTo(totalPriceWithDiscount) < 0) {
                throw new NotEnoughMoneyException("Not enough money!");
            }

            basketPositionList.add(new BasketPosition(
                    quantity,
                    product.getDescription(),
                    productPrice,
                    discountAmount,
                    productTotalPrice));

        }

        DiscountInfo discountInfo = createDiscountInfo(order, discountCard);
        Total total = new CashierCheck.Total(totalPrice, totalDiscount, totalPriceWithDiscount);

        if (CONSOLE_ADDITIONAL_INFO) {
            BigDecimal balanceAfterOrder = balanceDebitCard.subtract(totalPriceWithDiscount);
            System.out.printf("Card balance after order: %s%n%n", balanceAfterOrder);
        }

        return new CashierCheck(dateTime, basketPositionList, discountInfo, total);
    }

    private BigDecimal calculateProductDiscount(BigDecimal price, int quantity, boolean isWholesale, DiscountCard discountCard, Boolean isDiscountCardSpecified) {
        int discountPercentage = 0;

        if (quantity >= WHOLESALE_DEFAULT_MIN_QUANTITY && isWholesale) {
            discountPercentage = WHOLESALE_DEFAULT_DISCOUNT_PERCENTAGE;
        } else if (discountCard != null) {
            discountPercentage = discountCard.getDiscountAmount();
        } else if (isDiscountCardSpecified) {
            discountPercentage = DEFAULT_DISCOUNT_PERCENTAGE;
        }

        return calcDiscount(price, discountPercentage);
    }


    private DiscountInfo createDiscountInfo(Order order, DiscountCard discountCard) {
        if (order.discountCard() != null) {
            int discountAmount = (discountCard == null) ? DEFAULT_DISCOUNT_PERCENTAGE : discountCard.getDiscountAmount();
            return new DiscountInfo(order.discountCard(), discountAmount);
        }
        return null;
    }

    private BigDecimal calcDiscount(BigDecimal price, int discountPercentage) {
        BigDecimal mathPercentage = BigDecimal.valueOf(discountPercentage).divide(HUNDRED, BIG_DECIMAL_SCALE_DEFAULT, RoundingMode.HALF_UP);
        return price.multiply(mathPercentage);
    }
}
