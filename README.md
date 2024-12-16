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

## Configuration
The edge component is configured by editing the [SparkplugBEdge.json](SparkplugBEdge.json) file downloaded. This has several sections:
- ThingWorxConnectInfo: Connection to ThingWorx
- SparkplugBInfo: SparkplugB namespace / group / node IDs and behaviour
- MQTTConnectInfo: Connection to the MQTT broker
- PropertyMappings: MQTT topic subscriptions for this remote "Thing" in ThingWorx
- ApplicationDetails: ThngWorx details published in SparkplugB birth / death certificates onto the UNS

### Configuration - ThingWorxConnectInfo
This section defines the following parameters:
- ServerURL: The URL for the ThingWorx server that the edge component connects to e.g. "ws://localhost:8080/Thingworx/WS". Change ‘ws:’ to ‘wss:’ for ThingWorx environments that have a server certificate.
- AppKey: Generate an Application Key in ThingWorx Composer and copy the generated key value here.
- RemoteThingName: This is the name of the remote “Thing” that will be recognised by ThingWorx.
- ValueUpdateTimeout: The timeout value in seconds for ThingWorx property value updates.

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
