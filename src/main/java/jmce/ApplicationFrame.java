/**
   $Id: ApplicationFrame.java 814 2012-03-29 11:07:49Z mviara $

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
package jmce;


import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import jmce.sim.CPU;
import jmce.sim.ExceptionEvent;
import jmce.sim.LoadInfo;
import jmce.sim.Tape;
import jmce.sim.TapeEventListener;
import jmce.sim.TapePulse;
import jmce.sim.cpu.AbstractCPU;
import jmce.sim.tape.TapeData;
import jmce.sim.tape.TapeFile;
import jmce.swing.SwingThread;
import jmce.util.Logger;

/**
 * Base JFrame for all graphics JMCE application.
 *
 * @author Mario Viara
 * @version 1.00
 * @since 1.02
 */
public class ApplicationFrame extends JFrame implements TapeEventListener
{
	private static final long serialVersionUID = 1L;
	protected AbstractCPU cpu;
	private TapeFile tape = null;
	protected JFileChooser   fc = null;
	private JProgressBar  tapeBar = new JProgressBar();
	private JLabel usage = new JLabel("100.00%");
	private boolean restoreRealTime = false;
	
	protected AbstractAction actionTapeLoad;
	protected AbstractAction actionTapeListen;
	protected AbstractAction actionTapePlay;
	protected AbstractAction actionTapeFast;
	protected AbstractAction actionTapeStop;
	protected AbstractAction actionFileLoad;
	protected AbstractAction actionFileExit;

