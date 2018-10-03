package io.codelirium.ethereum.scanner.service;

import org.slf4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetCode;
import java.io.IOException;
import java.math.BigInteger;

import static java.math.BigInteger.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;


public abstract class BalanceScannerService {

	private static final Logger LOGGER = getLogger(BalanceScannerService.class);


	public abstract void scan(final String startPrivateKey, final String endPrivateKey);


	BigInteger getBalance(final Web3j web3, final String address) {

		notNull(web3, "The ethereum client cannot be null.");
		notNull(address, "The address cannot be null.");


		EthGetBalance ethGetBalance;


		try {

			ethGetBalance = web3.ethGetBalance(address, LATEST).send();

		} catch (final IOException e) {

			LOGGER.error("Error for getBalance(" + address + "): ", e);


			return ZERO;
		}


		if (ethGetBalance.hasError()) {

			LOGGER.error("Error for getBalance(" + address + "): " + ethGetBalance.getError().getMessage());


			return ZERO;
		}


		return ethGetBalance.getBalance();
	}


	boolean isContract(final Web3j web3, final String address) {

		notNull(web3, "The ethereum client cannot be null.");
		notNull(address, "The address cannot be null.");


		EthGetCode ethGetCode;


		try {

			ethGetCode = web3.ethGetCode(address, LATEST).send();

		} catch (final IOException e) {

			LOGGER.error("Error for isContract(" + address + "): ", e);


			return false;

		}


		if (ethGetCode.hasError()) {

			LOGGER.error("Error for isContract(" + address + "): " + ethGetCode.getError().getMessage());


			return false;

		}


		return !ethGetCode.getCode().equals("0x");
	}
}
