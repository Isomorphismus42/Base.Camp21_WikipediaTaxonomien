"""
Einfaches Script zum entfernen Von Reflexiven und Symmetrischen Relationen,
sowie möglichen Leereinträgen in den Spalten.
"""


#  Parameter: Verzeichnis der Daten
def cleanData(datei):
    summeReflexiv = 0
    summeSymmetrisch = 0
    summeLeer = 0
    menge = set()
    with open(datei, "r", encoding='UTF-8', newline='\n') as f:
        taxonomienClean = open(datei[:-4] + "_clean.txt", "x", encoding='UTF-8', newline='\n')
        ref = open(datei[:-4] + "_entfernteReflexionen.txt", "x", encoding='UTF-8', newline='\n')
        symm = open(datei[:-4] + "_entfernteSymmetrien.txt", "x", encoding='UTF-8', newline='\n')
        for line in f:
            colums = line.lower().split("\t")
            if (colums[0] == colums[1]):
                summeReflexiv += 1
                ref.write(line)
            elif (colums[0] == "" or colums[1] == ""):
                summeLeer += 1
            elif colums[1] + "\t" + colums[0] in menge:
                summeSymmetrisch += 1
                symm.write(line)
            else:
                menge.add(colums[0] + "\t" + colums[1])
                taxonomienClean.write(line)
    print("Reflexiv entfernt: " + str(summeReflexiv))
    print("Symmetrisch entfernt: " + str(summeSymmetrisch))
    print("Leereinträge entfernt: " + str(summeLeer))


if __name__ == '__main__':
    datei = 'wiki_all_sorted_final.txt'  # Name/Pfad der zu bereinigen Text-Datei.
    cleanData(datei)
