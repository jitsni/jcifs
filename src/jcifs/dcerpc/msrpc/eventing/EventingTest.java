package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.Auth3;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.msrpc.NtlmSecurityProvider;
import jcifs.smb.NtlmPasswordAuthentication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/*
 * @author Jitendra Kotamraju
 */
public class EventingTest {

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("java EventingTest properties-file");
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
        DcerpcHandle rpcHandle = DcerpcHandle.getHandle("ncacn_ip_tcp:" + hostname +"[mseven6]", auth);
        rpcHandle.setDcerpcSecurityProvider(new NtlmSecurityProvider(auth));
        rpcHandle.bind();

        DcerpcMessage auth3 = new Auth3();
        rpcHandle.send(auth3);

        String xpath = "*[System[EventID=4624 or EventID=4634]]";

//        even6.EvtRpcRegisterLogQuery logQuery = new even6.EvtRpcRegisterLogQuery(
//                "Security", xpath, even6.EvtQueryChannelName | even6.EvtReadNewestToOldest);
//        rpcHandle.sendrecv(logQuery);
//
//        even6.EvtRpcQueryNext results = new even6.EvtRpcQueryNext(
//                logQuery.handle, 5, 1000, 0);
//        rpcHandle.sendrecv(results);
//        System.out.println(results);

//        even6.EvtRpcGetChannelList channelList = new even6.EvtRpcGetChannelList(0);
//        rpcHandle.sendrecv(channelList);

        String bookmark = null;
//                "<BookmarkList>" +
//                "<Bookmark Channel=\"Security\" RecordId=\"2004\" IsCurrent=\"True\"/>" +
//                "</BookmarkList>";
        even6.EvtRpcRegisterRemoteSubscription subscription = new even6.EvtRpcRegisterRemoteSubscription(
                "Security", xpath, bookmark, even6.EvtSubscribeStartAtOldestRecord);
        rpcHandle.sendrecv(subscription);

        //while(true) {
            even6.EvtRpcRemoteSubscriptionNextAsync async = new even6.EvtRpcRemoteSubscriptionNextAsync(
                    subscription.handle, 5, 0);
            rpcHandle.sendrecv(async);
            System.out.println(async);
            for(int i=0; i < async.numActualRecords; i++) {
                EventRecord record = new EventRecord(async.resultBuffer, async.eventDataIndices[i], async.eventDataSizes[i]);
                System.out.println("\t" + record);

//                try(OutputStream out = new FileOutputStream("/tmp/binxml-" + i + ".xml")) {
//                    out.write(async.resultBuffer, record.binXmlOffset(), record.binXmlSize);
//                }
            }

        //}

    }
}