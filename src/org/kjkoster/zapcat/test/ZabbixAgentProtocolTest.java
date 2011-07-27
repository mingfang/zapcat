package org.kjkoster.zapcat.test;

/* This file is part of Zapcat.
 *
 * Zapcat is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Zapcat is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zapcat. If not, see <http://www.gnu.org/licenses/>.
 */

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Test;
import org.kjkoster.zapcat.Agent;
import org.kjkoster.zapcat.zabbix.ZabbixAgent;
import org.springframework.jmx.export.MBeanExporter;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * A test case to test that the protocol configuration works.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ZabbixAgentProtocolTest {
    static{
        BasicConfigurator.configure();
    }

    final Properties originalProperties = (Properties) System.getProperties()
            .clone();

    /**
     * Sleep a little, to give the agent time to die. Restore the system
     * properties and check that we have not settings screwing up our
     * experiments.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @After
    public void tearDown() throws Exception {
        Thread.sleep(100);

        System.setProperties(originalProperties);
        assertNull(originalProperties
                .getProperty(ZabbixAgent.PROTOCOL_PROPERTY));
        assertNull(originalProperties.get(ZabbixAgent.PROTOCOL_PROPERTY));
        assertNull(System.getProperty(ZabbixAgent.PROTOCOL_PROPERTY));
    }

    /**
     * Test that by default we have protocol version 1.4.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void testDefault() throws Exception {
        final Agent agent = new ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("system.property[java.version]\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        in.read(buffer);

        assertEquals('Z', buffer[0]);
        assertEquals('B', buffer[1]);
        assertEquals('X', buffer[2]);
        assertEquals('D', buffer[3]);
        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }

    /**
     * Test that we can use a Java system property to configure the protocol
     * version on the agent.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void testSetTo14() throws Exception {
        System.setProperty(ZabbixAgent.PROTOCOL_PROPERTY, "1.4");
        assertEquals("1.4", System.getProperty(ZabbixAgent.PROTOCOL_PROPERTY));

        testDefault();
    }

    /**
     * Test that we can use a Java system property to configure the protocol
     * version on the agent.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void testSetTo11() throws Exception {
        System.setProperty(ZabbixAgent.PROTOCOL_PROPERTY, "1.1");
        assertEquals("1.1", System.getProperty(ZabbixAgent.PROTOCOL_PROPERTY));

        final Agent agent = new ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("system.property[java.version]\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        in.read(buffer);

        final String version = System.getProperty("java.version");

        assertEquals(version.charAt(0), buffer[0]);
        assertEquals(version.charAt(1), buffer[1]);
        assertEquals(version.charAt(2), buffer[2]);
        assertEquals(version.charAt(3), buffer[3]);
        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }

    /**
     * Test the we can ping the agent.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void testPing() throws Exception {
        final Agent agent = new org.kjkoster.zapcat.zabbix.ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                org.kjkoster.zapcat.zabbix.ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("agent.ping\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        final int read = in.read(buffer);
        assertEquals(14, read);

        assertEquals('Z', buffer[0]);
        assertEquals('B', buffer[1]);
        assertEquals('X', buffer[2]);
        assertEquals('D', buffer[3]);

        assertEquals('1', buffer[13]);

        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }

    /**
     * Test robustness.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void testMissingArgument() throws Exception {
        final Agent agent = new org.kjkoster.zapcat.zabbix.ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                org.kjkoster.zapcat.zabbix.ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("jmx\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        final int read = in.read(buffer);
        assertEquals(29, read);

        assertEquals('Z', buffer[0]);
        assertEquals('B', buffer[1]);
        assertEquals('X', buffer[2]);
        assertEquals('D', buffer[3]);

        assertEquals('N', buffer[17]);
        assertEquals('O', buffer[18]);
        assertEquals('T', buffer[19]);

        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }

    /**
     * Test robustness.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void testMissingOpen() throws Exception {
        final Agent agent = new org.kjkoster.zapcat.zabbix.ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                org.kjkoster.zapcat.zabbix.ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("jmx(foo]\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        final int read = in.read(buffer);
        assertEquals(29, read);

        assertEquals('Z', buffer[0]);
        assertEquals('B', buffer[1]);
        assertEquals('X', buffer[2]);
        assertEquals('D', buffer[3]);

        assertEquals('N', buffer[17]);
        assertEquals('O', buffer[18]);
        assertEquals('T', buffer[19]);

        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }

    /**
     * Test robustness.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void testMissingClose() throws Exception {
        final Agent agent = new org.kjkoster.zapcat.zabbix.ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                org.kjkoster.zapcat.zabbix.ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("jmx[foo\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        final int read = in.read(buffer);
        assertEquals(29, read);

        assertEquals('Z', buffer[0]);
        assertEquals('B', buffer[1]);
        assertEquals('X', buffer[2]);
        assertEquals('D', buffer[3]);

        assertEquals('N', buffer[17]);
        assertEquals('O', buffer[18]);
        assertEquals('T', buffer[19]);

        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }
    @Test
    public void testNewKeys() throws Exception {
        final Agent agent = new org.kjkoster.zapcat.zabbix.ZabbixAgent(InetAddress.getLocalHost(), 1099);
        // give the agent some time to open the port
        Thread.sleep(100);
        query(1099, "jmx[\"jmx[\"org.kjkoster.zapcat:type=Agent,port=1099\",\"Port\"]", "1099".getBytes());
        agent.stop();
    }

    private void query(int port, String key, byte[] expected) throws IOException {
        final Socket socket = new Socket(InetAddress.getLocalHost(), port);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write(key);
        out.write('\n');
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        in.read(buffer);
        socket.close();
        System.out.println(new String(buffer));
        assertEquals('Z', buffer[0]);
        assertEquals('B', buffer[1]);
        assertEquals('X', buffer[2]);
        assertEquals('D', buffer[3]);
        assertEquals((byte) 0x01, buffer[4]);
        byte[] actual = Arrays.copyOfRange(buffer, 13, 13 + expected.length);
        assertArrayEquals(expected, actual);
//        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    public void testMultiAgent() throws Exception {
        ZabbixAgent agent1;
        {
            MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
            agent1 = new ZabbixAgent(InetAddress.getLocalHost(), 1098);
            agent1.setMbeanServer(mbeanServer);

            MBeanExporter exporter = new MBeanExporter();
            exporter.setServer(agent1.getMbeanServer());

            Monitor monitor = MonitorFactory.getMonitor("monitor1", "");
            monitor.setHits(1);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("test:name=monitor", monitor);
            exporter.setBeans(map);

            exporter.afterPropertiesSet();
        }
        ZabbixAgent agent2;
        {
            MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
            agent2 = new ZabbixAgent(InetAddress.getLocalHost(), 1099);
            agent2.setMbeanServer(mbeanServer);

            MBeanExporter exporter = new MBeanExporter();
            exporter.setServer(agent2.getMbeanServer());

            Monitor monitor = MonitorFactory.getMonitor("monitor2", "");
            monitor.setHits(2);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("test:name=monitor", monitor);
            exporter.setBeans(map);

            exporter.afterPropertiesSet();
        }
        
        Thread.sleep(1000);
        query(1098, "jmx[\"test:name=monitor\",\"Hits\"]", new byte[]{'1'});
        query(1099, "jmx[\"test:name=monitor\",\"Hits\"]", new byte[]{'2'});

        agent1.stop();
        agent2.stop();
    }

    @Test
    public void testAgentVersion() throws Exception {
        final Agent agent = new org.kjkoster.zapcat.zabbix.ZabbixAgent(InetAddress.getLocalHost(), 1099);
        // give the agent some time to open the port
        Thread.sleep(100);
        query(1099, "agent.version", "zapcat 1.4".getBytes());
        agent.stop();

    }
}
