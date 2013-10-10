package org.springframework.ldap.odm.test.utils;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Added because the close down of the embedded Apache DS used
// for unit testing does not seem to free up its port.
public class GetFreePort {
    private static Logger LOG=LoggerFactory.getLogger(GetFreePort.class);
    
    public static int getFreePort()
    throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        
        LOG.debug(String.format("Port number: %1$s", port));
        
        return port;
    }
}
