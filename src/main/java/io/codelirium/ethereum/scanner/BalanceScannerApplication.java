package io.codelirium.ethereum.scanner;

import io.codelirium.ethereum.scanner.action.RangeScanAction;
import io.codelirium.ethereum.scanner.service.SequentialBalanceScannerService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.retry.annotation.EnableRetry;
import javax.inject.Inject;
import java.math.BigInteger;

import static io.codelirium.ethereum.scanner.util.EthereumUtil.isValidEthereumPrivateKey;
import static java.lang.System.out;
import static java.util.Objects.isNull;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.Banner.Mode.OFF;


@EnableRetry
@SpringBootApplication
public class BalanceScannerApplication implements CommandLineRunner {

	private static final Logger LOGGER = getLogger(BalanceScannerApplication.class);


	@Value("${scanner.parallel.batch-size:10}")
	private long batchSize;

	@Inject
	private SequentialBalanceScannerService sequentialBalanceScannerService;


	public static void main(final String[] args) {

		new SpringApplicationBuilder(BalanceScannerApplication.class)
				.bannerMode(OFF)
				.logStartupInfo(false)
				.run(args);

	}


	@Override
	public void run(final String... args) throws Exception {

		if (args.length == 3 && args[0].endsWith("-scan") && !isNull(args[1]) && !isNull(args[2])) {

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


			switch (args[0]) {

				case "--sequential-scan":

					LOGGER.debug("Scanning balances within the range [" + startPrivateKey + "] -> [" + endPrivateKey + "] ...");


					sequentialBalanceScannerService.scan(start, end);


					return;

				case "--parallel-scan":

					commonPool().invoke(new RangeScanAction(sequentialBalanceScannerService, batchSize, start, end));


					return;

				default:

					out.println("\nThe scan command [" + args[0] + "] was not recognised.");

			}
		}


		printUsage();
	}


	private void printUsage() {

		out.println("\nUsage: java -jar target/ethereum-scanner.jar [scan-command] <start-pk> <end-pk>\n");
		out.println("\tAvailable scan commands: ");
		out.println("\t\t--sequential-scan:\tScans the addresses in range one by one.");
		out.println("\t\t--parallel-scan:\tScans the addresses in parallel.\n");
		out.println("\tValid range: ");
		out.println("\t\tstart-pk: 0x0000000000000000000000000000000000000000000000000000000000000001");
		out.println("\t\tend-pk:   0xfffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364140\n");

	}
}
