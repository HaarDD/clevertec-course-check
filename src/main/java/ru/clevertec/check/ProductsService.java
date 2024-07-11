package main.java.ru.clevertec.check;

import main.java.ru.clevertec.check.CSVReader.CSVReaderNote;
import main.java.ru.clevertec.check.CustomExceptions.BadRequestException;
import main.java.ru.clevertec.check.CustomExceptions.InternalServerErrorException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class ProductsService {

    private static final String PATH_PRODUCTS = "./src/main/resources/products.csv";

    private static final Map<Integer, Product> productMap;

    static {
        try {
            productMap = CSVReader.readCSVtoMap(PATH_PRODUCTS, Product.class);
        } catch (IOException e) {
            throw new InternalServerErrorException("Unable to read file by path: " + PATH_PRODUCTS);
        }
    }

    public Product getById(Integer id) {
        Product product = productMap.get(id);
        if (product == null) throw new BadRequestException("Product with id: " + id + " is not exist!");
        return product;
    }

    public static class Product implements IdentifiableObject {
        @CSVReaderNote(notEmpty = true)
        private final Integer id;

        @CSVReaderNote(notEmpty = true)
        private final String description;

        @CSVReaderNote(notEmpty = true, positive = true)
        private final BigDecimal price;

        @CSVReaderNote(notEmpty = true, positive = true)
        private final Integer quantityInStock;

        @CSVReaderNote(trueValue = "+")
        private final Boolean isWholesale;

        public Product(Integer id, String description, BigDecimal price, Integer quantityInStock, Boolean isWholesale) {
            this.id = id;
            this.description = description;
            this.price = price;
            this.quantityInStock = quantityInStock;
            this.isWholesale = isWholesale;
        }

        @Override
        public Integer getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public Integer getQuantityInStock() {
            return quantityInStock;
        }

        public Boolean isWholesale() {
            return isWholesale;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id=" + id +
                    ", description='" + description + '\'' +
                    ", price=" + price +
                    ", quantity=" + quantityInStock +
                    ", isWholesale=" + isWholesale +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Product product = (Product) o;
            return Objects.equals(id, product.id) && Objects.equals(description, product.description) && Objects.equals(price, product.price) && Objects.equals(quantityInStock, product.quantityInStock) && Objects.equals(isWholesale, product.isWholesale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, description, price, quantityInStock, isWholesale);
        }
    }

}
