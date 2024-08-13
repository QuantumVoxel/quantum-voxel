package dev.ultreon.quantum.command;

import dev.ultreon.libs.commons.v0.util.ExceptionUtils;
import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.graal.GraalLanguages;
import org.graalvm.polyglot.Value;

public class JSCommand extends Command {
    public JSCommand() {
        this.requirePermission("quantum.commands.js");
        this.setCategory(CommandCategory.EDIT);

        this.data().aliases("js");
    }

    @DefineCommand("<message>")
    public CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, String jsCode) {
        try {
            Value js = GraalLanguages.context.eval("js", jsCode);
            if (js.isNull()) return objectResult(null);
            else if (js.isBoolean()) return objectResult(js.asBoolean());
            else if (js.isString()) return objectResult(js.asString());
            else if (js.isDate()) return objectResult(js.asDate());
            else if (js.isHostObject()) return objectResult(js.asHostObject());
            else if (js.isProxyObject()) return objectResult(js.asProxyObject());
            else if (js.isInstant()) return objectResult(js.asInstant());
            else if (js.isDuration()) return objectResult(js.asDuration());
            else if (js.isTime()) return objectResult(js.asTime());
            else if (js.isTimeZone()) return objectResult(js.asTimeZone());
            else if (js.isException()) return objectResult(js.asHostObject());
            else if (js.isNativePointer()) return objectResult(js.asNativePointer());
            else if (js.isNumber()) return objectResult(js.asDouble());
            else return errorMessage("Invalid JS result");
        } catch (Throwable e) {
            return errorMessage(ExceptionUtils.getStackTrace(e));
        }

    }
}
