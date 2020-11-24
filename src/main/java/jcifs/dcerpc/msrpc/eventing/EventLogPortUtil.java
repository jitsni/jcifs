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

import jcifs.dcerpc.DcerpcTcpHandle;

import java.io.IOException;

public class EventLogPortUtil {

    private final String server;

    public EventLogPortUtil(String server) {
        this.server = server;
    }

    public int getPort(int connectTimeout, int readTimeout) {
        try (DcerpcTcpHandle ehandle = new DcerpcTcpHandle(server, 135, "even6")) {
            ehandle.setConnectTimeout(connectTimeout);
            ehandle.setSoTimeout(readTimeout);
            ehandle.epmBind();
            return ehandle.getPort();
        } catch (IOException ioe) {
            return -1;
        }
    }

}