	public ApplicationFrame(CPU _cpu)
	{
		this.cpu = (AbstractCPU)_cpu;

		fc = new JFileChooser();
	
		String cd = Property.getProperty(Property.fileDirectory,".");
		fc.setCurrentDirectory(new File(cd));
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				performExit();
			}
		});
	}

	/**
	 * Load a program in the cpu memory
	 */
	protected void performLoad()
	{
		try
		{

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				LoadInfo info = new LoadInfo();

				cpu.load(fc.getSelectedFile().getCanonicalPath(),0,info);
				showMessage("File :"+fc.getSelectedFile(),"Loaded : "+info);
				Property.setProperty(Property.fileDirectory,fc.getCurrentDirectory().getPath());
			}
		}
		catch (java.lang.Throwable ex)
		{
			showError(ex);
		}
	}


	/**
	 * Exit from the application
	 */
	protected final void performExit()
	{
		setVisible(false);

		cpu.stop();
		try
		{
			cpu.destroy();
			System.exit(0);

		}
		catch (Exception ex)
		{
			Logger.writeEx(ex);
			System.exit(0);
		}

	}

	/**
	 * Play on the sound system the current selected file
	 */
	private final void performTapeListen() throws Exception
	{
		final int sample = 44100;
		final byte wavLow = (byte)(128 - 25);
		final byte wavHigh = (byte)(128 + 25);
		tape.play();
		
		SwingThread w = new SwingThread(this,"Listen cassette file",tape.getConfig())
		{
			private static final long serialVersionUID = 1L;
			
			public void doWork() throws Exception
			{
		
				TapeData data = tape.getTapeData();
				if (data == null)
					throw new Exception("No file loaded");

				setMaximum(tape.getNumTapeData());
				AudioFormat fmt = new AudioFormat(sample ,8,1,false,false);
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
				SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(fmt);
				line.start();
				byte buffer[] = new byte[1024];
				int bufferLen = 0;
				TapePulse tp;
				byte wav = wavLow;

				while ((tp = tape.nextPulse()) != null)
				{
					if (isCancelled())
						break;
					setValue(tape.getCurrentTapeData());

					int n =  (int) ((0.5 + (((double)sample / data.getClock()) * (double)tp.getWidth())));

					for (int i = 0 ; i < n ; i++)
					{
						buffer[bufferLen++] = wav;
						if (bufferLen >= buffer.length)
						{
							line.write(buffer,0,bufferLen);
							bufferLen = 0;
						}
					}

					switch (tp.getType())
					{
						case	TapePulse.DATA_LOW:
							wav = wavLow;
							break;
						case	TapePulse.DATA_HIGH:
							wav = wavHigh;
							break;
						case	TapePulse.DATA_TOGGLE:
							wav = (wav == wavLow ? wavHigh : wavLow);
							break;
					}

				}
			
			line.close();
			}
		};

		w.start();
		tape.stop();
		
		if (w.getException() != null)
			showError(w.getException());
	}
	
	/**
	 * Load a file in the tape reader
	 */
	private final void performTapeLoad()
	{
		try
		{

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				String name = fc.getSelectedFile().getCanonicalPath();

				tape.setConfig(name);


			}
		}
		catch (java.lang.Throwable ex)
		{
			showError(ex);
		}

	}

	/**
	 * Perform a tape play.
	 */
	private final void performTapePlay()
	{
		try
		{
			tapeBar.setStringPainted(false);
			tapeBar.setIndeterminate(true);

			tape.play();
		}
		catch (Exception e)
		{
			showError(e);
		}
	}

	/**
	 * Stop the tape.
	 */
	private final void performTapeStop()
	{
		try
		{
			tape.stop();
		}
		catch (Exception e)
		{
			showError(e);
		}

	}

	/**
	 * Add an icon to one action
	 */
	protected final void addIcon(Action action,String name)
	{
		Icon icon = jmce.swing.Util.getIcon(this.getClass(),name);
		if (icon != null)
			action.putValue(Action.SMALL_ICON,icon);
	}

	/**
	 * Add a key accellerator to one menu
	 */
	protected final void addKey(JMenuItem item,char m)
	{
		item.setMnemonic(m);
		item.setAccelerator(KeyStroke.getKeyStroke(m, KeyEvent.ALT_MASK));
	}

	/**
	 * Create the file menu
	 */
	protected JMenu createMenuFile()
	{
		actionFileLoad = new AbstractAction("Load")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				performLoad();
			}

		};

		actionFileExit = new AbstractAction("Exit")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				performExit();
			}
		};

		addIcon(actionFileLoad,"load.gif");
		addIcon(actionFileExit,"exit.gif");

		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');
		menu.setIcon(jmce.swing.Util.getIcon(this.getClass(),"file.gif"));
		addKey(menu.add(actionFileLoad),'L');
		addKey(menu.add(actionFileExit),'X');

		return menu;
	}

	/**
	 * Create the tape menu only if a Tape is present in the
	 * current CPU oterwise return null.
	 */
	protected JMenu createMenuTape()
	{

		tape = (TapeFile)cpu.getHardware(TapeFile.class);
		if (tape == null)
			return null;


		actionTapePlay = new AbstractAction("Play")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				performTapePlay();
			}

		};

		actionTapeLoad = new AbstractAction("Load")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				performTapeLoad();
			}

		};

		actionTapeStop = new AbstractAction("Stop")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				performTapeStop();
			}

		};


		actionTapeFast = new AbstractAction("Fast play")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				restoreRealTime = true;
				cpu.setRealTime(false);
				performTapePlay();
			}

		};

		actionTapeListen = new AbstractAction("Listen")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				try
				{
					performTapeListen();
				}
				catch (Exception ex)
				{
					showError(ex);
				}
			}

		};
			
		JMenu menu = new JMenu("Tape");
		menu.setMnemonic('E');
		menu.setIcon(jmce.swing.Util.getIcon(this.getClass(),"cassette.png"));
		addKey(menu.add(actionTapeLoad),'L');
		addKey(menu.add(actionTapePlay),'P');
		addKey(menu.add(actionTapeFast),'F');
		addKey(menu.add(actionTapeStop),'S');
		addKey(menu.add(actionTapeListen),'K');
		

		tapeBar.setStringPainted(false);
		tapeBar.setIndeterminate(true);

		tape.addTapeEventListener(this);
		tapeStop(tape);

		return menu;

	}

	/**
	 * Show a message
	 */
	protected void showMessage(String title,String msg)
	{
		JOptionPane.showMessageDialog(this,msg,title,JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Show an error
	 */
	void showError(String msg)
	{
		JOptionPane.showMessageDialog(this,msg,"Error JMCE Ver. "+Jmce.versionNumber,JOptionPane.ERROR_MESSAGE);
	}


	void showError(ExceptionEvent e)
	{
		showError(e.ex);
	}

	void showError(Throwable e)
	{
		e.printStackTrace();
		String s =e.toString();
		if (s == null || s.length() == 0)
			s = e.toString();
		showError(s);
	}

	/************************************************
	 * Tape event listener implementation		*
	 ************************************************/
	public void tapeStop(Tape tape)
	{
		actionTapeListen.setEnabled(true);
		actionTapeFast.setEnabled(true);
		actionTapePlay.setEnabled(true);
		actionTapeStop.setEnabled(false);
		actionTapeLoad.setEnabled(true);
		if (restoreRealTime)
		{
			restoreRealTime = false;
			cpu.setRealTime(true);
		}
	}

	public void tapePlay(Tape tape)
	{
		actionTapeFast.setEnabled(false);
		actionTapeListen.setEnabled(false);
		actionTapePlay.setEnabled(false);
		actionTapeStop.setEnabled(true);
		actionTapeLoad.setEnabled(false);

	}

	public void tapeRec(Tape tape)
	{
	}

	public void tapePower(Tape tape)
	{
	}

	/**
	 * Create the toolbar
	 */
	protected final JToolBar createToolBar()
	{

		JToolBar p = new JToolBar();

		usage.setFont(new Font("Monospaced",Font.BOLD,20));
		usage.setForeground(Color.yellow);
		usage.setBackground(Color.black);
		usage.setOpaque(true);

		p.add(new JLabel(jmce.swing.Util.getIcon(this.getClass(),"Cpu.gif")));
		p.add(usage);
		
		if (tape != null)
		{
			p.add(new JToolBar.Separator());
			p.add(new JLabel(jmce.swing.Util.getIcon(this.getClass(),"cassette.png")));
			p.add(tapeBar);
		}

		return p;
	}

	/**
	 * Called at period interval from swing thread to update the
	 * graphics interface.
	 */
	protected void swingTimer()
	{
		if (cpu.isRunning())
		{
			double d = cpu.getUsage();

			usage.setForeground(Color.white);

			if (d > 90.0)
				usage.setForeground(Color.red);
			else if (d > 50.0)
				usage.setForeground(Color.yellow);
			else if (d > 25.0)
				usage.setForeground(Color.green);
			else if (d > 10.0)
				usage.setForeground(Color.cyan);

			String s = d+"%";
			while (s.length() < 7)
				s =" "+s;
			usage.setText(s);
		}

		if (tape != null)
		{
			if (tape.isPlay())
			{
				if (tapeBar.isIndeterminate())
				{
					tapeBar.setIndeterminate(false);
					tapeBar.setMaximum(tape.getNumTapeData());
					tapeBar.setStringPainted(true);

				}

				tapeBar.setValue(tape.getCurrentTapeData());
			}
		}

	}

	/**
	 * Set the emulation mode
	 */
	protected void setEmulation(boolean e)
	{
		if (actionFileLoad != null)
		{
			actionFileLoad.setEnabled(!e);
		}
	}
}

