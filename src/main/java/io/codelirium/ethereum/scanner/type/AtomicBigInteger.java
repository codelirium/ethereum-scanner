package io.codelirium.ethereum.scanner.type;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Boolean.TRUE;
import static java.math.BigInteger.ONE;


public final class AtomicBigInteger {

	private final AtomicReference<BigInteger> valueHolder = new AtomicReference<>();


	public AtomicBigInteger(final BigInteger bigInteger) {

		valueHolder.set(bigInteger);

	}



	public BigInteger get() {

		return valueHolder.get();

	}


	public void set(final BigInteger bigInteger) {

		valueHolder.set(bigInteger);

	}


	public BigInteger incrementAndGet() {

		while (TRUE) {

			final BigInteger current = valueHolder.get();

			final BigInteger next = current.add(ONE);


			if (valueHolder.compareAndSet(current, next)) {

				return next;

			}
		}


		return null;
	}
}
