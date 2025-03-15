package org.springframework.ldap.test.unboundid;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class SocketTestUtils {

	static boolean isPortOpen(int port) {
		try (Socket ignored = new Socket("localhost", port)) {
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	static int getFreePort() {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
		catch (IOException ex) {
			throw new RuntimeException("Could not get a free port", ex);
		}
	}

}
