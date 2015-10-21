import math
while(True):
    line = input("input the RSSI(dBm) collected\n")
    dbm = line.rstrip().split()
    #print(result)
    #dBm = 10log(mW)
    power = []
    for item in dbm:
        power.append(pow(10, int(item)/10))
    print(power)
