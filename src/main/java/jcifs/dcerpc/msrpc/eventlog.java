package jcifs.dcerpc.msrpc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.rpc;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;

public class eventlog {

   public static String getSyntax() {
       return "82273fdc-e32a-18c3-3f78-827929dc23ea:0.0";
   }

   public static final int EVENTLOG_SEQUENTIAL_READ = 0x0001;

   public static final int EVENTLOG_SEEK_READ = 0x0002;

   public static final int EVENTLOG_FORWARDS_READ = 0x0004;

   public static final int EVENTLOG_BACKWARDS_READ = 0x0008;

   public static final int EVENTLOG_SUCCESS = 0x0000;

   public static final int EVENTLOG_ERROR_TYPE = 0x0001;

   public static final int EVENTLOG_WARNING_TYPE = 0x0002;

   public static final int EVENTLOG_INFORMATION_TYPE = 0x0004;

   public static final int EVENTLOG_AUDIT_SUCCESS = 0x0008;

   public static final int EVENTLOG_AUDIT_FAILURE = 0x0010;

   public static class EventlogOpenUnknown0 extends NdrObject {

       public short unknown0;

       public short unknown1;

       public void encode(NdrBuffer _dst) throws NdrException {
           _dst.align(4);
           _dst.enc_ndr_short(unknown0);
           _dst.enc_ndr_short(unknown1);
       }

       public void decode(NdrBuffer _src) throws NdrException {
           _src.align(4);
           unknown0 = (short) _src.dec_ndr_short();
           unknown1 = (short) _src.dec_ndr_short();
       }
   }

   public static class sid_t extends NdrObject {

       public byte revision;

       public byte[] identifier_authority;

       public int[] sub_authority;

       public void encode(NdrBuffer _dst) throws NdrException {
           _dst.enc_ndr_small(revision);
           _dst.enc_ndr_small(sub_authority.length);
           for (int i = 0; i < 6; i++) {
               _dst.enc_ndr_small(identifier_authority[i]);
           }
           for (int i = 0; i < sub_authority.length; i++) {
               _dst.enc_ndr_long_noalign(sub_authority[i]);
           }
       }

       public void decode(NdrBuffer _src) throws NdrException {
           revision = (byte) _src.dec_ndr_small();
           int sub_authority_count = (byte) _src.dec_ndr_small();

           identifier_authority = new byte[6];
           for (int i = 0; i < 6; i++) {
               identifier_authority[i] = (byte) _src.dec_ndr_small();
           }
           sub_authority = new int[sub_authority_count];
           for (int i = 0; i < sub_authority_count; i++) {
               sub_authority[i] = (int) _src.dec_ndr_long_noalign();
           }
       }

       public String toString() {
           StringBuffer sb = new StringBuffer();
           long ia;
           ia = (identifier_authority[5]) + (identifier_authority[4] << 8)
                   + (identifier_authority[3] << 16)
                   + (identifier_authority[2] << 24);
           sb.append("S-").append(revision).append("-").append(ia);
           for (int i = 0; i < sub_authority.length; i++) {
               sb.append("-").append(sub_authority[i]);
           }
           return sb.toString();
       }
   }

   public static class EventlogRecord extends NdrObject {
       public int size;

       public int reserved;

       public int record_number;

       public Date time_generated;

       public Date time_written;

       public int event_id;

       public short event_type;

       public short event_category;

       public short reserved_flags;

       public int closing_record_number;

       public eventlog.sid_t sid;

       public String source_name;

       public String computer_name;

       public String[] strings;

       public byte[] raw_data;

