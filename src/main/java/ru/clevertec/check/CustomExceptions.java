package main.java.ru.clevertec.check;

public class CustomExceptions {

    public static class BadRequestException extends RuntimeException {

        @CSVWriter.CSVWriterNote(header = "ERROR")
        private static final String EXCEPTION_TITLE_BAD_REQUEST = "BAD REQUEST";

        public BadRequestException(String message) {
            super(EXCEPTION_TITLE_BAD_REQUEST + ": " + message);
            System.out.println(message);
        }

    }

    public static class NotEnoughMoneyException extends RuntimeException {

        @CSVWriter.CSVWriterNote(header = "ERROR")
        private static final String EXCEPTION_TITLE_NOT_ENOUGH_MONEY = "NOT ENOUGH MONEY";

        public NotEnoughMoneyException(String message) {
            super(EXCEPTION_TITLE_NOT_ENOUGH_MONEY + ": " + message);
            System.out.println(message);
        }

    }

    public static class InternalServerErrorException extends RuntimeException {

        @CSVWriter.CSVWriterNote(header = "ERROR")
        private static final String EXCEPTION_TITLE_INTERNAL_SERVER_ERROR = "INTERNAL SERVER ERROR";

        public InternalServerErrorException(String message) {
            super(EXCEPTION_TITLE_INTERNAL_SERVER_ERROR + ": " + message);
            System.out.println(message);
        }

    }

}