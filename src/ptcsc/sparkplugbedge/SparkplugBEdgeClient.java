package ptcsc.sparkplugbedge;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;

public class SparkplugBEdgeClient extends ConnectedThingClient {

	private static final String THINGWORX_CONNECT_INFO = "ThingWorxConnectInfo";
	private static final int SCAN_RATE = 1000;
	private static final Logger LOG = LoggerFactory.getLogger(SparkplugBEdgeClient.class);

	public SparkplugBEdgeClient(ClientConfigurator config) throws Exception {
		super(config);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("JSON configuration file not specified");
			System.exit(1);
		}

		// Set the required configuration information
		ClientConfigurator config = new ClientConfigurator();

		// Parse the JSON config
		File configFile = new File(args[0]);
	    InputStream configStream = new FileInputStream(configFile);
		if (configStream == null) {
			throw new NullPointerException("Cannot find config file " + args[0]);
		}

		JSONTokener tokenizer = new JSONTokener(configStream);
		JSONObject sparkplugbEdgeConfig = new JSONObject(tokenizer);

		// The uri for connecting to ThingWorx
		config.setUri(sparkplugbEdgeConfig.getJSONObject(THINGWORX_CONNECT_INFO).getString("ServerURL"));

		// Reconnect every 15 seconds if a disconnect occurs or if initial connection
		// cannot be made
		config.setReconnectInterval(15);

		// Set the security using an Application Key
		String appKey = sparkplugbEdgeConfig.getJSONObject(THINGWORX_CONNECT_INFO).getString("AppKey");

		// Set the security using an Application Key
		config.setSecurityClaims(new EdgeDBPasswordCallback(appKey));

		// Set the name of the client
		config.setName(null);

		// This will allow us to test against a server using a self-signed certificate.
		// This should be removed for production systems.
		config.ignoreSSLErrors(true); // All self signed certs

		// Create the client passing in the configuration from above
		SparkplugBEdgeClient client = new SparkplugBEdgeClient(config);
		String remoteThingName = sparkplugbEdgeConfig.getJSONObject(THINGWORX_CONNECT_INFO).getString("RemoteThingName");

		final SparkplugBEdgeThing edgeThing = new SparkplugBEdgeThing(remoteThingName, sparkplugbEdgeConfig, client);
		client.bindThing(edgeThing);

		try {
			client.start();
			System.out.println("client started");
		} catch (Exception eStart) {
			LOG.error("Initial Start Failed : " + eStart.getMessage());
		}

		// As long as the client has not been shutdown, continue
		while (!client.isShutdown()) {
			// Only process the Virtual Things if the client is connected
			if (client.isConnected()) {
				// Loop over all the Virtual Things and process them
				for (VirtualThing thing : client.getThings().values()) {
					try {
						thing.processScanRequest();
					} catch (Exception eProcessing) {
						LOG.error("Error Processing Scan Request for [" + thing.getName() + "] : "
								+ eProcessing.getMessage());
					}
				}
			}
			// Suspend processing at the scan rate interval
			Thread.sleep(SCAN_RATE);
		}
	}

}
