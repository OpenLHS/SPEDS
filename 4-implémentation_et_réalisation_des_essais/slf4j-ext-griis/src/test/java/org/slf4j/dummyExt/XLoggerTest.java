/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.slf4j.dummyExt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class XLoggerTest {

  ListAppender listAppender;
  Logger logbackRoot;

  final static String EXPECTED_FILE_NAME = "XLoggerTest";

  @BeforeEach
  public void setUp() throws Exception {

    // start from a clean slate for each test

    listAppender = new ListAppender();
    listAppender.extractLocationInfo = true;
    logbackRoot = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    logbackRoot.addAppender(listAppender);
    listAppender.start();
    logbackRoot.setLevel(Level.TRACE);
  }

  void verify(ILoggingEvent le, String expectedMsg) {
    assertEquals(expectedMsg, le.getMessage());
    assertEquals(EXPECTED_FILE_NAME, le.getLoggerName());
  }

  void verifyWithException(ILoggingEvent le, String expectedMsg, Throwable t) {
    verify(le, expectedMsg);
    assertEquals(t.toString(),
        le.getThrowableProxy().getClassName() + ": " + le.getThrowableProxy().getMessage());
  }

  void verifyWithLevelAndException(ILoggingEvent le, XLogger.Level level, String expectedMsg,
      Throwable t) {
    verify(le, expectedMsg);
    assertEquals(t.toString(),
        le.getThrowableProxy().getClassName() + ": " + le.getThrowableProxy().getMessage());
    assertEquals(le.getLevel().toString(), level.toString());
  }

  @Test
  public void testEntering() {
    XLogger logger = XLoggerFactory.getXLogger(this.getClass().getSimpleName());
    logger.entry();
    logger.entry(1);
    logger.entry("test");
    logger.entry("a", "b", "c", "d");
    logger.entry("a", "b", "c", "d", "e");
    logger.entry("a", "b", "c", "d", "e", "f");

    assertEquals(6, listAppender.list.size());
    verify(listAppender.list.get(0), "entry");
    verify(listAppender.list.get(1), "entry with (1)");
    verify(listAppender.list.get(2), "entry with (test)");
  }

  @Test
  public void testExiting() {
    XLogger logger = XLoggerFactory.getXLogger(this.getClass().getSimpleName());
    logger.exit();
    assertEquals(Integer.valueOf(0), logger.exit(0));
    assertEquals(Boolean.FALSE, logger.exit(false));

    assertEquals(3, listAppender.list.size());
    verify(listAppender.list.get(0), "exit");
    verify(listAppender.list.get(1), "exit with (0)");
    verify(listAppender.list.get(2), "exit with (false)");
  }

  @Test
  public void testThrowing() {
    XLogger logger = XLoggerFactory.getXLogger(this.getClass().getSimpleName());
    Throwable t = new UnsupportedOperationException("Test");
    assertEquals(t, logger.throwing(t));
    assertEquals(t, logger.throwing(XLogger.Level.DEBUG, t));
    assertEquals(2, listAppender.list.size());
    verifyWithException(listAppender.list.get(0), "throwing", t);
    ILoggingEvent event = listAppender.list.get(1);
    verifyWithLevelAndException(event, XLogger.Level.DEBUG, "throwing", t);
  }

  @Test
  public void testCaught() {
    XLogger logger = XLoggerFactory.getXLogger(this.getClass().getSimpleName());
    long x = 5;
    Throwable t = null;
    try {
      @SuppressWarnings("unused")
      long y = x / 0;
    } catch (Exception ex) {
      t = ex;
      logger.catching(ex);
      logger.catching(XLogger.Level.DEBUG, ex);
    }
    verifyWithException(listAppender.list.get(0), "catching", t);
    verifyWithLevelAndException(listAppender.list.get(1), XLogger.Level.DEBUG,
        "catching", t);
  }

  // See http://jira.qos.ch/browse/SLF4J-105
  // formerly http://bugzilla.slf4j.org/show_bug.cgi?id=114
  @Test
  public void testLocationExtraction_Bug114() {
    final String methodName = new Object() {}
        .getClass()
        .getEnclosingMethod()
        .getName();
    XLogger logger = XLoggerFactory.getXLogger(this.getClass().getSimpleName());
    int line = 146; // requires update if line numbers change
    logger.exit();
    logger.debug("hello");

    assertEquals(2, listAppender.list.size());

    {
      ILoggingEvent e = listAppender.list.get(0);
      assertEquals(this.getClass().getSimpleName(), e.getLoggerName());
      final int stackTraceIndex = findStackTraceElementForMethod(methodName, e.getCallerData());
      assertEquals(line, e.getCallerData()[stackTraceIndex].getLineNumber());
    }

    {
      ILoggingEvent e = listAppender.list.get(1);
      assertEquals(this.getClass().getSimpleName(), e.getLoggerName());
      final int stackTraceIndex = findStackTraceElementForMethod(methodName, e.getCallerData());
      assertEquals(line + 1, e.getCallerData()[stackTraceIndex].getLineNumber());
    }

  }

  private int findStackTraceElementForMethod(String methodName, StackTraceElement[] st) {
    int result = -1;
    for (int i = 0; i < st.length; i++) {
      if (st[i].getMethodName().equals(methodName)) {
        result = i;
        break;
      }
    }
    return result;
  }
}
