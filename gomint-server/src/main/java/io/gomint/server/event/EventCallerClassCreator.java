/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.server.event;

import io.gomint.event.EventListener;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;

/**
 * @author BlackyPaw
 * @version 1.0
 */
class EventCallerClassCreator {

    private EventCallerClassCreator() {}

    static byte[] createClass(EventListener instance, Method method, int nextProxyNumber) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        String listenerClassName = instance.getClass().getName().replace(".", "/");
        String className = listenerClassName + "Proxy" + nextProxyNumber;
        String eventClassName = method.getParameterTypes()[0].getName().replace(".", "/");

        // Define the class
        cw.visit(Opcodes.V11,
            Opcodes.ACC_PUBLIC,
            className,
            null,
            "java/lang/Object",
            new String[]{"io/gomint/server/event/EventProxy"});

        // Define the obj field
        cw.newField(className, "obj", "L" + listenerClassName + ";");
        cw.visitField(Opcodes.ACC_PUBLIC, "obj", "L" + listenerClassName + ";", null, null);

        // Build constructor
        MethodVisitor con = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        con.visitCode();
        con.visitVarInsn(Opcodes.ALOAD, 0);
        con.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        con.visitInsn(Opcodes.RETURN);
        con.visitMaxs(1, 1);

        // Build call method
        MethodVisitor callCon = cw.visitMethod(Opcodes.ACC_PUBLIC, "call", "(Lio/gomint/event/Event;)V", null, null);
        callCon.visitCode();
        callCon.visitVarInsn(Opcodes.ALOAD, 0);
        callCon.visitFieldInsn(Opcodes.GETFIELD, className, "obj", "L" + listenerClassName + ";");
        callCon.visitVarInsn(Opcodes.ALOAD, 1);
        callCon.visitTypeInsn(Opcodes.CHECKCAST, eventClassName);
        callCon.visitMethodInsn(Opcodes.INVOKEVIRTUAL, listenerClassName, method.getName(), "(L" + eventClassName + ";)V", false);
        callCon.visitInsn(Opcodes.RETURN);
        callCon.visitMaxs(2, 2);

        // Get bytecode
        return cw.toByteArray();
    }

}
