/**
 * 
 */
package ptcsc.sparkplugbedge;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.relationships.RelationshipTypes.ThingworxEntityTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.primitives.BooleanPrimitive;
import com.thingworx.types.primitives.IntegerPrimitive;
import com.thingworx.types.primitives.IPrimitiveType;
import com.thingworx.types.primitives.JSONPrimitive;
import com.thingworx.types.primitives.NumberPrimitive;

@SuppressWarnings("serial")
public class SparkplugBEdgeThing extends VirtualThing implements Runnable, MqttCallbackExtended {

	private static final String NDATA = "NDATA";
	private static final String NBIRTH = "NBIRTH";
	private static final String NDEATH = "NDEATH";
	private static final String BD_SEQ = "bdSeq";
	private static final String NODE_CONTROL_REBIRTH = "Node Control/Rebirth";
	private static final String TLS = "TLS";
	private static final String JKS = "JKS";
	private static final String PROPERTY = "Property";
	private static final String TOPIC = "Topic";
	private static final String TCP_PREFIX = "tcp://";
	private static final String SSL_PREFIX = "ssl://";
	private static final String APPLICATION_NAME = "ApplicationName";
	private static final String SOFTWARE_VERSION = "SoftwareVersion";
	private static final String VENDOR = "Vendor";
	private static final String TIME_TO_WAIT = "TimeToWait";
	private static final String KEEP_ALIVE_INTERVAL = "KeepAliveInterval";
	private static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
	private static final String CLEAN_SESSION = "CleanSession";
	private static final String AUTOMATIC_RECONNECT = "AutomaticReconnect";
	private static final String QOS = "QoS";
	private static final String RETAINED_MESSAGES = "RetainedMessages";
	private static final String TRUST_STORE_PASSWORD = "TrustStorePassword";
	private static final String TRUST_STORE_PATH = "TrustStorePath";
	private static final String VALIDATE_CERTIFICATE = "ValidateCertificate";
	private static final String USING_ENCRYPTION = "UsingEncryption";
	private static final String CLIENT_ID = "ClientID";
	private static final String PASSWORD = "Password";
	private static final String USERNAME = "Username";
	private static final String PORT = "Port";
	private static final String HOSTNAME = "Hostname";
	private static final String PUBLISH_BIRTH_DEATH_CERTIFICATES = "PublishBirthDeathCertificates";
	private static final String COMPRESSED_MESSAGES = "CompressedMessages";
	private static final String NODE_ID = "NodeID";
	private static final String GROUP_ID = "GroupID";
	private static final String NAMESPACE = "Namespace";
	private static final String VALUE_UPDATE_TIMEOUT = "ValueUpdateTimeout";
	private static final String OK = "OK";
	private static final String NAME = "name";
	private static final String THINGWORX_CONNECT_INFO = "ThingWorxConnectInfo";
	private static final String PROPERTY_MAPPINGS = "PropertyMappings";
	private static final String SPARKPLUG_B_INFO = "SparkplugBInfo";
	private static final String MQTT_CONNECT_INFO = "MQTTConnectInfo";
	private static final String APPLICATION_DETAILS = "ApplicationDetails";

	private static final CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;
	private static final Logger _logger = LoggerFactory.getLogger(SparkplugBEdgeThing.class);
	Map<String, String> mappings;
	private SparkplugBPayloadDecoder decoder;
	private SparkplugBPayloadEncoder encoder;
	private JSONObject sparkplugbEdgeConfig;
	private String namespace;
	private String groupID;
	private String nodeID;
	private boolean compressedMessages;
	private boolean publishBirthDeathCertificates;
	private String hostname;
	private int port;
	private String brokerURL;
	private String username;
	private String password;
	private String clientID;
	private boolean usingEncryption;
	private boolean validateCertificate;
	private String trustStorePath;
	private String trustStorePassword;
	private boolean retainedMessages;
	private Object qos;
	private boolean automaticReconnect;
	private boolean cleanSession;
	private int connectionTimeout;
	private int keepAliveInterval;
	private int timeToWait;
	private int seq;
	private String vendor;
	private String softwareVersion;
	private String applicationName;
	private MqttClient mqttClient;
	private int bdSeq;
	private JSONArray propertyMappings;
	private int valueUpdateTimeout;

