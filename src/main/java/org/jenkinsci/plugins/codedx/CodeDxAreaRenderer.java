package org.jenkinsci.plugins.codedx;

import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links. This renderer does not render tooltips, these need to be defined in
 * sub-classes.
 * 
 * @author ademartini This file is heavily derived from the sloccount-plugin
 */
public class CodeDxAreaRenderer extends StackedAreaRenderer2 {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1440842055316682192L;

    /** {@inheritDoc} */
    @Override
    public final String generateURL(final CategoryDataset dataset, final int row, final int column) {
        return getLabel(dataset, column).build.getNumber() + "";
    }

    /**
     * Returns the build label at the specified column.
     *
     * @param dataset
     *            data set of values
     * @param column
     *            the column
     * @return the label of the column
     */
    private NumberOnlyBuildLabel getLabel(final CategoryDataset dataset, final int column) {
        return (NumberOnlyBuildLabel)dataset.getColumnKey(column);
    }
}