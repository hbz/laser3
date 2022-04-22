#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# 2020-01-31
# david.klober@hbz-nrw.de

deFile = 'messages_de.properties'
enFile = 'messages.properties'

deValueDict = dict()
enValueDict = dict()

print("- reading file: {0}".format(deFile))

with open(deFile) as fp:
    line = fp.readline().strip()
    while line:
        foundKey = None
        foundValue = None
        parts = line.split('=', 1)

        if len(parts) > 1 and parts[0].find('#') != 0:
            foundKey = parts[0].strip()
            foundValue = parts[1].strip()

        else:
            parts = line.split(' ', 1)

            if len(parts) > 1 and parts[0].find('#') != 0 and parts[0].find('.') >= 0:
                foundKey = parts[0].strip()
                foundValue = parts[1].strip()

        if foundKey and foundValue:
            tmp = deValueDict.get(foundValue, [])
            tmp.append(foundKey)
            deValueDict.update({foundValue:tmp})

        line = fp.readline()

print("- reading file: {0}".format(enFile))

with open(enFile) as fp:
    line = fp.readline().strip()
    while line:
        foundKey = None
        foundValue = None
        parts = line.split('=', 1)

        if len(parts) > 1 and parts[0].find('#') != 0:
            foundKey = parts[0].strip()
            foundValue = parts[1].strip()

        else:
            parts = line.split(' ', 1)

            if len(parts) > 1 and parts[0].find('#') != 0 and parts[0].find('.') >= 0:
                foundKey = parts[0].strip()
                foundValue = parts[1].strip()

        if foundKey and foundValue:
            tmp = enValueDict.get(foundValue, [])
            tmp.append(foundKey)
            enValueDict.update({foundValue:tmp})

        line = fp.readline()

print("")
print("----- Duplikate DE -----")
print("")
for key, value in sorted(deValueDict.items(), key=lambda item: len(item[1])):
    if len(value) > 1:
        print("{0:^5} > {1} : {2}".format(len(value), value, key))

print("")
print("----- Duplikate EN -----")
print("")
for key, value in sorted(enValueDict.items(), key=lambda item: len(item[1])):
    if len(value) > 1:
        print("{0:^5} > {1} : {2}".format(len(value), value, key))
