# SparkplugB-Edge-SDK
A ThingWorx Java Edge SDK component for integrating ThingWorx with Unified Namespace / SparkplugB architectures.

# Overview
This is a field-developed ThingWorx edge SDK component for integrating to a Unified Namespace (UNS) architecture using MQTT and SparkplugB technologies.

This is not core PTC product developed by PTC R&D and must be treated as a field customisation.

The intent is for ThingWorx to integrate to UNS architectures both as an Industrial IIoT application development environment to consume from the UNS, and as a node to contribute new data to the UNS (e.g. OEE metrics).

The edge component connects to ThingWorx as a remote "Thing", with UNS topics published to JSON payload properties on the remote "Thing". This "Thing" also has a remote service to publish ThingWorx information back onto the UNS.

The extension has been developed using the ThingWorx Java Edge SDK and Eclipse MQTT / SparkplugB libraries for encoding / decoding SparkplugB messages. Source code can be found in the 'src' directory [here](src/ptcsc/sparkplugbedge).

![sparkplugb edge agent architecture](https://github.com/user-attachments/assets/985ed102-f542-498c-8f84-f058f4a86d17)

# Installation
Download the files [SparkplugBEdgeAgent.jar](SparkplugBEdgeAgent.jar) and [SparkplugBEdge.json](SparkplugBEdge.json) to the same directory. The JAR file is the edge SDK component itself, the JSON file configures the edge component to integrate to the UNS and ThingWorx.

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
- **QoS**: Standard MQTT client setting, identified Quality of Service that this edge component will use in MQTT exchanges.
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
        	"Topic": "spBv1.0/Plant A/DDATA/Production Line 1/PLC 1",
        	"Property": "Plant_A_Production_Line_1_PLC_1_Payload"
        },
  	{
		"Topic": "spBv1.0/Plant A/DDATA/Production Line 1/PLC 2",
        	"Property": "Plant_A_Production_Line_1_PLC_2_Payload"
        },
	{
		"Topic": "spBv1.0/+/DBIRTH/+/+",
		"Property": "DBIRTH"
	}
 ],
```

>The entries in the "Property" field MUST be created as properties on the remote "Thing" within ThingWorx Composer, with the “Base Type” set to JSON.
>There is no requirement to make these 'persistable' or 'logged'; see later for subscription code to unwrap these payloads.
>Both '+' (single) and '#' (multiple) wildcards are allowed as defined in the MQTT standard for topic subscriptions.

With such mappings in place, the "metrics" published as SparkplugB-encoded messages on the MQTT broker will be decoded and stored in ThingWorx as JSON-formatted data that can be subsequently parsed out onto "Thing" properties within the ThingWorx model. The topic name is also ingested into the JSON payload stored on the "Thing" property:

![image](https://github.com/user-attachments/assets/483e0d14-3cc1-4b2a-83e5-a71c22bc66b4)

## Configuration - ApplicationDetails
This section identifies information populated in the Node Birth certificate issued by ThingWorx when connecting to the Namespace if the previous setting **PublishBirthDeathCertificates** is set to true:
- **Vendor**: Name of the software vendor for ThingWorx, default is "PTC".
- **SoftwareVersion**: Version of ThingWorx deployed.
- **ApplicationName**: Default to "ThingWorx"

# Run the Edge SDK component
Create an application launch file (e.g. "run.bat" on Windows platforms) in the same directory that the edge JAR and json files were downloaded to. In this file, specify the following:
>java -jar ./SparkplugBEdgeAgent.jar ./SparkplugBEdge.json

Then execute the file - you should see the edge component connect to both the MQTT broker and ThingWorx in it's logging output.

# Configure ThingWorx Platform
This section identifies additional configuration within ThingWorx Composer to expose the UNS to the ThingWorx "Thing Model".

## Create Remote "Thing"
1. Create a "Thing" in Composer using the Thing Template **RemoteThing** (or a Thing Template that inherits **RemoteThing**).
2. Set it's "Identifier" to match what was entered in the **RemoteThingName** JSON configuration entry. If the edge component is connected, you should be able to find this using the pencil icon in the "Identifier" field.

![remote thing](https://github.com/user-attachments/assets/d941a6e7-c8ed-4d0f-ac40-af6d793aa1ba)

## Processing JSON Message Content
1. Create JSON properties on the "Remote Thing" to match what was defined in the **PropertyMappings** JSON configuration entry

![image](https://github.com/user-attachments/assets/421453f6-9b11-4748-b997-2c9ab98a770e)

2. Other “Things” can be created in ThingWorx to store the individual properties that are contained in the JSON payloads saved on the "Remote Thing" connected to the edge component. These other Things can subscribe to "Data Change" events on the JSON payload properties to parse the JSON and store the individual values. See example below:

![subscription](https://github.com/user-attachments/assets/9b36b65f-b8f1-4686-ba4d-31384aead84a)

For reference, the example subscription code above is given below for easier copy / paste:

```javascript
// Get metrics from UNS Payload
let metrics = events["UNSGateway_DataChange_PLC_1_Payload"].eventData.newValue.value.metrics;

//Create infotable for property updates with timestamps from edge
let propertyUpdates = Resources["InfoTableFunctions"].CreateInfoTableFromDataShape({
    infoTableName: "InfoTable",
    dataShapeName: "NamedVTQ"
});

// Parse metrics
metrics.forEach(row => {
    if (me[row.name] != undefined) { // if metric is defined as a property on this Thing
        // insert updates to infotable
        propertyUpdates.AddRow({
            name: row.name, 
            time: row.timestamp, 
            value: row.value 
        });
    }
});

// Persist updates
me.UpdatePropertyValues({
    values: propertyUpdates
});
```

# Sending messages to the Unified Namespace
The remote Thing provides a **SendThingProperties** service to send data derived by ThingWorx back onto the UNS. This takes 1 input parameter:
- ThingName – the name of the Thing you wish to report back to the UNS.

This will send all properties on the specified Thing back to the namespace on the topic structure identified by the UNS Thing’s SparkplugB configuration. Select “Browse Remote Services” to bind the edge service to the platform "Thing":

![remote service](https://github.com/user-attachments/assets/b5872312-45cc-457c-a701-3427cb537b7b)

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
