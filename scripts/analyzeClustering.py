import sys
import pandas as pd
import csv

# Verifica se il nome del file di output è stato passato come argomento
if len(sys.argv) < 2:
    print('Specificare il file di output da analizzare')
    sys.exit(1)

output_filename = sys.argv[1]

count = 0
# 192
num_iter = int(len(pd.read_csv(output_filename, header=None))/2)

# Per poter stampare ad esempio 0_100_100 in output così da capire a quale settaggio si riferisce la percentuale
base = output_filename.replace(".csv", "")
target = base[::-1][:11][::-1]
if target.startswith("5"):
    target = "0" + target

with open(output_filename, newline='') as csvfile:
    reader = csv.reader(csvfile, delimiter=',')
    next(reader)
    # Itera sulle righe in coppie
    for training_row, trainingAndGround_row in zip(reader, reader):

        # Estrae i nomi dei file, al momento non servono ma magari potranno tornare utili
        trainingAndGround_filename = trainingAndGround_row[0]
        training_filename = training_row[0]

        # Estrae l'id del cluster per i file di training e di ground+training
        trainingAndGround_cluster_id = trainingAndGround_row[1]
        cluster_id = training_row[1]

        if cluster_id == trainingAndGround_cluster_id:
            count += 1

    percentuale = count / num_iter * 100
    print(f"La percentuale di corrispondenza dei cluster con settings {target} è del {percentuale}%")


