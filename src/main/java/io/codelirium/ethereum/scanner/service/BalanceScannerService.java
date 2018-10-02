package io.codelirium.ethereum.scanner.service;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetCode;
import java.io.IOException;
import java.math.BigInteger;

import static org.springframework.util.Assert.notNull;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;


public abstract class BalanceScannerService {


	public abstract void scan(final String startPrivateKey, final String endPrivateKey);


	BigInteger getBalance(final Web3j web3, final String address) {

		notNull(web3, "The ethereum client cannot be null.");
		notNull(address, "The address cannot be null.");


		EthGetBalance ethGetBalance;


		try {

			ethGetBalance = web3.ethGetBalance(address, LATEST).send();

		} catch (final IOException e) {

			return getBalance(web3, address);

		}


		if (ethGetBalance.hasError()) {

			return getBalance(web3, address);

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

			return isContract(web3, address);

		}


		if (ethGetCode.hasError()) {

			return isContract(web3, address);

		}


		return !ethGetCode.getCode().equals("0x");
	}
}
