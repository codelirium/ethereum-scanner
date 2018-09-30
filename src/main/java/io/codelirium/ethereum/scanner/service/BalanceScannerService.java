package io.codelirium.ethereum.scanner.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;
import static org.web3j.crypto.Credentials.create;
import static org.web3j.protocol.Web3j.build;


@Service
public class BalanceScannerService {

	private static final Logger LOGGER = getLogger(BalanceScannerService.class);


	@Value("${ethereum.node}")
	private String ethereumNode;

	private Web3j web3;

	private BigInteger currentPrivateKeyBI;

	private BigInteger totalScanned = ZERO;


	public BalanceScannerService() { }


	@PostConstruct
	private void init() {

		web3 = build(new HttpService(ethereumNode));

	}


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

			final BigInteger balance = getBalance(publicAddress);


			if (balance.compareTo(ZERO) > 0) {

					LOGGER.debug("Address: " + publicAddress + " - Key: " + getFormatted(currentPrivateKeyBI.toString(16), 64) + " - Balance: " + balance);

			}


			currentPrivateKeyBI = currentPrivateKeyBI.add(ONE);

			totalScanned = totalScanned.add(ONE);
		}

		return;
	}


	private static String getPublicAddress(final String privateKey) {

		notNull(privateKey, "The private key cannot be null.");

		return create(privateKey).getAddress();
	}


	private BigInteger getBalance(final String address) throws InterruptedException, ExecutionException {

		notNull(address, "The address cannot be null.");


		final EthGetBalance ethGetBalance = web3
				.ethGetBalance(address, DefaultBlockParameterName.LATEST)
				.sendAsync()
				.get();


		return ethGetBalance.getBalance();
	}

	private static String getFormatted(final String input, final int length) {

		notNull(input, "The input cannot be null.");


		final StringBuilder output = new StringBuilder("0x");

		for (int i = 0; i < length - input.length(); i++) {

			output.append("0");

		}

		output.append(input);


		return output.toString();
	}
}
