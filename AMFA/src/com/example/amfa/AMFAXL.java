package com.example.amfa;

import org.jivesoftware.smack.XMPPConnection;

public class AMFAXL {

	private XMPPConnection connection = null;

	private static AMFAXL instance = null;

	public synchronized static AMFAXL getInstance() {
		if (instance == null) {
			instance = new AMFAXL();
		}
		return instance;
	}

	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
	}

	public XMPPConnection getConnection() {
		return this.connection;
	}

}