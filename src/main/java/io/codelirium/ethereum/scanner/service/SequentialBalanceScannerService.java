package io.codelirium.ethereum.scanner.service;

import io.codelirium.ethereum.scanner.client.ClientPool;
import io.codelirium.ethereum.scanner.type.AtomicBigInteger;
import org.slf4j.Logger;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.collect.Lists.newLinkedList;
import static io.codelirium.ethereum.scanner.util.EthereumUtil.getAddressFormatted;
import static io.codelirium.ethereum.scanner.util.EthereumUtil.getPublicAddress;
import static java.lang.Runtime.getRuntime;
import static java.math.BigInteger.ZERO;
import static java.util.Collections.sort;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;


@Service
public class SequentialBalanceScannerService extends BalanceScannerService {

	private static final Logger LOGGER = getLogger(SequentialBalanceScannerService.class);


	private static final int HISTORY_TRAIL_SIZE = 2 * getRuntime().availableProcessors();


	private ClientPool clientPool;

	private AtomicBigInteger totalScanned = new AtomicBigInteger(ZERO);

	private List<BigInteger> lastTriedPrivateKeys = newLinkedList();

	private Lock lock = new ReentrantLock();


	@Inject
	public SequentialBalanceScannerService(final ClientPool clientPool) {

		this.clientPool = clientPool;

	}


	@PreDestroy
	private void printLastKey() {

		sort(lastTriedPrivateKeys);


		LOGGER.debug("Last smallest key tried: " + toKey(lastTriedPrivateKeys.get(0)));

		LOGGER.debug("Total number of addresses scanned: " + totalScanned.get().toString());

	}


	@Override
	@Retryable(value = { RuntimeException.class }, maxAttempts = 500, backoff = @Backoff(delay = 2000))
	public void scan(final String startPrivateKey, final String endPrivateKey) {

		notNull(startPrivateKey, "The start private key cannot be null.");
		notNull(endPrivateKey, "The end private key cannot be null.");


		final BigInteger startPrivateKeyBI = new BigInteger(startPrivateKey, 16);

		final BigInteger endPrivateKeyBI = new BigInteger(endPrivateKey, 16);


		final AtomicBigInteger localCurrentPrivateKeyBI = new AtomicBigInteger(startPrivateKeyBI);


		while (localCurrentPrivateKeyBI.get().compareTo(endPrivateKeyBI) <= 0) {

			final String publicAddress = getPublicAddress("0x" + localCurrentPrivateKeyBI.get().toString(16));

			final BigInteger balance = getBalance(clientPool, publicAddress);


			if (balance.compareTo(ZERO) > 0) {

				LOGGER.debug("Address: " + publicAddress + " - Key: " + toKey(localCurrentPrivateKeyBI.get()) + " - Balance: " + balance + " wei.");

			}


			if (isContract(clientPool, publicAddress)) {

				LOGGER.debug("Address: " + publicAddress + " - Key: " + toKey(localCurrentPrivateKeyBI.get()) + " - Contract detected.");

			}


			try {

				lock.lock();

				if (lastTriedPrivateKeys.size() == HISTORY_TRAIL_SIZE) {

					lastTriedPrivateKeys.remove(0);

				}

				lastTriedPrivateKeys.add(localCurrentPrivateKeyBI.get());

			} finally {

				lock.unlock();

			}


			totalScanned.incrementAndGet();

			localCurrentPrivateKeyBI.incrementAndGet();
		}
	}


	private String toKey(final BigInteger bigInteger) {

		return getAddressFormatted(bigInteger.toString(16), 64);

	}
}
