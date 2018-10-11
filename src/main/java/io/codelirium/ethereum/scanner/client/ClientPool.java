package io.codelirium.ethereum.scanner.client;

import io.codelirium.ethereum.scanner.type.CircularList;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import javax.inject.Inject;

import static com.google.common.collect.Lists.newArrayList;
import static org.web3j.protocol.Web3j.build;


@Component
public class ClientPool {

	private CircularList<Web3j> clients;


	@Inject
	public ClientPool(final NodeConfiguration nodes) {

		clients = new CircularList<>(newArrayList(build(new HttpService(nodes.getEndpointOne())),
												  build(new HttpService(nodes.getEndpointTwo())),
												  build(new HttpService(nodes.getEndpointThree())),
												  build(new HttpService(nodes.getEndpointFour()))));

	}


	public Web3j getClient() {

		return clients.getNextElement().get();

	}
}
