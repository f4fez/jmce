
============================================================================
$Id: readme.txt 946 2012-12-02 11:01:18Z mviara $
Java Multiple Computer Emulator
============================================================================

Revision history
============================================================================
1.02	02 Dec 2012
	Now java 1.6 is required.
	Fixed bug in JLed component.
	Added DPTR support for LPC764.
	Added jmce.properties with user preference.
	Better performance for MCS51 family.
	Fixed bug in MCS51 MOXV @RI,A and MOVX A,@RI
	Added support for fast cassette play.
	Preliminary support for spectrum tape with .TZX files.
	New tape data model with better design and performance.
	New memory model for ZX spectrum and support for spectrum 128K.
	Fixed bug on global memory read/write listener.
	Added support for virtual 8052 jmce.viara.v8052.V8052
	Fixed bug in serial devices.
	Added new option -d to start emulation with integrated debugger.
	Removed few warning.
	Added support for Motorola S19 files.
	Added initial support for freescale M68HC05 and M68HC08.
	Fixed bug 8051 in MUL AB thank you to Dmitri Danilki.
	Ported the project in eclipse and now can be builded using Ant.
	Compiling with Ant require cpptasks.jar and ant-contrib.jar.
	Fixed bug in 8051 serial now interrupt work tx and rx.
	Improved interrupt emulation.

1.01	25 Dec 2010
	Improved realtime emulation now the clock error is less than 1%.
	Added support for VIC20.
	Added for mos 6552 and mos 6551.
	Added support for tape in library and monitor.
	Added support for MOS 6502.
	Improved emulation of ZX Spectrum speaker.
	Improved performance of Plain and  Banked memory.
	Added support for Philips P2000T without cassette.
	Added support for teletext characters generator SAA5050.
	Added Z80 CTC peripheral.
	Added support for Interrupt mode 2 in Z80.
	Added auto detect of idle time for Yaze,Altair and Z80pack systems.
	Fixed many bug in disassembler for i8080 / Z80.
	Add support in DirDiskCPM for Turbodos attribute.
	Added support for property readOnly on interface jmce.sim.Disk
	Improved performance of PolledSerial

1.00	30 Sep 2010
	JMCE a simulator for 8 bit microprocessor (Intel 8080, Zilog Z80,
	Intel 8051) and many computer based over them as ZX Spectrum, Altair
	8800 , Z80Pack, Yaze, etc. running their original ROM and operating
	system. All JMCE computer can be configure programmatically or using
	xml, for example it is possible connect the console of IMSAI 8080
	to one TCP server or to one phisical serial interface without writing
	one line of code or it is possible change the memory for the Z80Pack
	computer from plain to banked memory again only editing a single xml
	files. A more complete documentation of parameters is available in the
	javadocs of the package.


Information wanted
============================================================================
If anyone have detailed information about :

 - Philips P2000T cassette interface.

Please send me one email. I need this information to improve JMCE.

JMCE Quick installation guide.
============================================================================
A better guide can be found in the file readme.htl or
at the JMCE web site http://www.viara.eu/en/jmce/docs.


INSTALLATION
============================================================================
JMCE executable archive is named jmce-xxxx.tar.gz where xxxx is the version
number. Extract the package in one new directory, you will see the following
directories and files :

jars\		Directory with jar files
bin\		Directory with batch file and native library
xml\		Directory with XML configured files
hex\		Directory with intel executable.
jmce		Script for Linux / Unix
jmce.bat	Script fot Windows
.......


RUNNING JMCE
============================================================================
You Must Have Java JRE 1.6 Or Better Installed.

Try the command jmce.bat for Windows or ./jmce (later I will write only jmce
instead of jmce.bat or ./jmce) if the Java machine is installed correctly a
brief list of command options will be displayed.

JMCE have few options and as argument the name of the class for the
desired computer or the xml files with the configuration.
The argument can be not specified if JMCE is started with the
integrated debugger / monitor (option -m) program.

The option -m Start JMCE with the integrated monitor. As default the
monitor run in one Swing window but can also be started as TCP server
listening for a connection in a specific port (options -p port and -t1).

The option -d start JMCE withe the integrated GUI debugger monitor.

To stop the JMCE when run without integrated monitor or swing interface
press the key CTRL-^ in the terminal of the emulated conputer.

Some example :

1. jmce jmce.z80pack.Z80Pack

Start jmce in  the current TTY without integrated monitor and run the Z80Pack
emulator with CP/M 3.0

2. jmce jmce.sinclair.spectrum.Spectrum48K

Start in a Swing window the emulator for Sinclair ZX Spectrum 48K.

3. jmce -m jmce.sinclair.spectrum.Spectrum48K

Start in a swing windows the emulator for Sinclair ZX Spectrum 48K and
in another window the integrated debugger/monitor. To start the emulator
you must type the command GO in the monitor window. For a list of the monitor
command try HELP.

4. jmce xml/cmon51_tty.xml

Start in the current console the simulator of one Intel 8052 running
the CMON51 software.

5. jmce xml/cmon51_tcp.xml

Start the emulator for CMON51 with the console connected to a TCP server
listening on port 2023. You must start another session ans with a raw telnet
client (as putty) connect to the port 2023 of your computer. If you use a telnet
client it works but the telnet options are not negotiated.

6. jmce -d jmce.commodore.vic20.VIC20

Start the commodore VIC20 emulator with the integrated GUI debugger.

For more detailed information about the xml configuration and supported
computer see the html documentations.

For any question do not esitate to contact me at mario@viara.eu
