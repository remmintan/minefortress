package org.minefortress.fortress.automation.iterators;

import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

abstract class AbstractFilteredIterator implements Iterator<BlockPos> {

    private final Iterator<BlockPos> iterator;

    public AbstractFilteredIterator(Iterator<BlockPos> iterable) {
        final var spliterator = Spliterators.spliteratorUnknownSize(iterable, Spliterator.ORDERED);
        this.iterator = StreamSupport
                .stream(spliterator, false)
                .map(BlockPos::toImmutable)
                .filter(this::getFilter)
                .iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public BlockPos next() {
        return iterator.next();
    }

    protected abstract boolean getFilter(BlockPos pos);

}
