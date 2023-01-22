package org.minefortress.fortress.automation.iterators;

import java.util.Iterator;

public interface ResetableIterator<T> extends Iterator<T> {

    void reset();

}
