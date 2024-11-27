package net.remmintan.mods.minefortress.core.automation.iterators;

import java.util.Iterator;

public interface ResetableIterator<T> extends Iterator<T> {

    void reset();

}
