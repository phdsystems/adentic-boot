/** AgenticBoot Module - Lightweight framework for building agentic systems. */
module dev.engineeringlab.adentic.boot {
  // ==================== Boot Framework ====================
  exports dev.engineeringlab.adentic.boot;
  exports dev.engineeringlab.adentic.boot.annotations;
  exports dev.engineeringlab.adentic.boot.context;
  exports dev.engineeringlab.adentic.boot.event;
  exports dev.engineeringlab.adentic.boot.exception;
  exports dev.engineeringlab.adentic.boot.registry;
  exports dev.engineeringlab.adentic.boot.scanner;
  exports dev.engineeringlab.adentic.boot.web;
  exports dev.engineeringlab.adentic.boot.web.annotations;

  // ==================== Tools ====================
  exports dev.engineeringlab.adentic.tool.calculator;
  exports dev.engineeringlab.adentic.tool.calculator.config;
  exports dev.engineeringlab.adentic.tool.calculator.model;
  exports dev.engineeringlab.adentic.tool.database;
  exports dev.engineeringlab.adentic.tool.database.annotation;
  exports dev.engineeringlab.adentic.tool.database.config;
  exports dev.engineeringlab.adentic.tool.database.model;
  exports dev.engineeringlab.adentic.tool.database.provider;
  exports dev.engineeringlab.adentic.tool.datetime;
  exports dev.engineeringlab.adentic.tool.datetime.config;
  exports dev.engineeringlab.adentic.tool.datetime.model;
  exports dev.engineeringlab.adentic.tool.email;
  exports dev.engineeringlab.adentic.tool.email.annotation;
  exports dev.engineeringlab.adentic.tool.email.config;
  exports dev.engineeringlab.adentic.tool.email.model;
  exports dev.engineeringlab.adentic.tool.filesystem;
  exports dev.engineeringlab.adentic.tool.filesystem.config;
  exports dev.engineeringlab.adentic.tool.filesystem.model;
  exports dev.engineeringlab.adentic.tool.filesystem.security;
  exports dev.engineeringlab.adentic.tool.http;
  exports dev.engineeringlab.adentic.tool.http.config;
  exports dev.engineeringlab.adentic.tool.http.model;
  exports dev.engineeringlab.adentic.tool.marketdata;
  exports dev.engineeringlab.adentic.tool.marketdata.config;
  exports dev.engineeringlab.adentic.tool.marketdata.model;
  exports dev.engineeringlab.adentic.tool.marketdata.provider;
  exports dev.engineeringlab.adentic.tool.websearch;
  exports dev.engineeringlab.adentic.tool.websearch.annotation;
  exports dev.engineeringlab.adentic.tool.websearch.config;
  exports dev.engineeringlab.adentic.tool.websearch.model;
  exports dev.engineeringlab.adentic.tool.websearch.provider;
  exports dev.engineeringlab.adentic.tool.webtest;
  exports dev.engineeringlab.adentic.tool.webtest.annotation;
  exports dev.engineeringlab.adentic.tool.webtest.config;
  exports dev.engineeringlab.adentic.tool.webtest.model;
  exports dev.engineeringlab.adentic.tool.webtest.provider;

  // ==================== EE (Enterprise Edition) ====================
  exports dev.engineeringlab.ee.llm.tools;

  // ==================== RAG ====================
  exports dev.engineeringlab.rag.embedding;
  exports dev.engineeringlab.rag.embedding.valueobject;

  // ==================== LLM Module Dependencies ====================
  requires transitive dev.engineeringlab.llm;
  requires transitive dev.engineeringlab.llm.api;
  requires transitive dev.engineeringlab.llm.spi;
  requires dev.engineeringlab.llm.core;
  requires dev.engineeringlab.llm.common;

  // ==================== Java Platform Modules ====================
  requires java.sql;
  requires java.net.http;

  // ==================== Third-Party Dependencies ====================
  requires io.javalin;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires org.slf4j;
  requires reactor.core;
  requires static lombok;
}
