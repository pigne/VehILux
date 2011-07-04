import socket
import struct

FITNESS_CMD=0x10
TYPE_INT_ARRAY=0x51

HOST = 'localhost'
PORT = 4444

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((HOST, PORT))


i=0
while i< 100:
    i=i+1
    params=[1, 3, 5, 7]
    msg=""
    msg= struct.pack("!BBh", FITNESS_CMD, TYPE_INT_ARRAY, len(params))
    for p in params:
        msg = msg + struct.pack("!i", p)

    s.send(msg)
    #responce
    data = s.recv(4)
    fitness = struct.unpack('!i', data)[0]
    print 'Received', fitness


s.close()
