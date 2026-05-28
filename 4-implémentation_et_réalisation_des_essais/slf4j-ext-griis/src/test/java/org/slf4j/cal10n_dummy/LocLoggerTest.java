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
package org.slf4j.cal10n_dummy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.MessageConveyor;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;
import org.slf4j.dummyExt.ListAppender;

public class LocLoggerTest {

  ListAppender listAppender;
  Logger logbackRoot;

  IMessageConveyor imc = new MessageConveyor(Locale.UK);
  LocLoggerFactory llFactory_uk = new LocLoggerFactory(imc);

  final static String EXPECTED_FILE_NAME = "LocLoggerTest";

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

  @Test
  public void testSmoke() {
    LocLogger locLogger = llFactory_uk.getLocLogger(this.getClass().getSimpleName());
    locLogger.info(Months.JAN);
    verify(listAppender.list.get(0), "January");
  }
}
