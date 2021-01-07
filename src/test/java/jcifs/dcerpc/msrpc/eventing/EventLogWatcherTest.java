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

import jcifs.dcerpc.msrpc.eventing.EventLogQuery.PathType;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/*
 * @author Jitendra Kotamraju
 */
public class EventLogWatcherTest {

    private static volatile Exception exception;

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("java EventingTest properties-file");
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

        EventLogSession session = new EventLogSession(hostname, domain, user, password);

        String xpath = xpath();
        EventLogQuery query = new EventLogQuery("Security", PathType.LogName, xpath, session, false);

        try(EventLogWatcher watcher = new EventLogWatcher(query, EventLogWatcherTest::onEvents)) {
            watcher.start();
            while (true) {
                if (exception != null) {
                    throw exception;
                } else {
                    TimeUnit.SECONDS.sleep(5);
                }
            }
        }
    }

    private static String xpath() {
        return "*[System[EventID=4624 or EventID=4634]]";
    }

    private static String xpathFromFile() throws Exception {
        URL resource = EventLogWatcherTest.class.getResource("events-xpath.xml");
        Path path = Paths.get(resource.toURI());
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    private static void onEvents(List<EventRecord> events) {
        for(EventRecord record : events) {
            if (record.exception != null) {
                exception = record.exception;
                return;
            }
            System.out.println(LocalTime.now() + " Received event = " + record);
            System.out.println("\t" + record.event() + "\n");
        }
    }

}