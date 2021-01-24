/**
   $Id$

   Copyright (c) 2012, Mario Viara

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
package jmce;

import java.util.ResourceBundle;

import jmce.util.Logger;

/**
 * Class used to read version and subversion from subwvrev.properties.
 *
 * The version is in one properties file shared with ant build so the
 * distribution package name can contain the version and the subversion
 * release.
 *
 * @since 1.00c
 * @author Mario Viara
 */
public class Svnversion

{
	static private Logger log = Logger.getLogger(Svnversion.class);

	static public String SVNVERSION  = "";
	static public String VERSION = "0.0";

	static
	{
		ResourceBundle res = ResourceBundle.getBundle("svnversion");

		VERSION= res.getString("VERSION");
		SVNVERSION=res.getString("SVNVERSION");

	}

}