	public SparkplugBEdgeThing(String remoteThingName, JSONObject edgeConfig, ConnectedThingClient client)
			throws Exception {
		super(remoteThingName, "", remoteThingName, client);
		super.initializeFromAnnotations(); // required to discover remote services below

		sparkplugbEdgeConfig = edgeConfig;
		this.init();
	}

	private void init() throws Exception {
		// Initialise variables
		decoder = new SparkplugBPayloadDecoder();
		encoder = new SparkplugBPayloadEncoder();

		seq = 0;
		bdSeq = 0;

		valueUpdateTimeout = sparkplugbEdgeConfig.getJSONObject(THINGWORX_CONNECT_INFO).getInt(VALUE_UPDATE_TIMEOUT);

		namespace = sparkplugbEdgeConfig.getJSONObject(SPARKPLUG_B_INFO).getString(NAMESPACE);
		groupID = sparkplugbEdgeConfig.getJSONObject(SPARKPLUG_B_INFO).getString(GROUP_ID);
		nodeID = sparkplugbEdgeConfig.getJSONObject(SPARKPLUG_B_INFO).getString(NODE_ID);
		compressedMessages = sparkplugbEdgeConfig.getJSONObject(SPARKPLUG_B_INFO).getBoolean(COMPRESSED_MESSAGES);
		publishBirthDeathCertificates = sparkplugbEdgeConfig.getJSONObject(SPARKPLUG_B_INFO)
				.getBoolean(PUBLISH_BIRTH_DEATH_CERTIFICATES);

		hostname = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getString(HOSTNAME);
		port = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getInt(PORT);
		username = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getString(USERNAME);
		password = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getString(PASSWORD);
		clientID = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getString(CLIENT_ID);
		usingEncryption = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getBoolean(USING_ENCRYPTION);
		validateCertificate = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getBoolean(VALIDATE_CERTIFICATE);
		trustStorePath = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getString(TRUST_STORE_PATH);
		trustStorePassword = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getString(TRUST_STORE_PASSWORD);
		retainedMessages = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getBoolean(RETAINED_MESSAGES);
		qos = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getInt(QOS);
		automaticReconnect = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getBoolean(AUTOMATIC_RECONNECT);
		cleanSession = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getBoolean(CLEAN_SESSION);
		connectionTimeout = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getInt(CONNECTION_TIMEOUT);
		keepAliveInterval = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getInt(KEEP_ALIVE_INTERVAL);
		timeToWait = sparkplugbEdgeConfig.getJSONObject(MQTT_CONNECT_INFO).getInt(TIME_TO_WAIT);

		vendor = sparkplugbEdgeConfig.getJSONObject(APPLICATION_DETAILS).getString(VENDOR);
		softwareVersion = sparkplugbEdgeConfig.getJSONObject(APPLICATION_DETAILS).getString(SOFTWARE_VERSION);
		applicationName = sparkplugbEdgeConfig.getJSONObject(APPLICATION_DETAILS).getString(APPLICATION_NAME);

		// Deduce brokerURL
		brokerURL = usingEncryption ? SSL_PREFIX : TCP_PREFIX;
		brokerURL += hostname + ":" + port;

		// Initialise topic/property mappings
		mappings = new HashMap<String, String>();
		propertyMappings = sparkplugbEdgeConfig.getJSONArray(PROPERTY_MAPPINGS);
		for (int i = 0; i < propertyMappings.length(); i++) {
			JSONObject propertyMappingObject = propertyMappings.getJSONObject(i);
			String topicName = propertyMappingObject.getString(TOPIC);
			String propertyName = propertyMappingObject.getString(PROPERTY);
			mappings.put(topicName, propertyName);
		}

		// Connect to UNS
		connectListener();

	}

