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

import static io.codelirium.ethereum.scanner.util.EthereumUtil.getAddressFormatted;
import static io.codelirium.ethereum.scanner.util.EthereumUtil.getPublicAddress;
import static java.math.BigInteger.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;


@Service
public class SequentialBalanceScannerService extends BalanceScannerService {

	private static final Logger LOGGER = getLogger(SequentialBalanceScannerService.class);


	private ClientPool clientPool;

	private AtomicBigInteger currentPrivateKeyBI;

	private AtomicBigInteger totalScanned = new AtomicBigInteger(ZERO);


	@Inject
	public SequentialBalanceScannerService(final ClientPool clientPool) {

		this.clientPool = clientPool;

	}


	@PreDestroy
	private void printLastKey() {

		LOGGER.debug("Last key tried: " + getLastTriedAddress());

		LOGGER.debug("Total number of addresses scanned: " + totalScanned.get().toString());

	}


	@Override
	@Retryable(value = { RuntimeException.class }, maxAttempts = 500, backoff = @Backoff(delay = 2000))
	public void scan(final String startPrivateKey, final String endPrivateKey) {

		notNull(startPrivateKey, "The start private key cannot be null.");
		notNull(endPrivateKey, "The end private key cannot be null.");


		final BigInteger startPrivateKeyBI = new BigInteger(startPrivateKey, 16);

		final BigInteger endPrivateKeyBI = new BigInteger(endPrivateKey, 16);


		currentPrivateKeyBI = new AtomicBigInteger(startPrivateKeyBI);


		while (currentPrivateKeyBI.get().compareTo(endPrivateKeyBI) <= 0) {

			final String publicAddress = getPublicAddress("0x" + currentPrivateKeyBI.get().toString(16));

			final BigInteger balance = getBalance(clientPool, publicAddress);


			if (balance.compareTo(ZERO) > 0) {

				LOGGER.debug("Address: " + publicAddress + " - Key: " + getLastTriedAddress() + " - Balance: " + balance + " wei.");

			}


			if (isContract(clientPool, publicAddress)) {

				LOGGER.debug("Address: " + publicAddress + " - Key: " + getLastTriedAddress() + " - Contract detected.");

			}


			currentPrivateKeyBI.incrementAndGet();

			totalScanned.incrementAndGet();
		}
	}


	private String getLastTriedAddress() {

		return getAddressFormatted(currentPrivateKeyBI.get().toString(16), 64);

	}
}
