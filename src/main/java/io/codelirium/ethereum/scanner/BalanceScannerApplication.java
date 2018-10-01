package io.codelirium.ethereum.scanner;

import io.codelirium.ethereum.scanner.service.SequentialBalanceScannerService;
import org.slf4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import javax.inject.Inject;
import java.math.BigInteger;

import static java.util.Objects.isNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;


@SpringBootApplication
public class BalanceScannerApplication implements CommandLineRunner {

	private static final Logger LOGGER = getLogger(BalanceScannerApplication.class);


	@Inject
	private SequentialBalanceScannerService sequentialBalanceScannerService;


	public static void main(final String[] args) {

		new SpringApplicationBuilder(BalanceScannerApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.logStartupInfo(false)
				.run(args);

	}


	@Override
	public void run(final String... args) throws Exception {

		if (args.length == 3 && args[0].equals("--scan-balance") && !isNull(args[1]) && !isNull(args[2])) {

			final String startPrivateKey = args[1];

			final String endPrivateKey = args[2];


			if (!isValidEthereumPrivateKey(startPrivateKey)) {

				LOGGER.error("The start ethereum private key is incorrect.");


				return;
			}


			if (!isValidEthereumPrivateKey(endPrivateKey)) {

				LOGGER.error("The end ethereum private key is incorrect.");


				return;
			}


			final String start = startPrivateKey.substring(2, startPrivateKey.length());

			final String end = endPrivateKey.substring(2, endPrivateKey.length());


			if (new BigInteger(start, 16).compareTo(new BigInteger(end, 16)) >= 0) {

				LOGGER.error("The start private key cannot be greater than the end private key.");


				return;
			}


			LOGGER.debug("Scanning balances within the range [" + startPrivateKey + "] -> [" + endPrivateKey + "] ...");

			sequentialBalanceScannerService.scan(start, end);


			return;
		}


		System.out.println("\nUsage: java -jar target/ethereum-scanner-0.0.1-SNAPSHOT.jar --scan-balance <start-pk> <end-pk>\n");
	}


	private static boolean isValidEthereumPrivateKey(final String privateKey) {

		notNull(privateKey, "The ethereum private key cannot be null.");

		return privateKey.matches("^0x[0-9a-fA-F]{64}$");
	}
}
