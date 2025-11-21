package dev.engineeringlab.adentic.tool.webtest.model;

/** Supported browser types. */
public enum BrowserType {
  /** Chromium-based browser (Chrome, Edge). */
  CHROMIUM,

  /** Mozilla Firefox. */
  FIREFOX,

  /** WebKit (Safari engine). */
  WEBKIT,

  /** Google Chrome. */
  CHROME,

  /** Microsoft Edge. */
  EDGE,

  /** No browser (for HtmlUnit). */
  NONE
}
