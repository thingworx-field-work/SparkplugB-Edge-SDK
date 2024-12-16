package ptcsc.sparkplugbedge;

import com.thingworx.communications.client.IPasswordCallback;

// callback to return application key for authentication

public class EdgeDBPasswordCallback implements IPasswordCallback {
	private String appKey = null;

	public EdgeDBPasswordCallback(String appKey) {
		this.appKey = appKey;
	}

	@Override
	public char[] getSecret() {
		return appKey.toCharArray();
	}

}
