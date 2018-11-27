/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.lir.amd64;

import static jdk.vm.ci.amd64.AMD64.xmm0;
import static jdk.vm.ci.amd64.AMD64.xmm1;

import org.graalvm.compiler.core.common.LIRKind;
import org.graalvm.compiler.lir.LIRInstructionClass;
import org.graalvm.compiler.lir.Variable;
import org.graalvm.compiler.lir.gen.LIRGenerator;

import jdk.vm.ci.amd64.AMD64Kind;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.RegisterValue;
import jdk.vm.ci.meta.Value;

/**
 * This is the base class for all binary math intrinsics (stubs). It does not specify input or
 * output, but instead assumes input(s) being xmm0 (and xmm1), and output being xmm0 as well. Users
 * are responsible for adding mov LIRs before and after this LIR instruction, to move the inputs to
 * aforementioned registers, and to move the result from xmm0.
 */
public abstract class AMD64MathIntrinsicBinaryOp extends AMD64MathIntrinsicOp {

    @Def protected Value output;
    @Use protected Value input0;
    @Use protected Value input1;
    @Temp protected Value[] temps;

    public AMD64MathIntrinsicBinaryOp(LIRInstructionClass<? extends AMD64MathIntrinsicBinaryOp> type, Register... registers) {
        super(type);

        input0 = xmm0.asValue(LIRKind.value(AMD64Kind.DOUBLE));
        input1 = xmm1.asValue(LIRKind.value(AMD64Kind.DOUBLE));
        output = xmm0.asValue(LIRKind.value(AMD64Kind.DOUBLE));

        temps = registersToValues(registers);
    }

    public final Variable emitLIRWrapper(LIRGenerator gen, Value input0, Value input1) {
        LIRKind kind = LIRKind.combine(input0, input1);
        RegisterValue xmm0Value = xmm0.asValue(kind);
        gen.emitMove(xmm0Value, input0);
        RegisterValue xmm1Value = xmm1.asValue(kind);
        gen.emitMove(xmm1Value, input1);
        gen.append(this);
        Variable result = gen.newVariable(kind);
        gen.emitMove(result, xmm0Value);
        return result;
    }
}
