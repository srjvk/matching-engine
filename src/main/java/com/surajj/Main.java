package com.surajj;



import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IllegalAccessException {
        MatchingEngine engine = new MatchingEngine("AAPL");

        engine.submit(new Order(1, "AAPL", OrderSide.SELL, OrderType.LIMIT, new BigDecimal("50.40"), 100));
        engine.submit(new Order(2, "AAPL", OrderSide.SELL, OrderType.LIMIT, new BigDecimal("50.20"), 60));
        engine.submit(new Order(3, "AAPL", OrderSide.SELL, OrderType.LIMIT, new BigDecimal("50.10"), 20));
        engine.submit(new Order(4, "AAPL", OrderSide.BUY,  OrderType.LIMIT, new BigDecimal("50.00"), 30));
        engine.submit(new Order(5, "AAPL", OrderSide.BUY,  OrderType.LIMIT, new BigDecimal("49.90"), 80));
        engine.getBook().printBook();

        System.out.println("--- MARKET BUY 60 ---");
        Order marketBuy = new Order(6, "AAPL", OrderSide.BUY, OrderType.MARKET, null, 60);
        List<Trade> trades = engine.submit(marketBuy);
        for (Trade trade : trades) {
            System.out.println(trade);
        }
        System.out.println("Unfilled: " + marketBuy.getRemainingQuantity());
        engine.getBook().printBook();

        System.out.println("--- CANCEL order #5 ---");
        System.out.println("Cancelled? " + engine.cancel(5));
        System.out.println("Cancel again? " + engine.cancel(5));
        engine.getBook().printBook();
    }
}