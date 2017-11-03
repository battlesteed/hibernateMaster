/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
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
package steed.util.logging.impl;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.ReflectionUtil;

import steed.util.logging.Logger;
import steed.util.logging.LoggerFactory.InternalLoggerFactory;


public class Log4jLoggerFactory implements InternalLoggerFactory {
	
	public Logger getLogger(Class<?> clazz) {
		return new Log4jLogger(clazz);
	}
	
	public Logger getLogger(String name) {
		return new Log4jLogger(name);
	}
}

class Log4jLogger extends Logger { 
	private org.apache.logging.log4j.Logger log;
	
	/**
     * Gets the {@link LoggerContext} associated with the given caller class.
     *
     * @param callerClass the caller class
     * @return the LoggerContext for the calling class
     */
    protected LoggerContext getContext(final Class<?> callerClass) {
        ClassLoader cl = null;
        if (callerClass != null) {
            cl = callerClass.getClassLoader();
        }
        if (cl == null) {
            cl = LoaderUtil.getThreadContextClassLoader();
        }
        return LogManager.getContext(cl, false);
    }
	
    protected LoggerContext getContext() {
        final Class<?> anchor = ReflectionUtil.getCallerClass(steed.util.logging.Logger.class.getName(), "steed.util.logging");
        return anchor == null ? LogManager.getContext() : getContext(ReflectionUtil.getCallerClass(anchor));
    }
	
	Log4jLogger(Class<?> clazz) {
		log = getContext().getLogger(clazz.getName());
	}
	
	Log4jLogger(String name) {
		log = LogManager.getLogger(name);
	}
	
	public void debug(String format, Object... args){
		String msg = String.format(format, args);
		log.log(Level.DEBUG, msg);
	} 
	
	public void info(String format, Object... args){
		String msg = String.format(format, args);
		log.log(Level.INFO, msg);
	}
	
	public void warn(String format, Object... args){
		String msg = String.format(format, args);
		log.log(Level.WARN, msg);
	}
	
	public void error(String format, Object... args){
		String msg = String.format(format, args);
		log.log(Level.ERROR, msg);
	}
	
	public void info(String message) {
		log.log(Level.INFO, message);
	}
	
	public void info(String message, Throwable t) {
		log.log(Level.INFO, message, t);
	}
	
	public void debug(String message) {
		log.log(Level.DEBUG, message);
	}
	
	public void debug(String message, Throwable t) {
		log.log(Level.DEBUG, message, t);
	}
	
	public void warn(String message) {
		log.log(Level.WARN, message);
	}
	
	public void warn(String message, Throwable t) {
		log.log(Level.WARN, message, t);
	}
	
	public void error(String message) {
		log.log(Level.ERROR, message);
	}
	
	public void error(String message, Throwable t) {
		log.log(Level.ERROR, message, t);
	}
	
	public void fatal(String message) {
		log.log(Level.FATAL, message);
	}
	
	public void fatal(String message, Throwable t) {
		log.log(Level.FATAL, message, t);
	}
	
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}
	
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}
	
	public boolean isWarnEnabled() {
		return log.isEnabled(Level.WARN);
	}
	
	public boolean isErrorEnabled() {
		return log.isEnabled(Level.ERROR);
	}
	
	public boolean isFatalEnabled() {
		return log.isEnabled(Level.FATAL);
	}

	@Override
	public void trace(String message) {
		log.log(Level.TRACE, message);
	}

	@Override
	public void trace(String message, Throwable t) {
		log.log(Level.TRACE, message, t);
	}

	@Override
	public boolean isTraceEnabled() { 
		return log.isTraceEnabled();
	}
}