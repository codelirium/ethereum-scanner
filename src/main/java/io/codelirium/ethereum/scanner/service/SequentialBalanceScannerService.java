package io.codelirium.ethereum.scanner.service;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;


@Service
public class SequentialBalanceScannerService extends BalanceScannerService {

	private static final Logger LOGGER = getLogger(SequentialBalanceScannerService.class);


	@Inject
	private Web3j web3;

	private BigInteger currentPrivateKeyBI;

	private BigInteger totalScanned = ZERO;


	@PreDestroy
	private void printLastKey() {

		LOGGER.debug("Last key tried: " + getFormatted(currentPrivateKeyBI.toString(16), 64));

		LOGGER.debug("Total number of addresses scanned: " + totalScanned.toString());

	}


	public void scan(final String startPrivateKey, final String endPrivateKey) throws Exception {

		notNull(startPrivateKey, "The start private key cannot be null.");
		notNull(endPrivateKey, "The end private key cannot be null.");


		final BigInteger startPrivateKeyBI = new BigInteger(startPrivateKey, 16);

		final BigInteger endPrivateKeyBI = new BigInteger(endPrivateKey, 16);


		currentPrivateKeyBI = startPrivateKeyBI;


		while (currentPrivateKeyBI.compareTo(endPrivateKeyBI) <= 0) {

			final String publicAddress = getPublicAddress("0x" + currentPrivateKeyBI.toString(16));

			final BigInteger balance = getBalance(web3, publicAddress);


			if (balance.compareTo(ZERO) > 0) {

				LOGGER.debug("Address: " + publicAddress + " - Key: " + getFormatted(currentPrivateKeyBI.toString(16), 64) + " - Balance: " + balance + " wei.");

			}


			currentPrivateKeyBI = currentPrivateKeyBI.add(ONE);

			totalScanned = totalScanned.add(ONE);
		}
	}
}
