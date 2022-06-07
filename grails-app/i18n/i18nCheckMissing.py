#!/usr/bin/python3
# -*- coding: utf-8 -*-
#
# 2019-10-16
# david.klober@hbz-nrw.de

deFile = 'messages_de.properties'
enFile = 'messages.properties'

deKeys = []
enKeys = []
stats = dict()

print("- reading file: {0}".format(deFile))
with open(deFile) as fp:
    line = fp.readline().strip()
    while line:
        parts = line.split('=', 1)
        if len(parts) > 1 and parts[0].find('#') != 0:
            deKeys.append(parts[0].strip())
        else:
            parts = line.split(' ', 1)
            if len(parts) > 1 and parts[0].find('#') != 0 and parts[0].find('.') >= 0:
                deKeys.append(parts[0].strip())
        line = fp.readline()

print("- reading file: {0}".format(enFile))
with open(enFile) as fp:
    line = fp.readline().strip()
    while line:
        parts = line.split('=', 1)
        if len(parts) > 1 and parts[0].find('#') != 0:
            enKeys.append(parts[0].strip())
        else:
            parts = line.split(' ', 1)
            if len(parts) > 1 and parts[0].find('#') != 0 and parts[0].find('.') >= 0:
                enKeys.append(parts[0].strip())
        line = fp.readline()

print("")
print("- DE keys found: {0}".format(len(deKeys)))
print("- EN keys found: {0}".format(len(enKeys)))

for de in deKeys:
    stats[de] = 'de'

for en in enKeys:
    if en in stats.keys():
        del stats[en]
    else:
        stats[en] = 'en'

print("- diff found: {0}".format(len(stats)))

deCounter = 0
enCounter = 0

print("")
for key in sorted(stats.keys()):
    if stats[key] == 'de':
        print(" EN missing for: {0}".format(key))
        enCounter += 1

print("")
for key in sorted(stats.keys()):
    if stats[key] == 'en':
        print(" DE missing for: {0}".format(key))
        deCounter += 1

print("")
print("- missing EN translations: {0}".format(enCounter))
print("- missing DE translations: {0}".format(deCounter))
