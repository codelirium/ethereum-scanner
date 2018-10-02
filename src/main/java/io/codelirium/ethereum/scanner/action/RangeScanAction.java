package io.codelirium.ethereum.scanner.action;

import io.codelirium.ethereum.scanner.service.SequentialBalanceScannerService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import static java.math.BigInteger.valueOf;


public class RangeScanAction extends RecursiveAction {

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

			sequentialBalanceScannerService.scan(start, end);

		} else {

			invokeAll(createSubTasks());

		}
	}


	private List<RangeScanAction> createSubTasks() {

		final List<RangeScanAction> subTasks = new ArrayList<>(2);


		final BigInteger middle = startBI.add(endBI).divide(valueOf(2L));


		subTasks.add(new RangeScanAction(sequentialBalanceScannerService, batchSize, start, middle.toString(16)));

		subTasks.add(new RangeScanAction(sequentialBalanceScannerService, batchSize, middle.toString(16), end));


		return subTasks;
	}
}
