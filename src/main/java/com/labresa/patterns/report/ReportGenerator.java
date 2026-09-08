package com.labresa.patterns.report;

import java.util.List;

/**
 * Template Method pattern: fixes the skeleton (fetch -> format) that every
 * report follows, while letting each concrete report supply its own data
 * source and formatting. Adding a new report type (e.g. a cost report) only
 * requires implementing the two abstract steps below - the overall algorithm
 * shape never has to be duplicated or re-tested.
 */
public abstract class ReportGenerator<T> {

    public final String generateReport() {
        List<T> data = fetchData();
        return formatReport(data);
    }

    protected abstract List<T> fetchData();

    protected abstract String formatReport(List<T> data);
}
