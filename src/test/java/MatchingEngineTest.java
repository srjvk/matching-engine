

import com.surajj.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    private MatchingEngine engine;
    private long nextOrderId;

    // Runs before EVERY test, so each test starts from the same book:
    // ASKS: 50.40 (100), 50.20 (60), 50.10 (20)
    // BIDS: 50.00 (30), 49.90 (80)
    @BeforeEach
    void setUp() throws IllegalAccessException {
        engine = new MatchingEngine("AAPL");
        nextOrderId = 1;
        engine.submit(limit(OrderSide.SELL, "50.40", 100));
        engine.submit(limit(OrderSide.SELL, "50.20", 60));
        engine.submit(limit(OrderSide.SELL, "50.10", 20));
        engine.submit(limit(OrderSide.BUY,  "50.00", 30));
        engine.submit(limit(OrderSide.BUY,  "49.90", 80));
    }

    private Order limit(OrderSide side, String price, int quantity) {
        return new Order(nextOrderId++, "AAPL", side, OrderType.LIMIT, new BigDecimal(price), quantity);
    }

    private void assertTrade(Trade trade, int expectedQuantity, String expectedPrice) {
        assertEquals(expectedQuantity, trade.getQuantity());
        assertEquals(0, new BigDecimal(expectedPrice).compareTo(trade.getPrice()));
    }


    @Test
    void buyOrderWalksThroughTwoAskLevels() throws IllegalAccessException {
        Order buy = limit(OrderSide.BUY, "50.20", 70);
        List<Trade> trades = engine.submit(buy);

        assertEquals(2, trades.size());
        assertTrade(trades.get(0), 20, "50.10");
        assertTrade(trades.get(1), 50, "50.20");
        assertTrue(buy.isFilled());

        Order bestAsk = engine.getBook().getBestAsk();
        assertEquals(0, new BigDecimal("50.20").compareTo(bestAsk.getPrice()));
        assertEquals(10, bestAsk.getRemainingQuantity());
        assertEquals(2, engine.getBook().askCount());
        assertEquals(2, engine.getBook().bidCount());
    }


    @Test
    void earlierOrderAtSamePriceIsFilledFirst() throws IllegalAccessException {
        MatchingEngine freshEngine = new MatchingEngine("AAPL");
        Order x = new Order(1, "AAPL", OrderSide.SELL, OrderType.LIMIT, new BigDecimal("50.10"), 12);
        Order y = new Order(2, "AAPL", OrderSide.SELL, OrderType.LIMIT, new BigDecimal("50.10"), 8);
        freshEngine.submit(x);
        freshEngine.submit(y);

        List<Trade> trades = freshEngine.submit(
                new Order(3, "AAPL", OrderSide.BUY, OrderType.LIMIT, new BigDecimal("50.10"), 15));

        assertEquals(2, trades.size());
        assertEquals(1, trades.get(0).getSellOrderId());
        assertEquals(12, trades.get(0).getQuantity());
        assertEquals(2, trades.get(1).getSellOrderId());
        assertEquals(3, trades.get(1).getQuantity());
        assertEquals(5, y.getRemainingQuantity());
    }

    @Test
    void nonCrossingOrderRestsInTheBook() throws IllegalAccessException {
        Order sell = limit(OrderSide.SELL, "50.05", 40);
        List<Trade> trades = engine.submit(sell);

        assertTrue(trades.isEmpty());
        assertEquals(4, engine.getBook().askCount());
        assertSame(sell, engine.getBook().getBestAsk());   // new best ask
    }


    @Test
    void tradeUsesRestingOrderPriceNotIncomingPrice() throws IllegalAccessException {
        List<Trade> trades = engine.submit(limit(OrderSide.BUY, "51.00", 10));

        assertEquals(1, trades.size());
        assertTrade(trades.get(0), 10, "50.10");   // not 51.00
        assertEquals(10, engine.getBook().getBestAsk().getRemainingQuantity());
    }

    private Order market(OrderSide side, int quantity) {
        return new Order(nextOrderId++, "AAPL", side, OrderType.MARKET, null, quantity);
    }

    // Walks two price levels, paying a worse price for the second part (slippage)
    @Test
    void marketBuyWalksTheBook() throws IllegalAccessException {
        Order buy = market(OrderSide.BUY, 60);
        List<Trade> trades = engine.submit(buy);

        assertEquals(2, trades.size());
        assertTrade(trades.get(0), 20, "50.10");
        assertTrade(trades.get(1), 40, "50.20");
        assertTrue(buy.isFilled());
        assertEquals(20, engine.getBook().getBestAsk().getRemainingQuantity());
    }

    // The book has only 180 shares for sale. The other 320 are discarded, never rested.
    @Test
    void marketOrderNeverRestsInTheBook() throws IllegalAccessException {
        Order buy = market(OrderSide.BUY, 500);
        List<Trade> trades = engine.submit(buy);

        assertEquals(3, trades.size());
        assertEquals(320, buy.getRemainingQuantity());
        assertEquals(0, engine.getBook().askCount());
        assertEquals(2, engine.getBook().bidCount());      // unchanged: no new bid appeared
    }

    @Test
    void marketOrderOnEmptyBookTradesNothing() throws IllegalAccessException {
        MatchingEngine empty = new MatchingEngine("AAPL");
        Order buy = market(OrderSide.BUY, 10);

        assertTrue(empty.submit(buy).isEmpty());
        assertEquals(10, buy.getRemainingQuantity());
        assertEquals(0, empty.getBook().bidCount());
    }

    @Test
    void cancelRemovesRestingOrder() {
        assertTrue(engine.cancel(3));                       // order 3 = SELL 20 @ 50.10, the best ask

        assertEquals(2, engine.getBook().askCount());
        assertEquals(0, new BigDecimal("50.20").compareTo(engine.getBook().getBestAsk().getPrice()));
    }

    @Test
    void cancellingAnUnknownOrderReturnsFalse() {
        assertFalse(engine.cancel(999));
    }

    // A cancelled order must not match anything afterward
    @Test
    void cancelledOrderCannotBeMatched() throws IllegalAccessException {
        engine.cancel(3);

        List<Trade> trades = engine.submit(limit(OrderSide.BUY, "50.10", 20));

        assertTrue(trades.isEmpty());                       // the best ask is now 50.20, too high
        assertEquals(3, engine.getBook().bidCount());       // so the buy rests as a new bid
    }

    // Cancelling keeps fills that already happened; only the remainder leaves
    @Test
    void cancellingAPartiallyFilledOrderKeepsItsFills() throws IllegalAccessException {
        engine.submit(limit(OrderSide.BUY, "50.10", 10));   // fills 10 of order 3's 20

        assertTrue(engine.cancel(3));

        assertEquals(2, engine.getBook().askCount());
        assertEquals(0, new BigDecimal("50.20").compareTo(engine.getBook().getBestAsk().getPrice()));
    }

    @Test
    void cannotCancelAnOrderThatIsAlreadyFilled() throws IllegalAccessException {
        engine.submit(limit(OrderSide.BUY, "50.10", 20));   // completely fills order 3

        assertFalse(engine.cancel(3));
    }


}