	// Set up MQTT connection to the Broker
	private void connectListener() {
		try {
			// Build up DEATH payload - note DEATH payloads don't have a regular sequence
			// number
			SparkplugBPayload deathPayload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(),
					newUUID(), null);
			deathPayload = addBdSeqNum(deathPayload);
			byte[] deathBytes;

			if (compressedMessages) {
				deathBytes = encoder.getBytes(PayloadUtil.compress(deathPayload, compressionAlgorithm));
			} else {
				deathBytes = encoder.getBytes(deathPayload);
			}

			// Connect to the MQTT Server
			MqttConnectOptions options = new MqttConnectOptions();
			options.setAutomaticReconnect(automaticReconnect);
			options.setCleanSession(cleanSession);
			options.setConnectionTimeout(connectionTimeout); // 30 a good default
			options.setKeepAliveInterval(keepAliveInterval); // 30 a good default
			options.setUserName(username);
			options.setPassword(password.toCharArray());

			if (publishBirthDeathCertificates) {
				options.setWill(namespace + "/" + groupID + "/" + NDEATH + "/" + nodeID, deathBytes, (int) qos,
						retainedMessages);
			}

			// deal with encryption requirements
			if (usingEncryption) {
				SocketFactory sf = SSLSocketFactory.getDefault(); // default initialisation if using encryption
				if (validateCertificate) {
					// Load the truststore
					KeyStore trustStore = KeyStore.getInstance(JKS);
					trustStore.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());

					// Set up TrustManager
					TrustManagerFactory trustManagerFactory = TrustManagerFactory
							.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustManagerFactory.init(trustStore);

					// Create SSL context
					SSLContext sc = SSLContext.getInstance(TLS);
					sc.init(null, trustManagerFactory.getTrustManagers(), null);
					sf = sc.getSocketFactory();
				} else {
					TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(X509Certificate[] certs, String authType) {
						}

						public void checkServerTrusted(X509Certificate[] certs, String authType) {
						}
					} };

