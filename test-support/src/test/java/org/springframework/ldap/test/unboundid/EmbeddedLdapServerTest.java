package org.springframework.ldap.test.unboundid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

public class EmbeddedLdapServerTest {

	@Test
	public void shouldStartAndCloseServer() throws Exception {
		int port = getFreePort();
		assertFalse(isPortOpen(port));

		EmbeddedLdapServer server = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se", port);
		assertTrue(isPortOpen(port));

		server.close();
		assertFalse(isPortOpen(port));
	}

	@Test
	public void shouldStartAndAutoCloseServer() throws Exception {
		int port = getFreePort();
		assertFalse(isPortOpen(port));

		try (EmbeddedLdapServer ignored = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se", port)) {
			assertTrue(isPortOpen(port));
		}
		assertFalse(isPortOpen(port));
	}

	@Test
	public void shouldStartAndCloseServerViaLdapTestUtils() throws Exception {
		int port = getFreePort();
		assertFalse(isPortOpen(port));

		LdapTestUtils.startEmbeddedServer(port, "dc=jayway,dc=se", "jayway");
		assertTrue(isPortOpen(port));

		LdapTestUtils.shutdownEmbeddedServer();
		assertFalse(isPortOpen(port));
	}

	static boolean isPortOpen(int port) {
		try (Socket ignored = new Socket("localhost", port)) {
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	static int getFreePort() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
	}

}