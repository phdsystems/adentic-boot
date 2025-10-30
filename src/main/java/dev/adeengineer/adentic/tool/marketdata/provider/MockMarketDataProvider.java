package dev.adeengineer.adentic.tool.marketdata.provider;

import dev.adeengineer.adentic.tool.marketdata.config.MarketDataConfig;
import dev.adeengineer.adentic.tool.marketdata.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mock market data provider for testing and development.
 *
 * <p>Generates synthetic OHLC data with realistic price movements.
 */
@Slf4j
public class MockMarketDataProvider extends BaseMarketDataProvider {

  private final Random random = new Random();
  private static final BigDecimal BASE_PRICE = new BigDecimal("150.00");
  private static final BigDecimal VOLATILITY = new BigDecimal("0.02"); // 2% volatility

  public MockMarketDataProvider(MarketDataConfig config) {
    super(config);
    log.info("MockMarketDataProvider initialized");
  }

  @Override
  public Mono<Void> connect() {
    return Mono.fromRunnable(
        () -> {
          setConnected(true);
          log.info("Connected to Mock provider");
        });
  }

  @Override
  public Mono<Void> disconnect() {
    return Mono.fromRunnable(
        () -> {
          setConnected(false);
          log.info("Disconnected from Mock provider");
        });
  }

