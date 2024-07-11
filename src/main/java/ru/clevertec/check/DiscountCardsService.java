package main.java.ru.clevertec.check;

import main.java.ru.clevertec.check.CSVReader.CSVReaderNote;
import main.java.ru.clevertec.check.CustomExceptions.InternalServerErrorException;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class DiscountCardsService {

    private static final String PATH_DISCOUNT_CARDS = "./src/main/resources/discountCards.csv";

    private static final Map<Integer, DiscountCard> discountCardMap;

    static {
        try {
            discountCardMap = CSVReader.readCSVtoMap(PATH_DISCOUNT_CARDS, DiscountCard.class);
        } catch (IOException e) {
            throw new InternalServerErrorException("Unable to read file by path: " + PATH_DISCOUNT_CARDS);
        }
    }

    public DiscountCard getByNumber(String number) {
        for (DiscountCard card : discountCardMap.values()) {
            if (card.getNumber().equals(number)) {
                return card;
            }
        }
        return null;
    }

    public static class DiscountCard implements IdentifiableObject {

        @CSVReaderNote(notEmpty = true)
        private final Integer id;

        @CSVReaderNote(notEmpty = true)
        private final String number;

        @CSVReaderNote(notEmpty = true, positive = true)
        private final Integer discountAmount;

        public DiscountCard(Integer id, String number, Integer discountAmount) {
            this.id = id;
            this.number = number;
            this.discountAmount = discountAmount;
        }

        public Integer getId() {
            return id;
        }

        public String getNumber() {
            return number;
        }

        public Integer getDiscountAmount() {
            return discountAmount;
        }

        @Override
        public String toString() {
            return "DiscountCard{" +
                    "id=" + id +
                    ", number='" + number + '\'' +
                    ", discountAmount=" + discountAmount +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiscountCard that = (DiscountCard) o;
            return Objects.equals(id, that.id) && Objects.equals(number, that.number) && Objects.equals(discountAmount, that.discountAmount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, number, discountAmount);
        }
    }
}