       public void encode(NdrBuffer _dst) throws NdrException {
           int size = 0, size_offset = 0, size_offset2 = 0;
           int stringoffset = 0, stringoffset_offset = 0;
           int sid_length = 0, sid_length_offset = 0;
           int sid_offset = 0, sid_offset_offset = 0;
           int data_offset = 0, data_offset_offset = 0;
           size_offset = _dst.getIndex();
           _dst.enc_ndr_long(size);
           _dst.enc_ndr_long(reserved);
           _dst.enc_ndr_long(record_number);
           _dst.enc_ndr_long(time_generated == null ? 0
                   : (int) (time_generated.getTime() / 1000));
           _dst.enc_ndr_long(time_written == null ? 0 : (int) (time_written
                   .getTime() / 1000));
           _dst.enc_ndr_long(event_id);
           _dst.enc_ndr_short(event_type);
           _dst.enc_ndr_short(strings == null ? 0 : strings.length);
           _dst.enc_ndr_short(event_category);
           _dst.enc_ndr_short(reserved_flags);
           _dst.enc_ndr_long(closing_record_number);
           stringoffset_offset = _dst.getIndex();
           _dst.enc_ndr_long(stringoffset);
           sid_length_offset = _dst.getIndex();
           _dst.enc_ndr_long(sid_length);
           sid_offset_offset = _dst.getIndex();
           _dst.enc_ndr_long(sid_offset);
           _dst.enc_ndr_long(raw_data == null ? 0 : raw_data.length);
           data_offset_offset = _dst.getIndex();
           _dst.enc_ndr_long(data_offset);
           _dst.enc_ndr_string(source_name);
           _dst.enc_ndr_string(computer_name);
           sid_offset = _dst.getIndex();
           if (sid != null) {
               sid.encode(_dst);
               sid_length = _dst.getIndex() - sid_offset;
           }
           stringoffset = _dst.getIndex();
           if (strings != null && strings.length > 0) {
               for (int i = 0; i < strings.length; i++) {
                   _dst.enc_ndr_string(strings[i]);
               }
           }
           data_offset = _dst.getIndex();
           if (raw_data != null && raw_data.length > 0) {
               _dst.writeOctetArray(raw_data, 0, raw_data.length);
           }
           size_offset2 = _dst.getIndex();
           _dst.enc_ndr_long(size);
           size = _dst.getIndex() - size_offset;
           // Write information back.
           _dst.setIndex(size_offset);
           _dst.enc_ndr_long(size);
           _dst.setIndex(stringoffset_offset);
           _dst.enc_ndr_long(stringoffset);
           _dst.setIndex(sid_length_offset);
           _dst.enc_ndr_long(sid_length);
           _dst.setIndex(sid_offset_offset);
           _dst.enc_ndr_long(sid_offset);
           _dst.setIndex(data_offset_offset);
           _dst.enc_ndr_long(data_offset);
           _dst.setIndex(size_offset2);
           _dst.enc_ndr_long(size);
       }

       public void decode(NdrBuffer _src) throws NdrException {
           int cur_offset = _src.getIndex();
           size = _src.dec_ndr_long();
           reserved = _src.dec_ndr_long();
           record_number = _src.dec_ndr_long();
           long tmp = 0;
           tmp = _src.dec_ndr_long();
           time_generated = new Date(tmp * 1000);
           tmp = _src.dec_ndr_long();
           time_written = new Date(tmp * 1000);
           event_id = _src.dec_ndr_long();
           event_type = (short) _src.dec_ndr_short();
           int num_of_strings = _src.dec_ndr_short();
           event_category = (short) _src.dec_ndr_short();
           reserved_flags = (short) _src.dec_ndr_short();
           closing_record_number = _src.dec_ndr_long();
           int stringoffset = _src.dec_ndr_long();
           int sid_length = _src.dec_ndr_long();
           int sid_offset = _src.dec_ndr_long();
           int data_length = _src.dec_ndr_long();
           int data_offset = _src.dec_ndr_long();
           source_name = _src.dec_ndr_unistring();
           computer_name = _src.dec_ndr_unistring();
           if (sid_length > 0) {
               _src.setIndex(cur_offset + sid_offset);
               if (sid == null) {
                   sid = new eventlog.sid_t();
               }
               sid.decode(_src);
           } else {
               sid = null;
           }
           if (num_of_strings > 0) {
               _src.setIndex(cur_offset + stringoffset);
               strings = new String[num_of_strings];
               for (int i = 0; i < strings.length; i++) {
                   strings[i] = _src.dec_ndr_unistring();
               }
           } else {
               strings = new String[0];
           }
           if (data_length > 0) {
               _src.setIndex(cur_offset + data_offset);
               raw_data = new byte[data_length];
               _src.readOctetArray(raw_data, 0, data_length);
           } else {
               raw_data = new byte[0];
           }
           _src.setIndex(cur_offset + size);
       }
   }

   public static class EventlogClearEventLog extends DcerpcMessage {

       public int getOpnum() {
           return 0x00;
       }

       public int retval;

       public rpc.policy_handle handle;

       public rpc.unicode_string backupFile;

       public EventlogClearEventLog(rpc.policy_handle handle,
               rpc.unicode_string backupfile) {
           this.handle = handle;
           this.backupFile = backupfile;
           retval = -1;
       }

       public void encode_in(NdrBuffer _dst) throws NdrException {
           handle.encode(_dst);
           backupFile.encode(_dst);
       }

       public void decode_out(NdrBuffer _src) throws NdrException {
           retval = (int) _src.dec_ndr_long();
       }
   }

   public static class EventLogCloseEventLog extends DcerpcMessage {
       public int getOpnum() {
           return 0x02;
       }

