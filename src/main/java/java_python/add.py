import annoy
import random

def add(a,b):
    return a + b
print(add(5,4))

def ann():
    f = 40
    t = AnnoyIndex(f)
    for i in range(n):
        v = []
        for z in range(f):
            v.append(random.gauss(0, 1))
        t.add_item(i, v)

    t.build(10) # 10 trees
    t.save('test.tree')

    # …

    u = AnnoyIndex(f)
    u.load('test.tree') # super fast, will just mmap the file
    print(u.get_nns_by_item(0, 1000)) # will find the 1000 nearest neighbors