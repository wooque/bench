#!/usr/bin/env python2

import os
import string
import random
import psycopg2

letters = list(string.ascii_letters + ' ' + string.digits)
all_chars = string.maketrans('', '')
result_chars = ''.join(letters[b % len(letters)] for b in range(len(all_chars)))
trans = string.maketrans(all_chars, result_chars)


def rand_str(size):
    return os.urandom(size).translate(trans)

if __name__ == '__main__':
    conn = psycopg2.connect(host="127.0.0.1", database="bench", user="bench", password="bench")
    c = conn.cursor()

    total = 0
    for i in range(200):

        dr = [(rand_str(140), rand_str(140), random.randint(0, 5000), random.randint(0, 1000)) for _ in range(5000)]
        data = [c.mogrify("(%s, %s, %s, %s)", dr[j]) for j in range(5000)]
        c.execute("INSERT INTO tst (title, thumb, nc, nv) VALUES " + ",".join(data))

        total += 5000
        if total % 100000 == 0:
            print total

    conn.commit()
    c.close()
    conn.close()
