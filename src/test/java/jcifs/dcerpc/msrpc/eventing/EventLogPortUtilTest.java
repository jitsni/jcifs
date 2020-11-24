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
public class EventLogPortUtilTest {

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java EventLogPortUtilTest properties-file");
            return;
        }
        Properties properties = new Properties();
        try(InputStream in = new FileInputStream(args[0])) {
            properties.load(in);
        }

        String hostname = properties.getProperty("hostname");
        int connectTimeout = 5000;
        int readTimeout = 3000;

        EventLogPortUtil util = new EventLogPortUtil(hostname);
        int port = util.getPort(connectTimeout, readTimeout);
        System.out.println(port);
    }

}