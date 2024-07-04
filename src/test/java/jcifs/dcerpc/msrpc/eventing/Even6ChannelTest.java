/**
 * Copyright 2024 Jitendra Kotamraju.
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

import jcifs.dcerpc.Auth3;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.msrpc.NtlmSecurityProvider;
import jcifs.smb.NtlmPasswordAuthentication;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * @author Jitendra Kotamraju
 */
public class Even6ChannelTest {

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

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, password);
        DcerpcHandle rpcHandle = DcerpcHandle.getHandle("ncacn_ip_tcp:" + hostname +"[even6]", auth);
        rpcHandle.setDcerpcSecurityProvider(new NtlmSecurityProvider(auth, false));
        rpcHandle.bind();

        DcerpcMessage auth3 = new Auth3();
        rpcHandle.send(auth3);

        channelList(rpcHandle);
    }

    private static void channelList(DcerpcHandle rpcHandle) throws IOException {
        even6.EvtRpcGetChannelList channelList = new even6.EvtRpcGetChannelList();
        rpcHandle.sendrecv(channelList);
        assert channelList.retVal == 0;

        assert channelList.error.m_error == 0;
        assert channelList.error.m_subErr == 0;
        assert channelList.error.m_subErrParam == 0;
        for(String channel : channelList.channelList) {
            System.out.println(channel);
        }
    }
}