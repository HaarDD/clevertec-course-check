package main.java.ru.clevertec.check;

import main.java.ru.clevertec.check.CSVWriter.CSVWriterNote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CashierCheck {

    @CSVWriterNote(type = CSVWriter.CSVWriterFieldTypes.CELL_CONTAINER)
    private final DateTime dateTime;

    @CSVWriterNote(type = CSVWriter.CSVWriterFieldTypes.CELL_CONTAINER)
    private final List<BasketPosition> basketPositionList;

    @CSVWriterNote(type = CSVWriter.CSVWriterFieldTypes.CELL_CONTAINER)
    private final DiscountInfo discountInfo;

    @CSVWriterNote(type = CSVWriter.CSVWriterFieldTypes.CELL_CONTAINER)
    private final Total total;

    public CashierCheck(DateTime dateTime, List<BasketPosition> basketPositionList, DiscountInfo discountInfo, Total total) {
        this.dateTime = dateTime;
        this.basketPositionList = basketPositionList;
        this.discountInfo = discountInfo;
        this.total = total;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public List<BasketPosition> getBasketPositionList() {
        return basketPositionList;
    }

    public DiscountInfo getDiscountInfo() {
        return discountInfo;
    }

    public Total getTotal() {
        return total;
    }

    public record DateTime(@CSVWriterNote(header = "Date") LocalDate date,
                           @CSVWriterNote(header = "Time", format = "HH:mm:ss") LocalTime time) {
    }

    public record BasketPosition(@CSVWriterNote(header = "QTY") Integer quantity,
                                 @CSVWriterNote(header = "DESCRIPTION") String description,
                                 @CSVWriterNote(header = "PRICE", unit = "$", format = "#,##0.00") BigDecimal price,
                                 @CSVWriterNote(header = "DISCOUNT", unit = "$", format = "#,##0.00") BigDecimal discount,
                                 @CSVWriterNote(header = "TOTAL", unit = "$", format = "#,##0.00") BigDecimal total) {
    }

    public record DiscountInfo(@CSVWriterNote(header = "DISCOUNT CARD") String discountNumber,
                               @CSVWriterNote(header = "DISCOUNT PERCENTAGE", unit = "%") Integer discountPercentage) {
    }

    public record Total(@CSVWriterNote(header = "TOTAL PRICE", unit = "$", format = "#,##0.00") BigDecimal totalPrice,
                        @CSVWriterNote(header = "TOTAL DISCOUNT", unit = "$", format = "#,##0.00") BigDecimal totalDiscount,
                        @CSVWriterNote(header = "TOTAL WITH DISCOUNT", unit = "$", format = "#,##0.00") BigDecimal totalPriceWithDiscount) {
    }

    @Override
    public String toString() {
        return "CashierCheck{" +
                "dateTime=" + dateTime +
                ", basketPositionList=" + basketPositionList +
                ", discountInfo=" + discountInfo +
                ", total=" + total +
                '}';
    }
}
