package com.ultreon.quantum.util;

import com.ultreon.quantum.api.commands.*;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.libs.commons.v0.exceptions.SyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.NoSuchMechanismException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.util.*;

public class ExceptionMap {
    public static String getErrorCode(@NotNull Throwable t) {
        if (t instanceof NoSuchMechanismException) {
            return "JX-0001";
        } else if (t instanceof SyntaxException) {
            return "SRV-0001";
        } else if (t instanceof IllegalCommandException) {
            return "SRV-0004";
        } else if (t instanceof UnsupportedOperationException) {
            return "J-0001";
        } else if (t instanceof IllegalStateException) {
            return "J-0002";
        } else if (t instanceof IllegalFormatCodePointException) {
            return "J-0023";
        } else if (t instanceof IllegalFormatConversionException) {
            return "J-0025";
        } else if (t instanceof IllegalFormatFlagsException) {
            return "J-0026";
        } else if (t instanceof IllegalFormatPrecisionException) {
            return "J-0027";
        } else if (t instanceof IllegalFormatWidthException) {
            return "J-0028";
        } else if (t instanceof IllegalFormatException) {
            return "J-0029";
        } else if (t instanceof IllegalArgumentException) {
            return "J-0003";
        } else if (t instanceof IllegalAccessException) {
            return "J-0004";
        } else if (t instanceof IllegalAccessError) {
            return "J-0004";
        } else if (t instanceof VirtualMachineError) {
            return "J-0005";
        } else if (t instanceof ClassCastException) {
            return "J-0006";
        } else if (t instanceof ClassNotFoundException) {
            return "J-0007";
        } else if (t instanceof ClassFormatError) {
            return "J-0008";
        } else if (t instanceof FileNotFoundException) {
            return "J-0009";
        } else if (t instanceof EOFException) {
            return "J-0009";
        } else if (t instanceof AccessDeniedException) {
            return "J-0010";
        } else if (t instanceof FileAlreadyExistsException) {
            return "J-0011";
        } else if (t instanceof NullPointerException) {
            return "J-0013";
        } else if (t instanceof NoSuchFieldException) {
            return "J-0014";
        } else if (t instanceof NoSuchMethodException) {
            return "J-0015";
        } else if (t instanceof NoSuchFieldError) {
            return "J-0016";
        } else if (t instanceof NoSuchMethodError) {
            return "J-0017";
        } else if (t instanceof NoSuchElementException) {
            return "J-0018";
        } else if (t instanceof NoSuchFileException) {
            return "J-0019";
        } else if (t instanceof NoSuchObjectException) {
            return "J-0020";
        } else if (t instanceof NoClassDefFoundError) {
            return "J-0021";
        } else if (t instanceof AccessException) {
            return "J-0022";
        } else if (t instanceof ArrayIndexOutOfBoundsException) {
            return "J-0030";
        } else if (t instanceof StringIndexOutOfBoundsException) {
            return "J-0031";
        } else if (t instanceof IndexOutOfBoundsException) {
            return "J-0032";
        } else if (t instanceof InterruptedException) {
            return "J-0033";
        } else if (t instanceof Error) {
            return "J-9996";
        } else if (t instanceof RuntimeException) {
            return "J-9997";
        } else if (t instanceof Exception) {
            return "J-9998";
        }
        return "J-9999";
    }

    public static void sendFatal(CommandSender s, Throwable t) {
        ExceptionMap.sendFatal(s, t, false);
    }

