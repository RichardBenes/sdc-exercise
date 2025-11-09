package com.rbenes;

public sealed interface EnhancedCellI
    extends Comparable<EnhancedCell>
    permits EnhancedCell, EndOfProcessingCell {

    int getVisualRowIndex();

    @Override
    default int compareTo(EnhancedCell o) {
        return Integer.compare(
            getVisualRowIndex(), o.getVisualRowIndex());
    }
}
