package io.codelirium.ethereum.scanner.type;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Long.MAX_VALUE;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;


public class CircularList<T> {

	private final List<T> buffer;

	private final int size;

	private AtomicLong currentIndex;


	public CircularList(final Collection<T>items) {

		buffer = new CopyOnWriteArrayList<>(items);

		size = buffer.size();

		currentIndex = new AtomicLong(0L);

	}


	private int getNextIndex() {

		if (currentIndex.get() == MAX_VALUE) {

			currentIndex = new AtomicLong(0L);

		}


		return (int) (currentIndex.getAndIncrement() % size);
	}

	public Optional<T> getNextElement() {

		return (isNull(buffer) || buffer.isEmpty()) ? empty() : of(buffer.get(getNextIndex()));

	}
}
