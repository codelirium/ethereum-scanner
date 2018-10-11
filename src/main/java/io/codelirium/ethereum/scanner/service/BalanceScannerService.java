package io.codelirium.ethereum.scanner.service;

import io.codelirium.ethereum.scanner.client.ClientPool;
import org.slf4j.Logger;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetCode;
import java.math.BigInteger;

import static java.math.BigInteger.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;


public abstract class BalanceScannerService {

	private static final Logger LOGGER = getLogger(BalanceScannerService.class);


	public abstract void scan(final String startPrivateKey, final String endPrivateKey);


	@Retryable(value = { RuntimeException.class }, maxAttempts = 500, backoff = @Backoff(delay = 2000))
	BigInteger getBalance(final ClientPool pool, final String address) {

		notNull(pool, "The pool cannot be null.");
		notNull(address, "The address cannot be null.");


		EthGetBalance ethGetBalance;


		try {

			ethGetBalance = pool.getClient().ethGetBalance(address, LATEST).send();

		} catch (final Exception e) {

			throw new RuntimeException("Error for getBalance(" + address + "): " + e.getMessage());

		}


		if (ethGetBalance.hasError()) {

			LOGGER.error("Error for getBalance(" + address + "): " + ethGetBalance.getError().getMessage());


			return ZERO;
		}


		return ethGetBalance.getBalance();
	}


	@Retryable(value = { RuntimeException.class }, maxAttempts = 500, backoff = @Backoff(delay = 2000))
	boolean isContract(final ClientPool pool, final String address) {

		notNull(pool, "The pool cannot be null.");
		notNull(address, "The address cannot be null.");


		EthGetCode ethGetCode;


		try {

			ethGetCode = pool.getClient().ethGetCode(address, LATEST).send();

		} catch (final Exception e) {

			throw new RuntimeException("Error for isContract(" + address + "): " + e.getMessage());

		}


		if (ethGetCode.hasError()) {

			LOGGER.error("Error for isContract(" + address + "): " + ethGetCode.getError().getMessage());


			return false;
		}


		return !ethGetCode.getCode().equals("0x");
	}
}
