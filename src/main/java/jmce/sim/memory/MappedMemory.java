/**
 * $Id: MappedMemory.java 476 2010-12-16 07:41:45Z mviara $
 */
package jmce.sim.memory;

import jmce.sim.*;

/**
 * Mapped memory <p>
 *
 * This Class implement one memory base over other memory mapped to
 * this one. All part of memory not mapped will be redirect to a
 * standard PlainMemory. The memory can be mapped at address and size
 * multiple of the page size.
 *
 * @author Mario Viara
 * @version 1.01
 */
public class MappedMemory extends PlainMemory
{
	/** Inner class with information of single page */
	class Page
	{
		Memory		m;
		int		offset;
		
		Page(Memory m,int offset)
		{
			this.m = m;
			this.offset = offset;
		}
	};
	
	/** Size of minimum mapped unit in bytes */
	protected int page = 1024;

	/** Pages of memory */
	protected Page pages[] = null;

	/**
	 * Constructor with all paramater.
	 *
	 * @param name - Name of the memory.
	 * @param size - Total size of memory.
	 * @param page - Size of each page.
	 */
	public MappedMemory(String name,int size,int page)
	{
		super(name,size);
		setPage(page);
	}

	/**
	 * Constructor with only name and size.
	 */
	public MappedMemory(String name,int size)
	{
		this(name,size,1024);
	}

	/**
	 * Default constructor
	 *
	 * @since 1.01
	 */
	public MappedMemory()
	{
		this("MapMemory",0x100000,4096);
	}
	
	public int getPage()
	{
		return page;
	}

	public void setPage(int page)
	{
		this.page = page;
	}

	public void mapMemory(Memory m,int address,int offset,int size) throws SIMException
	{
		if ((size % page) != 0)
			throw new SIMException("MapMemory size must be multiple of "+page);

		if ((address % page) != 0)
			throw new SIMException("MapMemory address must be multiple of "+page);

		/**
		 * Create the array of page if do not exist.
		 */
		if (pages == null)
		{
			if ((getSize() % page) != 0)
				throw new SIMException("Memory size "+getSize()+" not compatible with page size "+page);
			pages  = new Page[getSize()/page];
		}

		/**
		 * Calculate page start
		 */
		int i = address / page;

		/**
		 * Map all requird pages.
		 */
		while (size > 0)
		{
			Page p = new Page(m,offset);
			pages[i] = p;
			i++;
			offset += page;
			size -= page;
		}
			
		
	}

	protected Memory mapMemory(int a)
	{
		if (pages == null)
			return this;

		Page p  = pages[a / page];

		if (p == null)
			return this;
		
		if (p.m == null)
			return this;

		return p.m;
	}

	protected int mapAddress(int a)
	{
		if (pages == null)
			return a;
		Page p = pages[a / page];

		if (p == null)
			return a;
		
		if (p.m == null)
			return a;

		return (a % page) + p.offset;
	}

	public String toString()
	{
		return "Map PAGE="+page+" "+super.toString();
	}
}

   