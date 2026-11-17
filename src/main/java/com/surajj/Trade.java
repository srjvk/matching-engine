package com.surajj;

import java.math.BigDecimal;

public class Trade {

    private final long id;
    private final String symbol;
    private final long buyOrderID;
    private final long sellOrderId;
    private final BigDecimal price;
    private final int quantity;

    public Trade(long id, String symbol, long buyOrderID, long sellOrderId, BigDecimal price, int quantity) {

        if(symbol == null || symbol.isBlank()){
            throw new IllegalArgumentException("Symbol is required");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.id = id;
        this.symbol = symbol;
        this.buyOrderID = buyOrderID;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
    }


    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getBuyOrderID() {
        return buyOrderID;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", buyOrderID=" + buyOrderID +
                ", sellOrderId=" + sellOrderId +
                ", price=" + price.toPlainString() +
                ", quantity=" + quantity +
                '}';
    }
}
