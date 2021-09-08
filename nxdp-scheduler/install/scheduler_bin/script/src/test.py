# -*- coding: utf-8 -*-

import sys, json, os

src_path = os.path.dirname(os.path.abspath(__file__))
sys.path.append(src_path + "/..")
print src_path + " &&"
etc_path = src_path + "/../etc"
print etc_path
sys.path.append(etc_path)
import parser_configs

print parser_configs.log_path
print 'hello'

string = 'str\n dd'
print string
print string.split('\n')[0]

# def functionName( level ):
#     if level < 2:
#         raise Exception("Invalid level! %s" %str(level))
#
# functionName(1)

import base64 as b64

def xor_encrypt(tips,key):
    ltips=len(tips)
    lkey=len(key)
    secret=[]
    num=0
    for each in tips:
        if num>=lkey:
            num=num%lkey
        secret.append( chr( ord(each)^ord(key[num]) ) )
        num+=1

    print  "".join( secret ).encode()
    return b64.b64encode( "".join( secret ).encode() ).decode()


def xor_decrypt(secret,key):

    tips = b64.b64decode( secret.encode() ).decode()

    ltips=len(tips)
    # lkey=len(key)
    secret=[]
    num=0
    for each in tips:
        # if num>=lkey:
        #     num=num%lkey

        secret.append( chr( ord(each)^ord('a') ) )
        num+=1

    return "".join(secret)


tips= "afsdfbd"
key= "owen"
secret = xor_encrypt(tips,key)
print( "cipher_text:", secret )

# DhEWCgkVAQ==
plaintxt = xor_decrypt( 'GQADAlBTUg==', key )
# plaintxt = xor_decrypt( 'kImKi9na2w==', key )
print("plain_text:", plaintxt)


def te():
    return 1,2
print te()[0]
