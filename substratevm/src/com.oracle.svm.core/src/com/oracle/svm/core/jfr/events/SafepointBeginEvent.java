/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022, 2022, Red Hat Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.svm.core.jfr.events;

import com.oracle.svm.core.annotate.Uninterruptible;
import com.oracle.svm.core.jfr.JfrEvent;
import com.oracle.svm.core.jfr.HasJfrSupport;
import com.oracle.svm.core.jfr.JfrNativeEventWriter;
import com.oracle.svm.core.jfr.JfrNativeEventWriterData;
import com.oracle.svm.core.jfr.JfrNativeEventWriterDataAccess;
import com.oracle.svm.core.jfr.JfrTicks;
import com.oracle.svm.core.jfr.SubstrateJVM;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.word.UnsignedWord;

public class SafepointBeginEvent {
    /**
     * HotSpot reports the number of Java threads, so we do the same. In our case that includes the
     * VM operation thread. This is a major difference to HotSpot but affects other JFR areas as
     * well.
     */
    @Uninterruptible(reason = "Accesses a JFR buffer.")
    public static void emit(UnsignedWord safepointId, int numJavaThreads, long startTicks) {
        if (!HasJfrSupport.get()) {
            return;
        }

        if (SubstrateJVM.isRecording() && SubstrateJVM.get().isEnabled(JfrEvent.SafepointBegin)) {
            JfrNativeEventWriterData data = StackValue.get(JfrNativeEventWriterData.class);
            JfrNativeEventWriterDataAccess.initializeThreadLocalNativeBuffer(data);

            JfrNativeEventWriter.beginEventWrite(data, false);
            JfrNativeEventWriter.putLong(data, JfrEvent.SafepointBegin.getId());
            JfrNativeEventWriter.putLong(data, startTicks);
            JfrNativeEventWriter.putLong(data, JfrTicks.elapsedTicks() - startTicks);
            JfrNativeEventWriter.putEventThread(data);
            JfrNativeEventWriter.putLong(data, safepointId.rawValue());
            JfrNativeEventWriter.putInt(data, numJavaThreads);
            JfrNativeEventWriter.putInt(data, 0); // jniCriticalThreadCount
            JfrNativeEventWriter.endEventWrite(data, false);
        }
    }
}
