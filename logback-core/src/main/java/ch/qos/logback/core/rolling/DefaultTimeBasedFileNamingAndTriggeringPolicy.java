/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.File;
import java.time.Instant;
import java.util.Date;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.helper.TimeBasedArchiveRemover;

/**
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 * @param <E>
 */
@NoAutoStart
public class DefaultTimeBasedFileNamingAndTriggeringPolicy<E> extends TimeBasedFileNamingAndTriggeringPolicyBase<E> {

    @Override
    public void start() {
        super.start();
        if (!super.isErrorFree())
            return;
        if (tbrp.fileNamePattern.hasIntegerTokenCOnverter()) {
            addError("Filename pattern [" + tbrp.fileNamePattern
                    + "] contains an integer token converter, i.e. %i, INCOMPATIBLE with this configuration. Remove it.");
            return;
        }

        archiveRemover = new TimeBasedArchiveRemover(tbrp.fileNamePattern, rc);
        archiveRemover.setContext(context);
        started = true;
    }

    public boolean isTriggeringEvent(File activeFile, final E event) {
        long currentTime = getCurrentTime();
        long localNextCheck = atomicNextCheck.get();
        if (currentTime >= localNextCheck) {
            long nextCheckCandidate = computeNextCheck(currentTime);
            boolean success = atomicNextCheck.compareAndSet(localNextCheck, nextCheckCandidate);
            if(success) {
                //Date dateOfElapsedPeriod = new Date(this.dateInCurrentPeriod.getTime());
                Instant instantOfElapsedPeriod = dateInCurrentPeriod;
                addInfo("Elapsed period: " + instantOfElapsedPeriod.toString());
                this.elapsedPeriodsFileName = tbrp.fileNamePatternWithoutCompSuffix.convert(instantOfElapsedPeriod);
                setDateInCurrentPeriod(currentTime);
            }
            return success;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "c.q.l.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy";
    }

}
