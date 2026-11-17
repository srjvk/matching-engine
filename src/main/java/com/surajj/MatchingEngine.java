package com.surajj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MatchingEngine {
    private final String symbol;
    private final OrderBook book;
    private long nextTradeId = 1;

    public MatchingEngine(String symbol) {
        this.symbol = symbol;
        this.book = new OrderBook(symbol);
    }

    public OrderBook getBook() {
        return book;
    }

    public List<Trade> submit(Order incoming) throws IllegalAccessException {
        if(!incoming.getSymbol().equals(symbol)){
            throw new IllegalArgumentException("This engine handles " + symbol + " but the order is for "+ incoming.getSymbol());
        }
        List<Trade> trades = new ArrayList<>();



        while(!incoming.isFilled()){
            Order resting = (incoming.getSide() == OrderSide.BUY) ? book.getBestAsk() : book.getBestBid();

            if(resting == null){
                break;
            }

            if(!priceCross(incoming, resting)){
                break;
            }

            int quantity = Math.min(incoming.getRemainingQuantity(), resting.getRemainingQuantity());
            BigDecimal tradePrice = resting.getPrice();

            incoming.fill(quantity);
            resting.fill(quantity);

            trades.add(createTrade(incoming, resting, tradePrice, quantity));

            if(resting.isFilled()){
                book.removeOrder(resting);
            }
        }
        if(!incoming.isFilled() && incoming.getType() == OrderType.LIMIT){
            book.addOrder(incoming);
        }

        return trades;

    }

    public boolean cancel(long orderId){
        return book.cancelOrder(orderId) != null;
    }

    private boolean priceCross(Order incoming, Order resting){
        if(incoming.getType() == OrderType.MARKET){
            return true;
        }
        int comparison = incoming.getPrice().compareTo(resting.getPrice());
        if(incoming.getSide() == OrderSide.BUY){
            return comparison >= 0;
        }
        else {
            return comparison<= 0;
        }
    }

    private Trade createTrade(Order incoming, Order resting,BigDecimal price, int quantity){
        long buyOrderId;
        long sellOrderId;
        if(incoming.getSide() == OrderSide.BUY){
            buyOrderId = incoming.getId();
            sellOrderId = resting.getId();
        } else{
            buyOrderId = resting.getId();
            sellOrderId = incoming.getId();
        }
        return new Trade(nextTradeId++, symbol, buyOrderId, sellOrderId, price, quantity);
    }
}
