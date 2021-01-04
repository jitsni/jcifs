/**
 * Copyright 2020 Jitendra Kotamraju.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcifs.dcerpc.msrpc.eventing;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
 * @author Jitendra Kotamraju
 */
public class EventLogConnectionUtilTest {

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java EventLogUtilTest properties-file");
            return;
        }
        Properties properties = new Properties();
        try(InputStream in = new FileInputStream(args[0])) {
            properties.load(in);
        }

        String hostname = properties.getProperty("hostname");
        String domain = properties.getProperty("domain");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");

        int connectTimeout = 5000;
        int epmTimeout = 3000;
        int pullTimeout = 3000;
        int waitTimeout = 3000;
        int totalTimeout = 7000;

        EventLogPortUtil portUtil = new EventLogPortUtil(hostname);
        int port = portUtil.getPort(connectTimeout, epmTimeout);
        System.out.println(String.format("%s even6 epm port = %d", hostname, port));

        try (EventLogConnectionUtil util = new EventLogConnectionUtil(hostname, port, domain, user, password, "Security")) {
            util.setConnectTimeout(connectTimeout);
            util.setEpmTimeout(epmTimeout);
            util.setPullTimeout(pullTimeout);
            util.setWaitTimeout(waitTimeout);
            util.setRequestedRecords(1);

            EventLogConnectionUtil.ConnectionStatus connectionStatus = util.testConnection(totalTimeout, MILLISECONDS);
            System.out.println(connectionStatus);
        }
    }

}