    public static void sendFatal(CommandSender s, Throwable thrown, boolean printStackTrace) {
        if (printStackTrace)
            QuantumServer.LOGGER.error("An error occurred: ", thrown);

        if (thrown instanceof NoSuchMechanismException) {
            s.sendMessage("[JX-0001] No such mechanism: " + thrown.getMessage());
        } else if (thrown instanceof CommandExecuteException) {
            s.sendMessage("[B-0002] Illegal or invalid command: " + thrown.getMessage());
        } else if (thrown instanceof SpecSyntaxException) {
            s.sendMessage("[SRV-0001] Syntax error: " + thrown.getMessage());
        } else if (thrown instanceof OverloadConflictException overloadConflictException) {
            s.sendMessage("[SRV-0004] Overload conflicts for commands: " + StringUtils.join(overloadConflictException.getAliases(), ", ") + ": " + thrown.getMessage());
        } else if (thrown instanceof UnsupportedOperationException) {
            s.sendMessage("[J-0001] Unsupported operation: " + thrown.getMessage());
        } else if (thrown instanceof IllegalStateException) {
            s.sendMessage("[J-0002] Illegal state: " + thrown.getMessage());
        } else if (thrown instanceof IllegalFormatCodePointException) {
            s.sendMessage("[J-0023] Illegal format code point: " + thrown.getMessage());
        } else if (thrown instanceof IllegalFormatConversionException) {
            s.sendMessage("[J-0025] Illegal format conversion: " + thrown.getMessage());
        } else if (thrown instanceof IllegalFormatFlagsException) {
            s.sendMessage("[J-0026] Illegal format flags: " + thrown.getMessage());
        } else if (thrown instanceof IllegalFormatPrecisionException) {
            s.sendMessage("[J-0027] Illegal format precision: " + thrown.getMessage());
        } else if (thrown instanceof IllegalFormatWidthException) {
            s.sendMessage("[J-0028] Illegal format width: " + thrown.getMessage());
        } else if (thrown instanceof IllegalFormatException) {
            s.sendMessage("[J-0029] Illegal format: " + thrown.getMessage());
        } else if (thrown instanceof IllegalArgumentException) {
            s.sendMessage("[J-0003] Illegal argument: " + thrown.getMessage());
        } else if (thrown instanceof IllegalAccessException) {
            s.sendMessage("[J-0004] Illegal access: " + thrown.getMessage());
        } else if (thrown instanceof IllegalAccessError) {
            s.sendMessage("[J-0004] Illegal access error: " + thrown.getMessage());
        } else if (thrown instanceof VirtualMachineError) {
            s.sendMessage("[J-0005] Virtual machine error: " + thrown.getMessage());
        } else if (thrown instanceof ClassCastException) {
            s.sendMessage("[J-0006] Class cast failure: " + thrown.getMessage());
        } else if (thrown instanceof ClassNotFoundException) {
            s.sendMessage("[J-0007] Class was not found: " + thrown.getMessage());
        } else if (thrown instanceof ClassFormatError) {
            s.sendMessage("[J-0008] Invalid class format: " + thrown.getMessage());
        } else if (thrown instanceof FileNotFoundException) {
            s.sendMessage("[J-0009] File not found: " + thrown.getMessage());
        } else if (thrown instanceof EOFException) {
            s.sendMessage("[J-0009] Unexpected EOF: " + thrown.getMessage());
        } else if (thrown instanceof AccessDeniedException) {
            s.sendMessage("[J-0010] Access was internally denied: " + thrown.getMessage());
        } else if (thrown instanceof FileAlreadyExistsException) {
            s.sendMessage("[J-0011] File already exists: " + thrown.getMessage());
        } else if (thrown instanceof NullPointerException) {
            s.sendMessage("[J-0013] Unexpected null pointer: " + thrown.getMessage());
        } else if (thrown instanceof NoSuchFieldException) {
            s.sendMessage("[J-0014] No such field: " + thrown.getMessage());
        } else if (thrown instanceof NoSuchMethodException) {
            s.sendMessage("[J-0015] No such method: " + thrown.getMessage());
        } else if (thrown instanceof NoSuchFieldError) {
            s.sendMessage("[J-0016] No such field error: " + thrown.getMessage());
        } else if (thrown instanceof NoSuchMethodError) {
            s.sendMessage("[J-0017] No such method error: " + thrown.getMessage());
        } else if (thrown instanceof NoSuchElementException) {
            s.sendMessage("[J-0018] No such element: " + thrown.getMessage());
        } else if (thrown instanceof NoSuchFileException) {
            s.sendMessage("[J-0019] No such file: " + thrown.getMessage());
        } else if (thrown instanceof NoSuchObjectException) {
            s.sendMessage("[J-0020] No such object: " + thrown.getMessage());
        } else if (thrown instanceof NoClassDefFoundError) {
            s.sendMessage("[J-0021] No class definition found: " + thrown.getMessage());
        } else if (thrown instanceof AccessException) {
            s.sendMessage("[J-0022] Access: " + thrown.getMessage());
        } else if (thrown instanceof ArrayIndexOutOfBoundsException) {
            s.sendMessage("[J-0030] Array index out of bounds: " + thrown.getMessage());
        } else if (thrown instanceof StringIndexOutOfBoundsException) {
            s.sendMessage("[J-0031] String index out of bounds: " + thrown.getMessage());
        } else if (thrown instanceof IndexOutOfBoundsException) {
            s.sendMessage("[J-0032] Index out of bounds: " + thrown.getMessage());
        } else if (thrown instanceof InterruptedException) {
            s.sendMessage("[J-0033] Thread was interrupted: " + thrown.getMessage());
        } else if (thrown instanceof Error) {
            s.sendMessage("[J-9996] An unknown compile error occurred: " + thrown.getMessage());
        } else if (thrown instanceof RuntimeException) {
            s.sendMessage("[J-9997] An unknown runtime error occurred: " + thrown.getMessage());
        } else if (thrown instanceof Exception) {
            s.sendMessage("[J-9998] An unknown generic error occurred.: " + thrown.getMessage());
        } else {
            s.sendMessage("[J-9999] An unknown error occurred.");
        }
    }
}
