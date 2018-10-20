package io.codelirium.ethereum.scanner.action;

import io.codelirium.ethereum.scanner.service.SequentialBalanceScannerService;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
//import static io.codelirium.ethereum.scanner.util.EthereumUtil.getAddressFormatted;
import static java.math.BigInteger.valueOf;
import static org.slf4j.LoggerFactory.getLogger;


public class RangeScanAction extends RecursiveAction {

	private static final Logger LOGGER = getLogger(RangeScanAction.class);


	private String start;

	private String end;

	private BigInteger startBI;

	private BigInteger endBI;

	private long batchSize;

	private SequentialBalanceScannerService sequentialBalanceScannerService;


	public RangeScanAction(final SequentialBalanceScannerService sequentialBalanceScannerService, final long batchSize, final String start, final String end) {

		this.start = start;
		this.end = end;
		this.startBI = new BigInteger(start, 16);
		this.endBI = new BigInteger(end, 16);
		this.batchSize = batchSize;
		this.sequentialBalanceScannerService = sequentialBalanceScannerService;

	}


	@Override
	protected void compute() {

		final BigInteger length = endBI.subtract(startBI);


		if (length.compareTo(valueOf(batchSize)) < 0) {

			//LOGGER.debug("Scanning balances within the range [" + getAddressFormatted(start, 64) + "] -> [" + getAddressFormatted(end, 64) + "] ...");

			sequentialBalanceScannerService.scan(start, end);

		} else {

			invokeAll(createSubTasks());

		}
	}


	private List<RangeScanAction> createSubTasks() {

		final List<RangeScanAction> subTasks = newArrayListWithCapacity(2);


		final BigInteger middle = startBI.add(endBI).divide(valueOf(2L));


		subTasks.add(new RangeScanAction(sequentialBalanceScannerService, batchSize, start, middle.toString(16)));

		subTasks.add(new RangeScanAction(sequentialBalanceScannerService, batchSize, middle.toString(16), end));


		return subTasks;
	}
}
