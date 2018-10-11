package io.codelirium.ethereum.scanner.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
class NodeConfiguration {

	@Value("${infura.client.endpoint.1}")
	private String endpointOne;

	@Value("${infura.client.endpoint.2}")
	private String endpointTwo;

	@Value("${infura.client.endpoint.3}")
	private String endpointThree;

	@Value("${infura.client.endpoint.4}")
	private String endpointFour;

}
