/**
   $Id: Logger.java 626 2011-06-08 08:29:30Z mviara $

   Copyright (c) 2010, Mario Viara

   Permission is hereby granted, free of charge, to any person obtaining a
   copy of this software and associated documentation files (the "Software"),
   to deal in the Software without restriction, including without limitation
   the rights to use, copy, modify, merge, publish, distribute, sublicense,
   and/or sell copies of the Software, and to permit persons to whom the
   Software is furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
   ROBERT M SUPNIK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

   Except as contained in this notice, the name of Mario Viara shall not be
   used in advertising or otherwise to promote the sale, use or other dealings
   in this Software without prior written authorization from Mario Viara.
*/
package jmce.util;

import java.util.logging.*;
import java.io.*;

/**
 * Extension to  java.util.logging.Logger to provide 
 * method for create logger using the class object and not the class
 * name. Also at startup read the configuration files from the disk.
 *
 * <h2>Log level</h2>
 * 
 * <ul>
 *  <li>trace or finest provide the most detailed information about
 *  execution.</li>
 *  <li>debug or finer provide detailed information.</li>
 *  <li>notice or fine provide more information.</li>
 *  <li>info provide normal information.</li>
 *  <li>config provide configuration information.</li>
 *  <li>warning provide information about recoverable error.</li>
 *  </ul>
 *  
 * @author Mario Viara
 * @version 1.00
 */
public class Logger extends java.util.logging.Logger
{
	static LogManager logManager = null;
	static public String configuration = "logging.properties";

	/**
	 * Return a logger for the specified class
	 */
	@SuppressWarnings("rawtypes")
	static public Logger getLogger(Class clazz)
	{
		return getLogger(clazz.getName());

	}

	/**
	 * Return a logger for the specified class name
	 */
	static public Logger getLogger(String name)
	{
		/** Initialize the log manager */
		if (logManager == null)
		{
			try
			{
				logManager = LogManager.getLogManager();
				logManager.reset();
				FileInputStream is = new FileInputStream(configuration);
				logManager.readConfiguration(is);
				is.close();
			}
			catch (Exception ignore)
			{
			}
			
		}


		java.util.logging.Logger l = logManager.getLogger(name);

		if (l == null || !(l instanceof Logger))
		{
			l = new Logger(name);
			logManager.addLogger(l);
		}
		
		return (Logger)l;
	}

	
	private void log(Level level,Exception ex)
	{
		if (isLoggable(level))
		{
			writeEx(ex);
			StringBuffer sb = new StringBuffer();
			log(level,ex.toString());
			StackTraceElement st[] = ex.getStackTrace();
			for (int i = 0 ; i < st.length ; i++)
			{
				
				sb.append(st[i].toString());
				if (i != st.length - 1)
					sb.append("\n");
			}
			log(level,sb.toString());
		}
	}
	
	public void fine(Exception ex)
	{
		log(Level.FINE,ex);
	}

	public void info(Exception ex)
	{
		log(Level.INFO,ex);
	}
	
	public void warning(Exception ex)
	{
		log(Level.WARNING,ex);
	}
	
	public void fatal(Exception ex)
	{
		log(Level.SEVERE,ex);
		System.exit(1);
	}
	
	private Logger(String name)
	{
		super(name,null);
	}

	/**
	 * Set the file name for the logger configuration
	 */
	static public void setConfiguration(String s)
	{
		configuration =s;
	}

	/**
	 * Dump the strack trace after one exception.
	 *
	 * @since 1.02
	 */
	static public void writeEx(Throwable e)
	{
		System.err.println(e);
		
		try
		{
			FileOutputStream os = new FileOutputStream("ex.txt");
			e.printStackTrace(new PrintStream(os));
			os.close();

		}
		catch (Exception ignore)
		{
		}

	}
}