       public int retval;

       public rpc.policy_handle handle;

       public EventLogCloseEventLog(rpc.policy_handle handle) {
           this.handle = handle;
           ptype = 0;
           retval = -1;
       }

       public void encode_in(NdrBuffer _dst) throws NdrException {
           handle.encode(_dst);
       }

       public void decode_out(NdrBuffer _src) throws NdrException {
           handle.decode(_src);
           retval = (int) _src.dec_ndr_long();
       }
   }

   public static class EventLogGetNumRecords extends DcerpcMessage {
       public int getOpnum() {
           return 0x04;
       }

       public int numberOfRecords;

       public int retval;

       public rpc.policy_handle handle;

       public EventLogGetNumRecords(rpc.policy_handle handle) {
           this.handle = handle;
           ptype = 0;
           retval = -1;
       }

       public void encode_in(NdrBuffer _dst) throws NdrException {
           handle.encode(_dst);
       }

       public void decode_out(NdrBuffer _src) throws NdrException {
           numberOfRecords = (int) _src.dec_ndr_long();
           retval = (int) _src.dec_ndr_long();
       }
   }

   public static class EventLogGetOldestEntry extends DcerpcMessage {
       public int getOpnum() {
           return 0x05;
       }

       public int oldestEntryNumber;

       public int retval;

       public rpc.policy_handle handle;

       public EventLogGetOldestEntry(rpc.policy_handle handle) {
           this.handle = handle;
           ptype = 0;
           retval = -1;
       }

       public void encode_in(NdrBuffer _dst) throws NdrException {
           handle.encode(_dst);
       }

       public void decode_out(NdrBuffer _src) throws NdrException {
           oldestEntryNumber = (int) _src.dec_ndr_long();
           retval = (int) _src.dec_ndr_long();
       }
   }

   public static class EventLogOpenEventLog extends DcerpcMessage {
       public int getOpnum() {
           return 0x07;
       }

       public rpc.unicode_string logname;

       public rpc.unicode_string servername;

       public int retval;

       public rpc.policy_handle handle;

       public EventLogOpenEventLog(rpc.unicode_string logname,
               rpc.unicode_string servername) {
           this.logname = logname;
           this.servername = servername;
           ptype = 0;
           retval = -1;
       }

       public void encode_in(NdrBuffer _dst) throws NdrException {
           _dst.align(8);
           _dst.enc_ndr_long(0xf000baaa);
           _dst.enc_ndr_short(0x64);
           _dst.enc_ndr_short(0x01);
           logname.encode(_dst);
           servername.encode(_dst);
           // _dst.align(4);
           _dst.enc_ndr_long(0x01);
           _dst.enc_ndr_long(0x01);
       }

       public void decode_out(NdrBuffer _src) throws NdrException {
           if (handle == null) {
               handle = new rpc.policy_handle();
           }
           handle.decode(_src);
           retval = (int) _src.dec_ndr_long();
       }
   }

   public static class EventLogReadEventLog extends DcerpcMessage {
       public int getOpnum() {
           return 0x0a;
       }

       public int retval;

       public int flags;

       public int offset;

       public int maxReadSize;

       public int bytesInNextRecords;

       public List entries;

       public int sent_size;

       public int real_size;

       public rpc.policy_handle handle;

       public EventLogReadEventLog(rpc.policy_handle handle, int flags,
               int offset, int maxReadSize) {
           this.handle = handle;
           this.flags = flags;
           this.offset = offset;
           this.maxReadSize = maxReadSize;
           ptype = 0;
           retval = -1;
       }

       public void encode_in(NdrBuffer _dst) throws NdrException {
           _dst.align(8);
           handle.encode(_dst);
           _dst.enc_ndr_long(flags);
           _dst.enc_ndr_long(offset);
           _dst.enc_ndr_long(maxReadSize);
       }

       public void decode_out(NdrBuffer _src) throws NdrException {
           int bytesInResponse = 0, curOffset = 0, bytesTotal = 0;
           bytesInResponse = _src.dec_ndr_long();
           curOffset = _src.getIndex();
           _src.setIndex(curOffset + bytesInResponse);
           sent_size = _src.dec_ndr_long();
           real_size = _src.dec_ndr_long();
           retval = (int) _src.dec_ndr_long();
           _src.setIndex(curOffset);
           while (bytesTotal < sent_size) {
               if (entries == null) {
                   entries = new ArrayList();
               }
               EventlogRecord entry = new EventlogRecord();
               entry.decode(_src);
               entries.add(entry);
               bytesTotal += entry.size;
           }
       }
   }
}

