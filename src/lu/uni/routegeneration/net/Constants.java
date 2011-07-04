/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Constants.java
 * @date Jun 10, 2011
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.net;

/**
 * 
 * Lengths are expresses with 16-bit shorts
 * 
 */
public enum Constants {
	
	CMD_GETVERSION((byte)0x00),
	CMD_START((byte)0x01),
	CMD_END((byte)0x02),
	
	CMD_FITNESS((byte)0x10),
	
	
	// GS commands idea...
	CMD_STEP((byte)0x20),
	CMD_ADD_GRAPH((byte)0x21),
	CMD_CHANGE_GRAPH((byte)0x22),
	CMD_DELETE_GRAPH((byte)0x23),
	CMD_ADD_NODE((byte)0x24),
	CMD_CHANGE_NODE((byte)0x25),
	CMD_DELETE_NODE((byte)0x26),
	CMD_ADD_EDGE((byte)0x27),
	CMD_CHANGE_EDGE((byte)0x28),
	CMD_DELETE_EDGE((byte)0x29),
	
	
	
	
	
	
	// Values types
	
	
	// Followed by an 32-bit signed integer
	TYPE_INT((byte)0xa0), 
	// An array of integers. Followed by first, a 16-bits integer for the number of integerss and then, a list of 32-bit signed integers
	TYPE_INT_ARRAY((byte)0xa1),
	// Followed by a double precision 64-bits floating point number
	TYPE_DOUBLE((byte)0xa2), 
	// Array of double. Followed by first, a 16-bits integer for the number of doubles and then, a list of 64-bit doubles
	TYPE_DOUBLE_ARRAY((byte)0xa3),
	// Followed by an 64-bit signed integer
	TYPE_LONG((byte)0xa4), 
	// An array of longs. Followed by first, a 16-bits integer for the number of longs and then, a list of 62-bit signed integers
	TYPE_LONG_ARRAY((byte)0xa5),
	
	// Array of characters. Followed by first, a 16-bits integer for the size in bytes (not in number of characters) of the string, then by the unicode string 
	TYPE_STRING((byte)0xa6),
	
	// Raw data, good for serialization. Followed by first, a 16-bits integer indicating the length in bytes of the dataset, and then the data itself.
	TYPE_RAW((byte)0xa7),
	
	// Compound data where arrays contain other arrays mixed with native types. Each data piece in this case, has to announce it's  type (and length if applicable). May be useless because hard to decode...
	TYPE_COMPOUND((byte)0xa8),

	// Used with TYPE_COMPOUND. An undefined type array. Followed by first, a 16-bits integer indicating the number of elements, and then, the elements themselves. The elements themselves have to give their types. 
	TYPE_ARRAY((byte)0x08),

	
	
	
	
	//end
	;
	
	
	
	
	
	

	private final byte code;

	Constants(byte code) {
		this.code = code;
	}
	public byte code(){
		return code;
	}
}
