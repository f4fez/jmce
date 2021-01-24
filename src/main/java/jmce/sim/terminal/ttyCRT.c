/**
   $Id: ttyCRT.c 371 2010-09-28 01:41:15Z mviara $

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

#ifdef __WIN32__
#include <windows.h>
#include <conio.h>
#endif

#include <stdio.h>

#ifdef __NCURSES__
#include <curses.h>
#endif

#include "jmce_sim_terminal_ttyCRT.h"

#ifdef __WIN32__
static char lineBuffer[256];
static CONSOLE_SCREEN_BUFFER_INFO csb;
static HANDLE hStdOut;
static HANDLE hStdIn;
static DWORD  oldConsoleInMode;
static DWORD  oldConsoleOutMode;
static WORD	  curColor;

#define	NORMAL		FOREGROUND_GREEN
#define	NORMAL_R	BACKGROUND_GREEN
#define	UNDER		FOREGROUND_RED
#define	UNDER_R		BACKGROUND_RED
#define BLINK	(FOREGROUND_RED|FOREGROUND_BLUE)
#define	BLINK_R (BACKGROUND_RED|BACKGROUND_BLUE)
#define	BLINKU	(FOREGROUND_RED|FOREGROUND_GREEN)
#define	BLINKU_R (BACKGROUND_RED|BACKGROUND_GREEN)

static WORD	  attributes[] =
{
	NORMAL						, // N
	NORMAL_R					, // R
	NORMAL|FOREGROUND_INTENSITY			, // H
	NORMAL_R|BACKGROUND_INTENSITY			, // R H
	UNDER						, // U N
	UNDER_R						, // U R
	UNDER|FOREGROUND_INTENSITY			, // U H
	UNDER_R|BACKGROUND_INTENSITY			, // U R H
	BLINK						, // B N
	BLINK_R						, // B R
	BLINK|FOREGROUND_INTENSITY			, // B H
	BLINK_R|BACKGROUND_INTENSITY			, // B R H
	BLINKU						, // B U N
	BLINKU_R					, // B U R
	BLINKU|FOREGROUND_INTENSITY			, // B U H
	BLINKU_R|BACKGROUND_INTENSITY			, // B U R H
	
	
};


JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttyReset(JNIEnv *env, jobject o)
{
	COORD c;
	CONSOLE_CURSOR_INFO cursor;

	SetConsoleMode(hStdIn,oldConsoleInMode);
	SetConsoleMode(hStdOut,oldConsoleOutMode);

	GetConsoleCursorInfo(hStdOut,&cursor);
	cursor.bVisible = TRUE;
	SetConsoleCursorInfo(hStdOut,&cursor);


	c.X = 0;
	c.Y = csb.dwSize.Y - 1;

	SetConsoleCursorPosition(hStdOut,c);
	SetConsoleTextAttribute(hStdOut,FOREGROUND_RED|FOREGROUND_BLUE|FOREGROUND_GREEN);

}

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttySetCursor(JNIEnv *env, jobject c,jboolean mode)
{
	CONSOLE_CURSOR_INFO cursor;

	GetConsoleCursorInfo(hStdOut,&cursor);
	cursor.bVisible = mode == JNI_TRUE ? TRUE : FALSE;
	SetConsoleCursorInfo(hStdOut,&cursor);
	
}


JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttySetCursorPosition(JNIEnv *env, jobject o,jint row,jint col)
{
	COORD c;

	c.X = col;
	c.Y = row;

	SetConsoleCursorPosition(hStdOut,c);
}


JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttyPutchar(JNIEnv *env, jobject c,jbyteArray arr,jint len,jint a)
{
	//SetConsoleTextAttribute(hStdOut, currentColor);
	int color = a & 0x0f;
	DWORD written = 0;
	int i;
	jbyte * body;
	

	
	if (attributes[color] != curColor)
	{
		curColor = attributes[color];
		SetConsoleTextAttribute(hStdOut,curColor);

	}

	body = (*env)->GetByteArrayElements(env, arr, 0);

	for (i = 0 ; i < len ; i++)
		lineBuffer[i] = body[i];
	(*env)->ReleaseByteArrayElements(env, arr, body, 0);
	
	WriteConsole(hStdOut,lineBuffer,len,&written,NULL);

}


JNIEXPORT jboolean JNICALL Java_jmce_sim_terminal_ttyCRT_ttyInit(JNIEnv *env, jobject c)
{
	curColor = 0;
	hStdOut = GetStdHandle(STD_OUTPUT_HANDLE);
	hStdIn  = GetStdHandle(STD_INPUT_HANDLE);

	GetConsoleScreenBufferInfo(hStdOut,&csb);
	GetConsoleMode(hStdIn,&oldConsoleInMode);
	GetConsoleMode(hStdOut,&oldConsoleOutMode);
	SetConsoleMode(hStdIn,oldConsoleInMode &~ ENABLE_PROCESSED_INPUT);
	SetConsoleMode(hStdOut,oldConsoleOutMode &~ ENABLE_WRAP_AT_EOL_OUTPUT);

	return JNI_TRUE;
	
}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetNumCol(JNIEnv *env, jobject c)
{
	return csb.dwSize.X;
}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetNumRow(JNIEnv *env, jobject c)
{
	return csb.dwSize.Y;
}



JNIEXPORT jboolean JNICALL Java_jmce_sim_terminal_ttyCRT_ttyKbhit(JNIEnv *env, jobject c)
{
	return kbhit() == 0 ? JNI_FALSE : JNI_TRUE;
}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetch(JNIEnv *env, jobject c)
{
	int ch;

	ch = getch();


	if (ch == 0 || ch == 0xe0)
	{
		ch = ((getch() & 0xff) << 8);
	}

	return ch;
	
}

#else

#ifdef __NCURSES__

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttyReset(JNIEnv *env, jobject o)
{
	endwin();
}

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttySetCursor(JNIEnv *env, jobject c,jboolean mode)
{
}

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttySetCursorPosition(JNIEnv *env, jobject o,jint row,jint col)
{

	move(row,col);
	refresh();

}

static int oldAtt = -1;

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttyPutchar(JNIEnv *env, jobject c,jbyteArray arr,jint len,jint a)
{
	int i;
	jbyte * body;

	if (a != oldAtt)
	{
		oldAtt = a;

		if (a & 0x01)
			attron(A_REVERSE);
		else
			attroff(A_REVERSE);
		
		if (a & 0x02)
			attron(A_STANDOUT);
		else
			attroff(A_STANDOUT);
		
		if (a & 0x04)
			attron(A_UNDERLINE);
		else
			attroff(A_UNDERLINE);
		
		if (a & 0x08)
			attron(A_BLINK);
		else
			attroff(A_BLINK);
	}
	
	body = (*env)->GetByteArrayElements(env, arr, 0);

	for (i = 0 ; i < len ; i++)
		addch(body[i]);
	(*env)->ReleaseByteArrayElements(env, arr, body, 0);
	refresh();
}

JNIEXPORT jboolean JNICALL Java_jmce_sim_terminal_ttyCRT_ttyInit(JNIEnv *env, jobject c)
{
	initscr();
	if (has_colors())
	{
		start_color();
		init_pair(1,COLOR_GREEN,COLOR_BLACK);
		attrset(COLOR_PAIR(1));
	}
	raw();
	noecho();
	nonl();
	intrflush(stdscr,FALSE);
	keypad(stdscr,TRUE);

}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetNumCol(JNIEnv *env, jobject c)
{
	return COLS;
}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetNumRow(JNIEnv *env, jobject c)
{
	return LINES;
}
JNIEXPORT jboolean JNICALL Java_jmce_sim_terminal_ttyCRT_ttyKbhit(JNIEnv *env, jobject c)
{
	int ch;
	
	nodelay(stdscr,TRUE);
	ch = getch();
	nodelay(stdscr,FALSE);


	if (ch != ERR)
	{

		ungetch(ch);
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetch(JNIEnv *env, jobject c)
{


	int ch =  getch();

	switch (ch)
	{
		case	KEY_DOWN:
			ch = 0x5000;
			break;
		case	KEY_UP:
			ch = 0x4800;
			break;
		case	KEY_LEFT:
			ch = 0x4b00;
			break;
		case	KEY_RIGHT:
			ch = 0x4d00;
			break;
		default:
			if (ch > 0xFF)
				ch = 0x4400;
	}
	
	return ch;
}
#else	// __NCURSES__
JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttyReset(JNIEnv *env, jobject o)
{
	
}

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttySetCursor(JNIEnv *env, jobject c,jboolean mode)
{
}

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttySetCursorPosition(JNIEnv *env, jobject o,jint row,jint col)
{
	printf("\033[%d;%dH",row,col);
	fflush(stdout);
}

JNIEXPORT void JNICALL Java_jmce_sim_terminal_ttyCRT_ttyPutchar(JNIEnv *env, jobject c,jbyteArray arr,jint len,jint a)
{
	int i;
	jbyte * body;


	body = (*env)->GetByteArrayElements(env, arr, 0);

	for (i = 0 ; i < len ; i++)
		putchar(body[i]);
	fflush(stdout);
}

JNIEXPORT jboolean JNICALL Java_jmce_sim_terminal_ttyCRT_ttyInit(JNIEnv *env, jobject c)
{
	printf("\33[2J");

}
JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetNumCol(JNIEnv *env, jobject c)
{
	return 80;
}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetNumRow(JNIEnv *env, jobject c)
{
	return 23;
}
JNIEXPORT jboolean JNICALL Java_jmce_sim_terminal_ttyCRT_ttyKbhit(JNIEnv *env, jobject c)
{
	return JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_jmce_sim_terminal_ttyCRT_ttyGetch(JNIEnv *env, jobject c)
{
	return 0x4400;
}

#endif	// __NCURSES__
#endif  // __WIN32__

