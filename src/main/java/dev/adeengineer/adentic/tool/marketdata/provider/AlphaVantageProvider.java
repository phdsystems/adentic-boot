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
 * Alpha Vantage market data provider (stub implementation).
 *
 * <p>Alpha Vantage provides stock, forex, and cryptocurrency data via REST API.
 *
 * <p>Free tier: 5 requests/minute, 500 requests/day
 *
 * <p>TODO: Implement HTTP client for Alpha Vantage API
 *
 * @see <a href="https://www.alphavantage.co/documentation/">Alpha Vantage API Documentation</a>
 */
@Slf4j
public class AlphaVantageProvider extends BaseMarketDataProvider {

  private static final String BASE_URL = "https://www.alphavantage.co/query";

  public AlphaVantageProvider(MarketDataConfig config) {
    super(config);
    log.info("AlphaVantageProvider initialized (stub)");
    if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
      log.warn(
          "Alpha Vantage API key not configured. Get a free key at https://www.alphavantage.co/support/#api-key");
    }
  }

  @Override
  public Mono<Void> connect() {
    return Mono.fromRunnable(
        () -> {
          setConnected(true);
          log.info("Connected to Alpha Vantage (stub)");
        });
  }

  @Override
  public Mono<Void> disconnect() {
    return Mono.fromRunnable(
        () -> {
          setConnected(false);
          log.info("Disconnected from Alpha Vantage");
        });
  }

  @Override
  public Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame timeFrame, int limit) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);
          validateLimit(limit);

          log.warn("Alpha Vantage provider is a stub. Returning empty result.");
          log.info(
              "To implement: HTTP GET {}?function=TIME_SERIES_INTRADAY&symbol={}&interval={}&apikey={}",
              BASE_URL,
              symbol,
              timeFrame.getAlphaVantageFormat(),
              getApiKey());

          return createSuccessResult(List.of(), symbol, timeFrame, false, 0);
        });
  }

  @Override
  public Mono<OHLCResult> getOHLCBars(
      String symbol, TimeFrame timeFrame, Instant startTime, Instant endTime) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);
          log.warn("Alpha Vantage provider is a stub. Returning empty result.");
          return createSuccessResult(List.of(), symbol, timeFrame, false, 0);
        });
  }

  @Override
  public Mono<OHLCResult> getHistoricalData(
      String symbol, TimeFrame timeFrame, LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(
        () -> {
          validateSymbol(symbol);
          log.warn("Alpha Vantage provider is a stub. Returning empty result.");
          return createSuccessResult(List.of(), symbol, timeFrame, false, 0);
        });
  }

  @Override
  public Mono<QuoteData> getQuote(String symbol) {
    return Mono.error(new UnsupportedOperationException("Alpha Vantage provider is a stub"));
  }

  @Override
  public Flux<QuoteData> streamQuotes(String symbol) {
    return Flux.error(
        new UnsupportedOperationException("Alpha Vantage does not support streaming"));
  }

  @Override
  public Mono<List<Symbol>> searchSymbols(String query) {
    return Mono.fromCallable(
        () -> {
          log.warn("Alpha Vantage provider is a stub. Returning empty list.");
          log.info(
              "To implement: HTTP GET {}?function=SYMBOL_SEARCH&keywords={}&apikey={}",
              BASE_URL,
              query,
              getApiKey());
          return List.of();
        });
  }

  @Override
  public Mono<Symbol> getSymbolInfo(String symbol) {
    return Mono.error(new UnsupportedOperationException("Alpha Vantage provider is a stub"));
  }

  @Override
  public String getProviderName() {
    return "Alpha Vantage";
  }

  @Override
  public MarketType getSupportedMarketType() {
    return MarketType.STOCKS;
  }

  @Override
  public boolean supportsTimeFrame(TimeFrame timeFrame) {
    // Alpha Vantage supports all standard time frames
    return true;
  }
}
