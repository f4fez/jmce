/**
   $Id: BankedMemory.java 639 2011-06-24 08:55:14Z mviara $

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
package jmce.sim.memory;

import java.util.logging.Logger;
import jmce.sim.*;
import jmce.util.Hex;

/*
 * Banked memory implementation.
 * <p>
 * This type of memory have one or more  bank of the same size, every
 * bank is divided in fixed lenght page. The banked mmu start to
 * operate after the initMmu() method it is called. If this method is
 * not called operated like a standard PlainMemory. Optionally the
 * memory can have one shared area mapped for each bank at the same
 * phisical address.
 * 
 * 
 * @author Mario Viara
 * @version 1.02
 *
 */
public class BankedMemory extends PlainMemory
{
	private static final Logger log = Logger.getLogger(BankedMemory.class.getName());
	
	private int bank = 0;
	private int numBank = 0;
	private int pageSize = 0;
	private int pageShift = 0;
	private int pageMask = 0;
	private int numPage = 0;
	private int pages[][];
	private int sharedStart = 0;
	private int sharedSize = 0;
	
	private boolean mmuInit = false;

	/** Default constructor */
	public BankedMemory()
	{
		
	}

	/**
	 * Constructor with all parameter.
	 */
	public BankedMemory(String name,int bankSize,int numBank,int numPage,int pageSize)
	{
		super(name,bankSize);

		setNumPage(numPage);
		setPageSize(pageSize);
		setNumBank(numBank);
				
	}

	/**
	 * Set the size of each page in byte.
	 */
	public final void setPageSize(int n)
	{
		pageSize = n;
		pageShift = 0;
		
		/** Calculate page shift */
		for (n >>>= 1 ; n != 0 ; n >>>= 1)
			pageShift ++;

		pageMask = (1 << pageShift) - 1;

		log.info("PageSize="+pageSize+" Shift="+pageShift+" mask="+pageMask);
		
	}

	/**
	 * Return the size of the page.
	 */
	public final int getPageSize()
	{
		return pageSize;
	}

	/**
	 * Set the number of page in each bank.
	 */
	public final void setNumPage(int n)
	{
		numPage = n;
	}

	/**
	 * Return the number of page in each bank.
	 */
	public final int getNumPage()
	{
		return numPage;
	}

	/**
	 * Set the number of bank
	 */
	public final void setNumBank(int n)
	{
		numBank = n;
	}

	/**
	 * Return the number of bank
	 */
	public final int getNumBank()
	{
		return numBank;
	}

	/**
	 * Initialize the MMU.
	 * <p>
	 * Till this method is not called the memory operate like a
	 * standard memory. After the MMU initialization the content
	 * of the memory for each bank is the same of the content of
	 * the standard memory before the initialization.
	 */
	protected final void initMmu()
	{
		int i;

		log.info("InitMMU NumBank="+numBank+" PageSize="+pageSize+" NumPage="+numPage+" Size="+getSize()+" SharedStart="+sharedStart+" Shared size="+sharedSize);
		mmuInit = true;
		pages = new int[numBank][numPage];
		tmpMemory = allocMemory(getSize());

		// Save the current memory in tmp array
		copyMemory(memory,0,tmpMemory,0,size);

		memory = allocMemory(getSize() * numBank);
		
		// Copy the current memory in all bank and init register
		for (int bank = 0 ; bank < numBank ; bank ++)
		{
			copyMemory(tmpMemory,0,memory,bank * size,size);
			for (int page = 0 ; page < numPage ; page++)
				setPageMap(bank,page,bank * numPage + page);
		}

		for (int bank = 1 ; bank < numBank ; bank++)
			for (i = 0 ; i < sharedSize ; i++)
				setPageMap(bank,sharedStart+i,sharedStart+i);

		setBank(0);
		showMemory();
		
	}

	/**
	 * Set the start address of the shared area in page.
	 */
	public final void setSharedStart(int n)
	{
		sharedStart = n;
	}

	/**
	 * Set the lenght of the shared area in page
	 */
	public final void setSharedSize(int n)
	{
		sharedSize = n;
	}

	/**
	 * Return the size of the shared area.
	 */
	public final int getSharedSize()
	{
		return sharedSize;
	}

	/**
	 * Return the start of the shared area in pages.
	 */
	public final int getSharedStart()
	{
		return sharedStart;
	}

	/**
	 * Return the current bank
	 */
	public final int getBank()
	{
		return bank;
	}

	/**
	 * Set the current bank.
	 */
	public final void setBank(int n)
	{
		bank = n;
	}

	/**
	 * Return the phisical address of the specified virtual address.
	 */
	private final int mmuLookup(int a)
	{
		return  (pages[bank][a >>> pageShift]) + (a & pageMask);
	}

	@Override
	protected final int get(int a)
	{
		if (mmuInit)
		{
			return memory[mmuLookup(a)] & 0xff;
		}
		
		return super.get(a);
	}

	@Override
	protected final void set(int a,int v)
	{
		if (mmuInit)
		{
			memory[mmuLookup(a)] = (byte)v;
			return;
		}

		super.set(a,v);
	}

	@Override
	public void reset() throws SIMException
	{
		memory = allocMemory(getSize());
		mmuInit = false;
		super.reset();
	}

	/**
	 * Return true if the MMU is initialized and the memory operate
	 * as banked.
	 */
	public boolean isMmuInitialized()
	{
		return mmuInit;
	}

	/**
	 * Show using log the configuration of memory.
	 */
	public void showMemory()
	{
		String s = (getSize() / 1024)+"KB "+
			 "Bank="+numBank+" "+
			 "Size="+((getSize() * numBank) / 1024)+"KB "+
			 "Page="+pageSize+" "+
		         "Pbank="+numPage+" ";

		if (sharedSize > 0)
		{
			 s += "Segment=0000-"+Hex.formatWord(((sharedStart * pageSize)-1))+" "+
			      "Ssize="+((sharedSize * pageSize)/1024+"KB");
		}
		log.info(s);
			 
		
		for (int i =  0 ; i < numBank ; i++)
		{
			s = "BANK #"+i+" ";
			for (int j = 0 ; j < numPage ; j++)
			{
				if (j > 0)
					s += ",";
				s += Hex.formatDword(getPageMap(i,j) * pageSize);
			}
			log.fine(s);
		}
	}

	/**
	 * Map a page of memory.
	 *
	 * @param bank - Bank number.
	 * @param page - Logical page.
	 * @param p - Phisical page.
	 *
	 * @since 1.01
	 */
	public final void setPageMap(int bank,int page,int p)
	{
		pages[bank][page] = p << pageShift;
	}

	/**
	 * Return the mapping for one page.
	 *
	 * @param bank - Bank number.
	 * @param page - Logical page.
	 *
	 * @return the Phisical page.
	 * @since 1.01
	 */
	public final int getPageMap(int bank,int page)
	{
		return pages[bank][page] >>> pageShift;
	}
	
	public String toString()
	{
		String s = "MMU "+numBank+"x"+(getSize()/1024)+" KB";
		if (sharedSize > 0)
			s+= " Shared "+Hex.formatWord(sharedStart * pageSize)+"-"+
			    Hex.formatWord((sharedStart+sharedSize) * pageSize - 1);
		
		s+= " MMU="+mmuInit;

		return s;
	}
	
}
