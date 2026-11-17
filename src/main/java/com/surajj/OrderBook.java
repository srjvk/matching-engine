package com.surajj;

import java.util.ArrayList;
import java.util.List;

public class OrderBook {
    private final String symbol;
    private final List<Order> bids = new ArrayList<>();
    private final List<Order> asks = new ArrayList<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public void addOrder(Order order){
        if (!order.getSymbol().equals(symbol)){
            throw new IllegalArgumentException("Order is for " + order.getSymbol() + " but this book is for " + symbol);
        }
        if(order.getType() != OrderType.LIMIT){
            throw new IllegalArgumentException("Only LIMIT orders can rest in the book");
        }
        if(order.isFilled()){
            throw new IllegalArgumentException("Only LIMIT orders can rest in the book");
        }

        if(order.getSide() == OrderSide.BUY){
            bids.add(order);
        } else {
            asks.add(order);
        }
    }

    public boolean removeOrder(Order order){
        if(order.getSide() == OrderSide.BUY){
            return bids.remove(order);
        } else {
            return asks.remove(order);
        }
    }
    public Order getBestBid(){
        return findBest(bids, true);
    }

    public Order getBestAsk(){
        return findBest(asks, false);
    }

    public int bidCount(){
        return bids.size();
    }
    public int askCount(){
        return asks.size();
    }

    private Order findBest(List<Order> orders, boolean highestPriceWins){
        Order best = null;

        for (Order candidate: orders){
            if(best == null){
                best = candidate;
            } else {
                int comparison = candidate.getPrice().compareTo(best.getPrice());
                boolean candidateIsBetter = highestPriceWins ? comparison > 0 : comparison < 0;
                if(candidateIsBetter){
                    best = candidate;
                }
            }
        }
        return best;
    }

    public void printBook(){
        System.out.println("=== " + symbol + " ORDER BOOK ===");
        System.out.println("ASKS (best first):");
        printSide(asks, false);
        System.out.println("BIDS (best first):");
        printSide(bids, true);
    }

    private void printSide(List<Order> orders, boolean highestPriceWins) {
        List<Order> copy = new ArrayList<>(orders);
        while (!copy.isEmpty()) {
            Order best = findBest(copy, highestPriceWins);
            System.out.println("  " + best);
            copy.remove(best);
        }
    }

    public Order cancelOrder(long orderId){
        Order removed = removeById(bids, orderId);
        if(removed != null){
            return removed;
        }
        return removeById(asks, orderId);
    }

    private Order removeById(List<Order> orders, long orderId){
        for(int i = 0; i < orders.size(); i++){
            if(orders.get(i).getId() == orderId){
                return orders.remove(i);
            }
        }
        return null;
    }


}
