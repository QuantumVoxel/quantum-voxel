/*
 * This file is part of Mixin, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dev.ultreon.xeox.compat.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;

public class LoggerAdapterSlf4j extends LoggerAdapterAbstract {
    private final Logger logger;

    public LoggerAdapterSlf4j(String name) {
        super(name);
        this.logger = LoggerFactory.getLogger(name);
    }

    @Override
    public String getType() {
        return "Log4j2 (via XeoxLoader)";
    }

    @Override
    public void catching(Level level, Throwable t) {
        switch (level) {
            case TRACE:
                this.logger.trace(t.getMessage(), t);
                break;
            case DEBUG:
                this.logger.debug(t.getMessage(), t);
                break;
            case INFO:
                this.logger.info(t.getMessage(), t);
                break;
            case WARN:
                this.logger.warn(t.getMessage(), t);
                break;
            default:
                this.logger.error(t.getMessage(), t);
        }
    }

    @Override
    public void catching(Throwable t) {
        this.logger.error(t.getMessage(), t);
    }

    @Override
    public void debug(String message, Object... params) {
        this.logger.debug(message, params);
    }

    @Override
    public void debug(String message, Throwable t) {
        this.logger.debug(message, t);
    }

    @Override
    public void error(String message, Object... params) {
        this.logger.error(message, params);
    }

    @Override
    public void error(String message, Throwable t) {
        this.logger.error(message, t);
    }

    @Override
    public void fatal(String message, Object... params) {
        this.logger.error(message, params);
    }

    @Override
    public void fatal(String message, Throwable t) {
        this.logger.error(message, t);
    }

    @Override
    public void info(String message, Object... params) {
        this.logger.info(message, params);
    }

    @Override
    public void info(String message, Throwable t) {
        this.logger.info(message, t);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        switch (level) {
            case TRACE:
                this.logger.trace(message, params);
                break;
            case DEBUG:
                this.logger.debug(message, params);
                break;
            case INFO:
                this.logger.info(message, params);
                break;
            case WARN:
                this.logger.warn(message, params);
                break;
            default:
                this.logger.error(message, params);
                break;
        }
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        switch (level) {
            case TRACE:
                this.logger.trace(message, t);
                break;
            case DEBUG:
                this.logger.debug(message, t);
                break;
            case INFO:
                this.logger.info(message, t);
                break;
            case WARN:
                this.logger.warn(message, t);
                break;
            default:
                this.logger.error(message, t);
                break;
        }
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        this.logger.error(t.getMessage(), t);
        return t;
    }

    @Override
    public void trace(String message, Object... params) {
        this.logger.trace(message, params);
    }

    @Override
    public void trace(String message, Throwable t) {
        this.logger.trace(message, t);
    }

    @Override
    public void warn(String message, Object... params) {
        this.logger.warn(message, params);
    }

    @Override
    public void warn(String message, Throwable t) {
        this.logger.warn(message, t);
    }

}
