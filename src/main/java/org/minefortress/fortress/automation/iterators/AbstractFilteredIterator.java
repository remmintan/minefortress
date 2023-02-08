package org.minefortress.fortress.automation.iterators;

import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.automation.AutomationBlockInfo;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

abstract class AbstractFilteredIterator implements ResetableIterator<AutomationBlockInfo> {

    private boolean reset = false;
    private final Iterator<AutomationBlockInfo> iterator;

    public AbstractFilteredIterator(Iterator<BlockPos> iterable) {
        final var spliterator = Spliterators.spliteratorUnknownSize(iterable, Spliterator.ORDERED);
        this.iterator = StreamSupport
                .stream(spliterator, false)
                .map(BlockPos::toImmutable)
                .filter(this::filter)
                .map(this::map)
                .iterator();
    }

    @Override
    public boolean hasNext() {
        return !reset && iterator.hasNext();
    }

    @Override
    public AutomationBlockInfo next() {
        return iterator.next();
    }

    protected abstract boolean filter(BlockPos pos);

    protected abstract AutomationBlockInfo map(BlockPos pos);

    @Override
    public void reset() {
        this.reset = true;
    }
}
