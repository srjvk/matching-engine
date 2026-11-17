package com.surajj;

import java.math.BigDecimal;

public class Order {
    private final long id;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final BigDecimal price;
    private final int originalQuantity;
    private int remainingQuantity;

    public Order(long id, String symbol, OrderSide side, OrderType type, BigDecimal price, int quantity) {

        if(symbol == null || symbol.isBlank()){
            throw new IllegalArgumentException("Symbol is required");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if(type == OrderType.LIMIT){
            if(price == null || price.compareTo(BigDecimal.ZERO) <= 0){
                throw new IllegalArgumentException("A LIMIT order needs a positive price");
            }
        }
        if(type == OrderType.MARKET && price !=null){
            throw new IllegalArgumentException("A MARKET order must not have a price");
        }



        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
    }

    public boolean isFilled(){
        return remainingQuantity == 0;
    }

    public void fill(int quantity) throws IllegalAccessException {
        if(quantity <= 0){
            throw new IllegalAccessException("Fill quantity must be positive");
        }
        if (quantity > remainingQuantity){
            throw new IllegalArgumentException("Cannot fill more than the remaining quantity");
        }
        remainingQuantity -= quantity;
    }

    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }


    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    @Override
    public String toString() {
        String priceText = (type == OrderType.MARKET) ? "MARKET" : price.toPlainString();

        return "Order{" +
                "id=" + id +
                ", side=" + side + " " +
                ", originalQuantity=" + originalQuantity + "/" +
                ", remainingQuantity=" + remainingQuantity + " " +
                symbol + " @ " + priceText +
                '}';
    }
}
