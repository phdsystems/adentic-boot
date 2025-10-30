package dev.adeengineer.adentic.tool.marketdata.provider;

import dev.adeengineer.adentic.tool.marketdata.config.MarketDataConfig;
import dev.adeengineer.adentic.tool.marketdata.model.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Yahoo Finance market data provider (stub implementation).
 *
 * <p>Yahoo Finance provides stock, ETF, and index data.
 *
 * <p>Free tier: Best effort (no official rate limits)
 *
 * <p>TODO: Implement HTTP client for Yahoo Finance API
 *
 * @see <a href="https://finance.yahoo.com/">Yahoo Finance</a>
 */
@Slf4j
public class YahooFinanceProvider extends BaseMarketDataProvider {

  private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";

  public YahooFinanceProvider(MarketDataConfig config) {
    super(config);
    log.info("YahooFinanceProvider initialized (stub)");
  }

  @Override
  public Mono<Void> connect() {
    return Mono.fromRunnable(
        () -> {
          setConnected(true);
          log.info("Connected to Yahoo Finance (stub)");
        });
  }

  @Override
  public Mono<Void> disconnect() {
    return Mono.fromRunnable(
        () -> {
          setConnected(false);
          log.info("Disconnected from Yahoo Finance");
        });
  }

  @Override
  public Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame timeFrame, int limit) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);
          validateLimit(limit);

          log.warn("Yahoo Finance provider is a stub. Returning empty result.");
          log.info(
              "To implement: HTTP GET {}{}?interval={}&range={}d",
              BASE_URL,
              symbol,
              timeFrame.getStandardFormat(),
              limit);

          return createSuccessResult(List.of(), symbol, timeFrame, false, 0);
        });
  }

  @Override
  public Mono<OHLCResult> getOHLCBars(
      String symbol, TimeFrame timeFrame, Instant startTime, Instant endTime) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);
          log.warn("Yahoo Finance provider is a stub. Returning empty result.");
          return createSuccessResult(List.of(), symbol, timeFrame, false, 0);
        });
  }

  @Override
  public Mono<OHLCResult> getHistoricalData(
      String symbol, TimeFrame timeFrame, LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);
          log.warn("Yahoo Finance provider is a stub. Returning empty result.");
          return createSuccessResult(List.of(), symbol, timeFrame, false, 0);
        });
  }

  @Override
  public Mono<QuoteData> getQuote(String symbol) {
    return Mono.error(new UnsupportedOperationException("Yahoo Finance provider is a stub"));
  }

  @Override
  public Flux<QuoteData> streamQuotes(String symbol) {
    return Flux.error(
        new UnsupportedOperationException("Yahoo Finance does not support streaming"));
  }

  @Override
  public Mono<List<Symbol>> searchSymbols(String query) {
    return Mono.fromCallable(
        () -> {
          log.warn("Yahoo Finance provider is a stub. Returning empty list.");
          return List.of();
        });
  }

  @Override
  public Mono<Symbol> getSymbolInfo(String symbol) {
    return Mono.error(new UnsupportedOperationException("Yahoo Finance provider is a stub"));
  }

  @Override
  public String getProviderName() {
    return "Yahoo Finance";
  }

  @Override
  public MarketType getSupportedMarketType() {
    return MarketType.STOCKS;
  }

  @Override
  public boolean supportsTimeFrame(TimeFrame timeFrame) {
    // Yahoo Finance primarily supports daily and higher time frames
    return timeFrame == TimeFrame.D1 || timeFrame == TimeFrame.W1 || timeFrame == TimeFrame.MN1;
  }
}
