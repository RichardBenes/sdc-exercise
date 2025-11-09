package com.rbenes;

public final class EndOfProcessingCell implements EnhancedCellI {

    public int getVisualRowIndex() {

        // This is safe; max. number of rows in Excel is around 1 bilion,
        // we're returning value above 2 bilions.
        return Integer.MAX_VALUE;
    }
}
