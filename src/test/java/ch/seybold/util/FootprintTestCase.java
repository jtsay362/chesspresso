/*
 * Copyright (C) Bernhard Seybold. All rights reserved.
 *
 * This software is published under the terms of the LGPL Software License,
 * a copy of which has been included with this distribution in the LICENSE.txt
 * file.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * $Id: FootprintTestCase.java,v 1.2 2003/01/04 16:17:14 BerniMan Exp $
 */

package ch.seybold.util;


import junit.framework.*;
import java.io.*;
import java.util.zip.*;


/**
 * Extension of a test case that supports footprints.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public abstract class FootprintTestCase extends TestCase
{
    
    private static LineNumberReader m_in;
    private static PrintStream m_out;

    //======================================================================
    
    private class FootprintWriter extends Writer
    {
        public void close() throws IOException
        {
            m_out.close();
        }
        
        public void flush() throws IOException
        {
            m_out.flush();
        }
        
        public void write(char[] cbuf, int off, int len) throws IOException
        {
            FootprintTestCase.this.write(new String(cbuf, off, len));
        }
        
    }
    
    //======================================================================
    
    public void startFootprint(String name, boolean zipped) throws Exception
    {
        String packageName = getClass().getPackage().getName().replace('.', '/');
        InputStream in = ClassLoader.getSystemResourceAsStream(packageName + '/' + name);
        if (in != null) {
            // input found
            if (zipped) {
                in = new GZIPInputStream(in);
            }
            m_in = new LineNumberReader(new InputStreamReader(in));
        } else {
            // no input found -> produce output
            assertTrue("Output file exists", !new File(name).exists());
            if (zipped) {
                m_out = new PrintStream(new GZIPOutputStream(new FileOutputStream(name)));
            } else {
                m_out = new PrintStream(new FileOutputStream(name));
            }
        }
    }
    
    public void stopFootprint() throws Exception
    {
        if (m_in != null) {
            try {
                for (;;) {
                    String line = m_in.readLine();
                    if (line == null) {
                        break;
                    } else if (line.length() > 0) {
                        fail("Too many lines in footprint: '" + line + "'");
                    }
                }
            } catch (EOFException ex) {
                // ok
            } finally {
                m_in.close();
                m_in = null;
            }
        } else {
            m_out.close();
            m_out = null;
            fail("No input footprint found, output produced");
        }
    }
    
    //======================================================================
    
    protected Writer getFootprint()
    {
        return new FootprintWriter();
    }
    
    private static boolean ignoreChar(char ch)
    {
        return ch < 32;
    }
    
    private char readChar() throws IOException
    {
        for (;;) {
            char ch = (char)m_in.read();
            if (!ignoreChar(ch)) return ch;
        }
    }
    
    private void write(String s, boolean newline)
    {
        try {
            if (m_in == null) {
                if (newline) {
                    m_out.println(s);
                } else {
                    m_out.print(s);
                }
            } else {
                for (int i=0; i<s.length(); i++) {
                    char ch = s.charAt(i);
                    if (!ignoreChar(ch)) {
                        assertEquals("Chars in line " + m_in.getLineNumber() + " not equal, last write: " + s, readChar(), ch);
                    }
                }
            }
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }
    
    protected void write(String s)
    {
        write(s, false);
    }
    
    protected void writeln()
    {
        write("", true);
    }
    
    protected void writeln(String s)
    {
        write(s, true);
    }
    
}