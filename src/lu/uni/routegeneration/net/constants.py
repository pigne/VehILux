"""

	@file    constants.py

	@date Jun 10, 2011
	
	@author Yoann Pigné

	Copyright  = (c) 2011 University of Luxembourg

"""



# ****************************************
# COMMANDS
# ****************************************
# command: get version
CMD_GETVERSION = 0x00

CMD_START = 0x01
CMD_END = 0x02
	
CMD_FITNESS = 0x20
	
	
#GS commands idea...
CMD_STEP = 0x20
CMD_ADD_GRAPH = 0x21
CMD_CHANGE_GRAPH = 0x22
CMD_DELETE_GRAPH = 0x23
CMD_ADD_NODE = 0x24
CMD_CHANGE_NODE = 0x25
CMD_DELETE_NODE = 0x26
CMD_ADD_EDGE = 0x27
CMD_CHANGE_EDGE = 0x28
CMD_DELETE_EDGE  =0x29
	
	
	
# ****************************************
# Values types
# ****************************************
	
	
	
# Values types
TYPE_INT = 0xa0
TYPE_INT_ARRAY = 0xa1 # followed by a 2-bytes int length
TYPE_DOUBLE = 0xa2
TYPE_DOUBLE_ARRAY = 0xa3
TYPE_STRING = 0xa4
TYPE_STRING_ARRAY = 0xa5	
TYPE_RAW = 0xa6
TYPE_COMPOUND = 0xa7