  @Override
  public Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame timeFrame, int limit) {
    return Mono.fromCallable(
        () -> {
          long startTime = System.currentTimeMillis();

          validateSymbol(symbol);
          validateLimit(limit);

          logOperation("getOHLCBars", String.format("%s %s limit=%d", symbol, timeFrame, limit));

          // Generate synthetic bars
          List<OHLCBar> bars = generateSyntheticBars(symbol, timeFrame, limit);

          long executionTime = System.currentTimeMillis() - startTime;
          return createSuccessResult(bars, symbol, timeFrame, false, executionTime);
        });
  }

  @Override
  public Mono<OHLCResult> getOHLCBars(
      String symbol, TimeFrame timeFrame, Instant startTime, Instant endTime) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);

          // Calculate number of bars based on time range
          long durationSeconds = endTime.getEpochSecond() - startTime.getEpochSecond();
          int numBars = (int) (durationSeconds / timeFrame.getSeconds());
          numBars = Math.min(numBars, getConfig().getMaxBarsPerRequest());

          List<OHLCBar> bars =
              generateSyntheticBarsWithStartTime(symbol, timeFrame, numBars, startTime);

          return createSuccessResult(bars, symbol, timeFrame, false, 50);
        });
  }

  @Override
  public Mono<OHLCResult> getHistoricalData(
      String symbol, TimeFrame timeFrame, LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(
        () -> {
          Instant startTime = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
          Instant endTime = endDate.atStartOfDay(ZoneOffset.UTC).toInstant();

          return getOHLCBars(symbol, timeFrame, startTime, endTime).block();
        });
  }

  @Override
  public Mono<QuoteData> getQuote(String symbol) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);

          BigDecimal lastPrice = generatePrice(BASE_PRICE);
          BigDecimal bidPrice = lastPrice.subtract(new BigDecimal("0.05"));
          BigDecimal askPrice = lastPrice.add(new BigDecimal("0.05"));

          return QuoteData.builder()
              .symbol(symbol)
              .lastPrice(lastPrice)
              .bidPrice(bidPrice)
              .askPrice(askPrice)
              .bidSize(new BigDecimal("100"))
              .askSize(new BigDecimal("100"))
              .volume(new BigDecimal("1000000"))
              .openPrice(BASE_PRICE)
              .highPrice(lastPrice.max(BASE_PRICE))
              .lowPrice(lastPrice.min(BASE_PRICE))
              .previousClose(BASE_PRICE)
              .timestamp(Instant.now())
              .provider(getProviderName())
              .build();
        });
  }

  @Override
  public Flux<QuoteData> streamQuotes(String symbol) {
    return Flux.error(
        new UnsupportedOperationException("Mock provider does not support streaming"));
  }

  @Override
  public Mono<List<Symbol>> searchSymbols(String query) {
    return Mono.just(
        List.of(
            Symbol.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .exchange("NASDAQ")
                .marketType(MarketType.STOCKS)
                .currency("USD")
                .region("US")
                .active(true)
                .build(),
            Symbol.builder()
                .ticker("GOOGL")
                .name("Alphabet Inc.")
                .exchange("NASDAQ")
                .marketType(MarketType.STOCKS)
                .currency("USD")
                .region("US")
                .active(true)
                .build()));
  }

  @Override
  public Mono<Symbol> getSymbolInfo(String symbol) {
    return Mono.just(
        Symbol.builder()
            .ticker(symbol)
            .name(symbol + " Mock Company")
            .exchange("MOCK_EXCHANGE")
            .marketType(MarketType.STOCKS)
            .currency("USD")
            .region("US")
            .active(true)
            .description("Mock symbol for testing")
            .build());
  }

  @Override
  public String getProviderName() {
    return "Mock";
  }

  @Override
  public MarketType getSupportedMarketType() {
    return MarketType.STOCKS;
  }

  /**
   * Generate synthetic OHLC bars with realistic price movements.
   *
   * @param symbol Trading symbol
   * @param timeFrame Time frame
   * @param count Number of bars to generate
   * @return List of OHLC bars
   */
  private List<OHLCBar> generateSyntheticBars(String symbol, TimeFrame timeFrame, int count) {
    Instant now = Instant.now();
    return generateSyntheticBarsWithStartTime(symbol, timeFrame, count, now);
  }

  /**
   * Generate synthetic OHLC bars starting from a specific time.
   *
   * @param symbol Trading symbol
   * @param timeFrame Time frame
   * @param count Number of bars to generate
   * @param startTime Start time for first bar
   * @return List of OHLC bars
   */
  private List<OHLCBar> generateSyntheticBarsWithStartTime(
      String symbol, TimeFrame timeFrame, int count, Instant startTime) {
    List<OHLCBar> bars = new ArrayList<>();
    BigDecimal currentPrice = BASE_PRICE;

    for (int i = count - 1; i >= 0; i--) {
      Instant barTime = startTime.minusSeconds(i * timeFrame.getSeconds());

      BigDecimal open = currentPrice;
      BigDecimal close = generatePrice(open);
      BigDecimal high = open.max(close).add(generateRandomChange(open, 0.5));
      BigDecimal low = open.min(close).subtract(generateRandomChange(open, 0.5));

      BigDecimal volume =
          new BigDecimal(500000 + random.nextInt(1000000)).setScale(0, RoundingMode.HALF_UP);

      bars.add(
          OHLCBar.builder()
              .timestamp(barTime)
              .open(open)
              .high(high)
              .low(low)
              .close(close)
              .volume(volume)
              .symbol(symbol)
              .timeFrame(timeFrame)
              .build());

      currentPrice = close;
    }

    return bars;
  }

  /**
   * Generate a random price based on current price with volatility.
   *
   * @param currentPrice Current price
   * @return New price
   */
  private BigDecimal generatePrice(BigDecimal currentPrice) {
    double changePercent = (random.nextGaussian() * VOLATILITY.doubleValue());
    BigDecimal change = currentPrice.multiply(new BigDecimal(changePercent));
    return currentPrice.add(change).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Generate a random price change.
   *
   * @param basePrice Base price
   * @param multiplier Volatility multiplier
   * @return Random change amount
   */
  private BigDecimal generateRandomChange(BigDecimal basePrice, double multiplier) {
    double change = Math.abs(random.nextGaussian() * VOLATILITY.doubleValue() * multiplier);
    return basePrice.multiply(new BigDecimal(change)).setScale(2, RoundingMode.HALF_UP);
  }
}