					SSLContext sc = SSLContext.getInstance(TLS);
					sc.init(null, trustAllCerts, new java.security.SecureRandom());
					sf = sc.getSocketFactory();
				}
				options.setSocketFactory(sf);
			}

			// Finalise MQTT connection
			mqttClient = new MqttClient(brokerURL, clientID, new MemoryPersistence());
			mqttClient.setTimeToWait(timeToWait); // short timeout on failure to connect - 5000 a good default

			mqttClient.setCallback(this);
			mqttClient.connect(options);
			// subscribeToTopics();

			_logger.info("MQTT connection to " + brokerURL + " is successful");

		} catch (Exception e) {
			_logger.error("Unable to connect to broker with details " + brokerURL + " - " + clientID);
			_logger.error(e.getMessage());
			_logger.error(e.toString());
			StackTraceElement[] traces = e.getStackTrace();
			for (int i = 0; i < traces.length; i++) {
				_logger.warn(traces[i].toString());
			}
		}

	}

	/***********************************************************/
	/*** The following methods implement required callbacks  ***/
	/*** for the MqttCallbackExtended interface to deal with ***/
	/*** broker connection, loss of connection and incoming  ***/
	/*** messages: ***/
	/*** - connectComplete ***/
	/*** - connectionLost ***/
	/*** - deliveryComplete ***/
	/*** - messageArrived ***/
	/***********************************************************/

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		_logger.info("MQTT connectComplete callback");
		try {
			subscribeToTopics();
		} catch (MqttException e) {
			_logger.error("Error subscribing to topics");
			e.printStackTrace();
		}

	}

	@Override
	public void connectionLost(Throwable cause) {
		if (automaticReconnect) {
			_logger.error("The MQTT Connection was lost! - will auto-reconnect");
		} else {
			_logger.error("The MQTT Connection was lost! - will NOT auto-reconnect");
		}
		System.out.println(cause.getMessage());
		StackTraceElement[] stes = cause.getStackTrace();
		for (StackTraceElement ste : stes) {
			System.out.println(ste.toString());
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		_logger.debug("Published message: " + token);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		// Get property name for this topic from mappings
		_logger.debug("UNS New message arrived on topic " + topic);
		String targetPropertyName = mappings.get(topic);
		if (targetPropertyName == null) {
			// search for matching * wildcard mapping
			for (String pattern : mappings.keySet()) {
				if (pattern.indexOf("#") >= 0) {
					String revisedTopicName = pattern.substring(0, pattern.length() - 1);
					if (topic.contains(revisedTopicName)) {
						targetPropertyName = mappings.get(pattern);
					}
				}
			}
			// search for matching + wildcard mapping
			for (String pattern : mappings.keySet()) {
				if (matchSingleWildcard(pattern, topic)) {
					targetPropertyName = mappings.get(pattern);
				}
			}
		}
		_logger.debug("UNS targetPropertyName=" + targetPropertyName);

		if (targetPropertyName != null) {
			SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());
			if (compressedMessages) {
				inboundPayload = PayloadUtil.decompress(inboundPayload);
			}

			// Convert the message to JSON and save to Thing
			try {
				String payloadString = mapper.writeValueAsString(inboundPayload);
				JSONObject payload = new JSONObject(payloadString);
				// insert topic name into message
				payload.put("topic", topic);

				// update property on Thing
				ValueCollection vc = new ValueCollection();
				vc.put(targetPropertyName, new JSONPrimitive(payload));
				this.getClient().writeProperties(ThingworxEntityTypes.Things, this.getName(), vc, valueUpdateTimeout);
			} catch (Exception e) {
				_logger.warn(e.getMessage());
			}
		}
	}

	/***********************************************************/
	/*** The following methods implement required ThingWorx  ***/
	/*** edge SDK characteristics:                           ***/
	/*** - synchronizeState                                  ***/
	/*** - run                                               ***/
	/***********************************************************/

	// This method will get called when a bind or a configuration of the bound
	// properties of this thing has changed on the thingworx platform.
	// Until this event occurs for the first time after binding no property pushes
	// should be made because they will not get sent to the platform
	public void synchronizeState() {
		// Send the property values to ThingWorx when a synchronization is required
		// This is more important for a solution that does not push its properties on a
		// regular basis
		super.syncProperties();
	}

	@Override
	public void run() {
		try {
			// Delay for a period to verify that the Shutdown service will return
			Thread.sleep(1000);
			// Shutdown the client
			this.getClient().shutdown();
		} catch (Exception e) {
			// Not much can be done if there is an exception here
			e.printStackTrace();
		}
	}

	/***********************************************************/
	/*** The following methods provide helper functions to   ***/
	/*** the other methods:                                  ***/
	/*** - subscribeToTopics                                 ***/
	/*** - matchSingleWildcard                               ***/
	/*** - publishNodeBirthCertificate                       ***/
	/*** - newUUID                                           ***/
	/*** - getSeqNum                                         ***/
	/*** - addBdSeqNum                                       ***/
	/*** - matchSingleWildcard                               ***/
	/***********************************************************/

	// Subscribe to all topics specified in the JSON configuration and publish node
	// birth certificate
	private void subscribeToTopics() throws MqttException {
		for (String topic : mappings.keySet()) {
			mqttClient.subscribe(topic, (int) qos);
			_logger.info("Subscribed to Topic " + topic);
		}
		_logger.info("Subscribed to Topics");

		// Trigger publishing of Birth certificates
		if (publishBirthDeathCertificates) {
			publishNodeBirthCertificate();
			_logger.info("Published Birth Certificate");
		}
	}

	// match a single wildcard for topic subscriptions
	private boolean matchSingleWildcard(String pattern, String topic) {
		String[] patternLevels = pattern.split("/");
		String[] topicLevels = topic.split("/");

		if (patternLevels.length != topicLevels.length) {
			return false;
		}

		for (int i = 0; i < patternLevels.length; i++) {
			if (!patternLevels[i].equals("+") && !patternLevels[i].equals(topicLevels[i])) {
				return false;
			}
		}

		return true;
	}

	// Publish node birth certificate for this device "Thing"
	private void publishNodeBirthCertificate() {
		// Reset the sequence number
		seq = 0;

		// Create the BIRTH payload and set the position
		SparkplugBPayload spbPayload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(),
				newUUID(), null);

		try {
			// Set BIRTH metrics
			spbPayload = addBdSeqNum(spbPayload);
			spbPayload
					.addMetric(new MetricBuilder(NODE_CONTROL_REBIRTH, MetricDataType.Boolean, false).createMetric());
			spbPayload.addMetric(new MetricBuilder(VENDOR, MetricDataType.String, vendor).createMetric());
			spbPayload.addMetric(
					new MetricBuilder(SOFTWARE_VERSION, MetricDataType.String, softwareVersion).createMetric());
			spbPayload.addMetric(
					new MetricBuilder(APPLICATION_NAME, MetricDataType.String, applicationName).createMetric());

			// Create and publish an MQTT message
			String topic = namespace + "/" + groupID + "/" + NBIRTH + "/" + nodeID;

			if (compressedMessages) {
				mqttClient.publish(topic,
						new MqttMessage(encoder.getBytes(PayloadUtil.compress(spbPayload, compressionAlgorithm))));
			} else {
				mqttClient.publish(topic, new MqttMessage(encoder.getBytes(spbPayload)));
			}
		} catch (Exception e) {
			_logger.error(e.getMessage());
		}
	}

	// Generate a unique ID for birth / death certificates
	private String newUUID() {
		return java.util.UUID.randomUUID().toString();
	}

	// Used to add the sequence number for birth / death certificates
	private long getSeqNum() {
		if (seq == 256) {
			seq = 0;
		}
		return seq++;
	}

	// Used to add the bd sequence number for birth / death certificates
	private SparkplugBPayload addBdSeqNum(SparkplugBPayload payload) throws Exception {
		if (bdSeq == 256) {
			bdSeq = 0;
		}
		payload.addMetric(new MetricBuilder(BD_SEQ, MetricDataType.Int64, (long) bdSeq).createMetric());
		bdSeq++;
		return payload;
	}

	/***********************************************************/
	/*** The following methods are remote services that can  ***/
	/*** be called from the ThingWorx platform:              ***/
	/*** - SendThingProperties                               ***/
	/***********************************************************/

	@ThingworxServiceDefinition(name = "SendThingProperties", description = "", category = "", isAllowOverride = false, aspects = {
			"isAsync:false" })
	@ThingworxServiceResult(name = "result", description = "", baseType = "STRING", aspects = {})
	public String SendThingProperties(
			@ThingworxServiceParameter(name = "ThingName", description = "", baseType = "THINGNAME") String ThingName) {

		_logger.trace("Entering Service: SendThingProperties");
		String result = null;

		try {
			// Create a new Sparkplug B payload
			SparkplugBPayload spbPayload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(),
					newUUID(), null);

			// Get Thing
			InfoTable properties = this.getClient().readProperties(ThingworxEntityTypes.Things, ThingName,
					valueUpdateTimeout);
			if (properties == null) {
				_logger.warn("Cannot find properties for Thing " + ThingName);
			} else {
				// Create Sparkplugb payload - include name of this Thing
				spbPayload.addMetric(new MetricBuilder(NAME, MetricDataType.String, ThingName).createMetric());

				// Create Sparkplugb payload - include metric properties on this Thing
				ValueCollection vc = properties.getFirstRow();
				for (String propertyName : vc.keySet()) {
					try {
						IPrimitiveType propertyType = vc.getPrimitive(propertyName);

						// Create a new metric
						boolean sendMetric = false;
						MetricDataType metricDataType = MetricDataType.String;
						if (propertyType instanceof BooleanPrimitive) {
							metricDataType = MetricDataType.Boolean;
							sendMetric = true;
						} else if (propertyType instanceof NumberPrimitive) {
							metricDataType = MetricDataType.Double;
							sendMetric = true;
						} else if (propertyType instanceof IntegerPrimitive) {
							metricDataType = MetricDataType.Int32;
							sendMetric = true;
						}
						if (sendMetric)
							spbPayload.addMetric(
									new MetricBuilder(propertyName, metricDataType, vc.getValue(propertyName))
											.createMetric());
					} catch (Exception e) {
						_logger.warn("Issue getting Thing property value");
					}
				}

				// Send the message to the Namespace
				String topic = namespace + "/" + groupID + "/" + NDATA + "/" + nodeID;

				if (compressedMessages) {
					mqttClient.publish(topic,
							new MqttMessage(encoder.getBytes(PayloadUtil.compress(spbPayload, compressionAlgorithm))));
				} else {
					mqttClient.publish(topic, new MqttMessage(encoder.getBytes(spbPayload)));
				}

				result = OK;
			}

		} catch (Exception e) {
			_logger.error(e.getMessage());
		}

		_logger.trace("Exiting Service: SendThingProperties");
		return result;
	}

}
