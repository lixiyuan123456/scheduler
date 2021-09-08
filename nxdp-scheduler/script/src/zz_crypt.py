# -*- coding: utf-8 -*-

import base64 as b64

class nxCrypt(object):

    def xor_encrypt(self, src_str):
        secret=[]
        for each in src_str:
            secret.append(chr(ord(each) ^ ord('a')))

        return b64.b64encode("".join(secret).encode()).decode()


    def xor_decrypt(self, secret):

        base64_str = b64.b64decode(secret.encode()).decode()

        secret=[]
        for each in base64_str:
            secret.append(chr(ord(each) ^ ord('a')))

        return "".join(secret)

if __name__ == "__main__":
    nxCrypt = nxCrypt()
    print nxCrypt.xor_encrypt("xbc123@!")
    print nxCrypt.xor_decrypt(nxCrypt.xor_encrypt("xbc123@!"))

