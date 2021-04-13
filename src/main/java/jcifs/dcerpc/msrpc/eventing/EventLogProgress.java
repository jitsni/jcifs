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

public class EventLogProgress {
    public EventLogException connectionError;
    public int lastEventRecordId;
    public long lastSubscriptionTime;
    public long lastPullTime;
    public String lastEventTimeCreated;

    public String toString() {
        return "(connectionError=" + connectionError +
                ", lastRecordId=" + lastEventRecordId +
                ", lastSubscriptionTime=" + lastSubscriptionTime +
                ", lastPullTime=" + lastPullTime +
                ", lastTimeCreated=" + lastEventTimeCreated +
                ")";
    }
}
