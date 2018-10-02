package io.codelirium.ethereum.scanner.util;

import static java.util.stream.IntStream.range;
import static org.springframework.util.Assert.notNull;
import static org.web3j.crypto.Credentials.create;


public class EthereumUtil {


	private EthereumUtil() { }


	public static boolean isValidEthereumPrivateKey(final String privateKey) {

		notNull(privateKey, "The ethereum private key cannot be null.");


		return privateKey.matches("^0x[0-9a-fA-F]{64}$");
	}


	public static String getPublicAddress(final String privateKey) {

		notNull(privateKey, "The private key cannot be null.");


		return create(privateKey).getAddress();
	}


	public static String getAddressFormatted(final String address, final int length) {

		notNull(address, "The address cannot be null.");


		final StringBuilder formatted = new StringBuilder("0x");


		range(0, length - address.length()).forEach(i -> formatted.append("0"));

		formatted.append(address);


		return formatted.toString();
	}
}
