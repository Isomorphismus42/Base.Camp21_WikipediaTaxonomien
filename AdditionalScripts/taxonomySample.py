"""
Einfaches Skript zum durchführen einer Stichporbe auf den Taxonomiedaten.
"""
import random


# Parameter: Verzeichnis der Daten und Stichprobengröße
def rateRandomLines(datei, stichprobengroesse):
    randomlist = []
    summe = 0
    num_lines = sum(1 for lines in open(datei))
    for j in range(0, stichprobengroesse):
        rdm = random.randint(1, num_lines)
        randomlist.append(rdm)

    with open(datei, "r", encoding='UTF-8') as f:
        for i, line in enumerate(f):
            if i in randomlist:
                print(f.readline())
                k = input("Taxonomie (1=ja,0=nein)")
                while not (k == "1" or k == "0"):
                    k = input("falsche Eingabe, Eingabe wiederholen:")
                summe = summe + int(k)
    print(float(summe) / stichprobengroesse)


if __name__ == '__main__':
    rateRandomLines('wiki_all_sorted_clean.txt', 100)
