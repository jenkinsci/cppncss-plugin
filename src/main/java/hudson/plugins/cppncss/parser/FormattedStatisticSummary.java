/*
 * The MIT License
 *
 * Copyright (c) 2018 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.cppncss.parser;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;

/**
 * @author Oleg Nenashev
 */
@Restricted(NoExternalUse.class)
public final class FormattedStatisticSummary extends StatisticSummary {
    
    private boolean doCompare = true;
    private final long wasFunctions;
    private final long wasNcss;
    private final long wasCcn;
    private final long wasCcnViolations;
    private final long wasNcssViolations;
    private final long wasMaxCcn;
    private final long functions;
    private final long ncss;
    private final long ccn;
    private final long ccnViolations;
    private final long ncssViolations;
    private final long maxCcn;

    public FormattedStatisticSummary(long wasCcn, long wasFunctions, long wasNcss,
            long wasCcnViolations, long wasNcssViolations, long wasMaxCcn, long ccn, long functions, 
            long ncss, long ccnViolations, long ncssViolations, long maxCcn) {
        this.wasFunctions = wasFunctions;
        this.wasNcss = wasNcss;
        this.wasCcn = wasCcn;
        this.wasCcnViolations = wasCcnViolations;
        this.wasNcssViolations = wasNcssViolations;
        this.wasMaxCcn = wasMaxCcn;
        this.functions = functions;
        this.ncss = ncss;
        this.ccn = ccn;
        this.ccnViolations = ccnViolations;
        this.ncssViolations = ncssViolations;
        this.maxCcn = maxCcn;
        
    }

    public FormattedStatisticSummary(long ccn, long functions, long ncss, long cnnViolations, long ncssViolations,
            long maxCcn) {
        this(0, 0, 0, 0, 0, 0, ccn, functions, ncss, cnnViolations, ncssViolations, maxCcn);
        doCompare = false;
    }

    public FormattedStatisticSummary(@Nonnull Statistic was, @Nonnull Statistic now) {
        this(was.getCcn(), was.getFunctions(), was.getNcss(), was.getCcnViolations(), was.getNcssViolations(),
                was.getMaxCcn(), now.getCcn(), now.getFunctions(), now.getNcss(), now.getCcnViolations(),
                now.getNcssViolations(), now.getMaxCcn());
    }

    public long getCcn() {
        return ccn;
    }

    public long getFunctions() {
        return functions;
    }

    public long getNcss() {
        return ncss;
    }

    private String diff(long old, long new_, String name) {
        String diffString = "<li>" + name + ": " + new_ + " ";
        if (doCompare) {
            if (old == new_) {
                diffString += "(No change)";
            } else if (old < new_) {
                diffString += "(+" + (new_ - old) + ")";
            } else { // if (a < b)
                diffString += "(-" + (old - new_) + ")";
            }
        }
        diffString += "</li>";
        return diffString;
    }

    /**
     * Converts object to the HTML string
     */
    @Override
    public String getHtmlSummary() {
        return "<ul>"
                + diff(wasCcn, ccn, "CCN")
                + diff(wasFunctions, functions, "Functions")
                + diff(wasNcss, ncss, "NCSS")
                + diff(wasCcnViolations, ccnViolations, "CCN Violations")
                + diff(wasNcssViolations, ncssViolations, "NCSS Violations")
                + diff(wasMaxCcn, maxCcn, "Max CCN")
                + "</ul>";
    }

    @Override
    public String toString() {
        return String.format("cnn: %s, functions: %s, ncss: %s. Was: %s/%s/%s", ccn, functions, ncss, wasCcn, wasFunctions, wasNcss);
    }
}
