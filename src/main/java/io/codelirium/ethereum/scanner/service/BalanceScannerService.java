package io.codelirium.ethereum.scanner.service;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static org.springframework.util.Assert.notNull;
import static org.web3j.crypto.Credentials.create;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;


public abstract class BalanceScannerService {


	public abstract void scan(final String startPrivateKey, final String endPrivateKey) throws Exception;


	static String getPublicAddress(final String privateKey) {

		notNull(privateKey, "The private key cannot be null.");


		return create(privateKey).getAddress();
	}


	BigInteger getBalance(final Web3j web3, final String address) throws InterruptedException, ExecutionException {

		notNull(web3, "The ethereum client cannot be null.");
		notNull(address, "The address cannot be null.");


		final EthGetBalance ethGetBalance = web3
				.ethGetBalance(address, LATEST)
				.sendAsync()
				.get();


		return ethGetBalance.getBalance();
	}


	static String getFormatted(final String input, final int length) {

		notNull(input, "The input cannot be null.");


		final StringBuilder output = new StringBuilder("0x");

		for (int i = 0; i < length - input.length(); i++) {

			output.append("0");

		}

		output.append(input);


		return output.toString();
	}
}
