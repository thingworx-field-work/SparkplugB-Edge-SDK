# SparkplugB-Edge-SDK
A ThingWorx Java Edge SDK component for integrating ThingWorx with Unified Namespace / SparkplugB architectures.

# Overview
This is a field-developed ThingWorx edge SDK component for integrating to a Unified Namespace (UNS) architecture using MQTT and SparkplugB technologies.

This is not core PTC product developed by PTC R&D and must be treated as a field customisation.

The intent is for ThingWorx to integrate to UNS architectures both as an Industrial IIoT application development environment to consume from the UNS, and as a node to contribute new data to the UNS (e.g. OEE metrics).

The edge component connects to ThingWorx as a remote "Thing", with UNS topics published to JSON payload properties on the remote "Thing". This "Thing" akso has a remote service to publish ThingWorx information back onto the UNS.

The extension has been developed using the ThingWorx Java Edge SDK and Eclipse MQTT / SparkplugB libraries for encoding / decoding SparkplugB messages. Source code can be found in the 'src' directory [here](src/ptcsc/sparkplugbedge).

![sparkplugb edge agent architecture](https://github.com/user-attachments/assets/985ed102-f542-498c-8f84-f058f4a86d17)

# Installation
Download the files [SparkplugBEdgeAgent.jar](SparkplugBEdgeAgent.jar) and [SparkplugBEdge.json](SparkplugBEdge.json). The JAR file is the edge SDK component itself, the JSON file configures the edge component to integrate to the UNS and ThingWorx.

# Configuration
The edge component is configured by editing the [SparkplugBEdge.json](SparkplugBEdge.json) file downloaded. This has several sections:
- **ThingWorxConnectInfo**: Connection to ThingWorx
- **SparkplugBInfo**: SparkplugB namespace / group / node IDs and behaviour
- **MQTTConnectInfo**: Connection to the MQTT broker
- **PropertyMappings**: MQTT topic subscriptions for this remote "Thing" in ThingWorx
- **ApplicationDetails**: ThngWorx details published in SparkplugB birth / death certificates onto the UNS

## Configuration - ThingWorxConnectInfo
This section defines the following parameters:
- **ServerURL**: The URL for the ThingWorx server that the edge component connects to e.g. "ws://localhost:8080/Thingworx/WS". Change ‘ws:’ to ‘wss:’ for ThingWorx environments that have a server certificate.
- **AppKey**: Generate an Application Key in ThingWorx Composer and copy the generated key value here.
- **RemoteThingName**: This is the name of the remote “Thing” that will be recognised by ThingWorx.
- **ValueUpdateTimeout**: The timeout value in seconds for ThingWorx property value updates.

## Configuration - SparkplugBInfo
This section defines the following parameters:
- **Namespace**: Set this to "spBv1.0" for SparkplugB specification.
- **GroupID**: Identify name of the logical grouping that ThingWorx will appear in within the MQTT topic structure on the UNS.
- **NodeID**: Identifies the "Edge of Network" node name that ThingWorx will appear in within the MQTT topic structure on the UNS.
- **CompressedMessages**: Set to true or false - specify whether message exchanges with the MQTT broker will use GZIP compression.
- **PublishBirthDeathCertificates**: Set to true or false - specify whether ThingWorx will publish SparkplugB birth / death certificates to the MQTT broker.

## Configuration - MQTTConnectInfo
This section defines the following parameters:
- **Hostname**: Hostname of the MQTT broker.
- **Port**: TCP port that the MQTT broker runs on, typically 8883 for brokers running SSL, 1883 otherwise.
- **Username**: Username to connect to the MQTT broker if required.
- **Password**: Password to connect to the MQTT broker if required.
- **ClientID**: How ThingWorx will identify itself to the MQTT broker.
- **UsingEncryption**: Set to true or false - specify whether the connection to the MQTT broker will use SSL encryption.
- **ValidateCertificate**: Set to true or false - set to “true” if the edge agent needs to validate the MQTT broker certificate for SSL-enabled brokers. In this case, create a JKS keystore and import the MQTT broker certificate into the keystore as a “Trusted Certificate”.
- **TrustStorePath**: If validating the MQTT broker certificate, the path to the JKS store identified above.
- **TrustStorePassword**: If validating the MQTT broker certificate, the password to access the JKS store identified above.
- **RetainedMessages**: Standard MQTT client setting, set to true or false.
- **QoS**: Standard MQTT client setting, identified Qulaity of Service that this edge component will use in MQTT exchanges.
- **AutomaticReconnect**: Standard MQTT client setting, set to true or false.
- **CleanSession**: Standard MQTT client setting, set to true or false.
- **ConnectionTimeout**: Standard MQTT client setting, specified in seconds.
- **KeepAliveInterval**: Standard MQTT client setting, specified in seconds.
- **TimeToWait**: Standard MQTT client setting, specified in seconds.

## Configuration - PropertyMappings
This section identifies which topics this remote "Thing" will subscribe to, and which properties on this "Thing" the resulting JSON payloads will be stored on once the extension has decoded the SparkplugB messages. These property mappings are specified as an array of Topic / Property couplings such as:

```json
"PropertyMappings": [
	{
		"Topic": "spBv1.0/PTC1/DDATA/SC/Sparkplug Device 1",
		"Property": "PTC1_SC_SP_Device_1_Payload"
	},
	{
		"Topic": "spBv1.0/PTC1/DDATA/SC/Sparkplug Device 2",
		"Property": "PTC1_SC_SP_Device_2_Payload"
	},
	{
		"Topic": "spBv1.0/+/DBIRTH/+/+",
		"Property": "DBIRTH"
	}
 ],
```
The entries in the "Property" field MUST be created as properties on the remote "Thing" within ThingWorx Composer, with the “Base Type” set to JSON. There is no requirement to make these 'persistable' or 'logged'; see later for subscription code to unwrap these payloads. Both '+' (single) and '#' (multiple) wildcards are allowed as defined in the MQTT standard for topic subscriptions.

## Configuration - ApplicationDetails
This section identifies information populated in the Node Birth certificate issued by ThingWorx when connecting to the Namespace if the previous setting **PublishBirthDeathCertificates** is set to true:
- **Vendor**: Name of the software vendor for ThingWorx, default is "PTC".
- **SoftwareVersion**: Version of ThingWorx deployed.
- **ApplicationName**: Default to "ThingWorx"

# Keywords
ThingWorx SparkplugB UNS Unified Namespace 

# Authors
Roy Clarke

# Disclaimer
By downloading this software, the user acknowledges that it is unsupported, not reviewed for security purposes, and that the user assumes all risk for running it.

Users accept all risk whatsoever regarding the security of the code they download.

This software is not an official PTC product and is not officially supported by PTC.

PTC is not responsible for any maintenance for this software.

PTC will not accept technical support cases logged related to this Software.

This source code is offered freely and AS IS without any warranty.

The author of this code cannot be held accountable for the well-functioning of it.

The author shared the code that worked at a specific moment in time using specific versions of PTC products at that time, without the intention to make the code compliant with past, current or future versions of those PTC products.

The author has not committed to maintain this code and he may not be bound to maintain or fix it.

# License
I accept the MIT License (https://opensource.org/licenses/MIT) and agree that any software downloaded/utilized will be in compliance with that Agreement. However, despite anything to the contrary in the License Agreement, I agree as follows:

I acknowledge that I am not entitled to support assistance with respect to the software, and PTC will have no obligation to maintain the software or provide bug fixes or security patches or new releases.

The software is provided “As Is” and with no warranty, indemnitees or guarantees whatsoever, and PTC will have no liability whatsoever with respect to the software, including with respect to any intellectual property infringement claims or security incidents or data loss.
