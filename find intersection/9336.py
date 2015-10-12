file = open("rssi.txt","r",encoding='utf-8')
collect=[]
a = 0
for line in file:
#for a in range(len(file)):
    if(a%3 == 1):
        collect.append(set(line.rstrip("\n").split(", ")))
    a += 1
#print(collect)
for i in range(0, len(collect)-2):
    for j in range(i+1, len(collect)-1):
        for k in range(j+1, len(collect)):
            result = collect[i] & collect[j] & collect[k]
            if( len(result) > 0):
                print(i,j,k,result)
    
