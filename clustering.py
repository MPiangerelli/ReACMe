# Script utilizzato dal tool per il clustering
import os.path

import pandas as pd
import numpy as np
from sklearn_extra.cluster import KMedoids
from sklearn import metrics
import sys
import csv

# Input da linea di comando
file_training = sys.argv[1]

# Estrazione del nome del file
matrix_name_t = file_training[:-4]

# Lettura del file csv
csv_training = pd.read_csv(file_training, delimiter=',')

# Estrazione dei nomi dei log
object_names = csv_training.iloc[:, 0].values

# Conversione in array numpy
arrayTraining = csv_training.iloc[:, 1:]
arrayTraining = arrayTraining.to_records(index=False)
arrayTraining = arrayTraining.tolist()
arrayTraining = np.array(arrayTraining, dtype=object)

# Esecuzione del clustering e ricerca del valore k migliore
k_max = 0
best_clusterization = {}
silhouettes = []
for k in range(2, len(arrayTraining)):
    kmedoids_instance_t = KMedoids(n_clusters=k, metric="precomputed", method="pam", init="heuristic", max_iter=30000,
                                   random_state=len(arrayTraining) - 1).fit(arrayTraining)
    score = metrics.silhouette_score(arrayTraining, kmedoids_instance_t.labels_, metric="precomputed")
    silhouettes.append(score)
    if score > best_clusterization.get('silhouette', -1):
        best_clusterization['silhouette'] = score
        best_clusterization['k'] = k
        best_clusterization['labels'] = kmedoids_instance_t.labels_

# Creazione dei cluster vuoti
clusters = [[] for _ in range(best_clusterization['k'])]
# Popolamento dei cluster con i nomi dei log corrispondenti
for i, label in enumerate(best_clusterization['labels']):
    clusters[label].append(object_names.tolist()[i])
# Nome del file di output
output_file = matrix_name_t + '_clusters.csv'
# Apertura del file in scrittura
with open(output_file, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    # Scrittura dei nomi dei cluster come header
    writer.writerow([f'Cluster {i + 1}' for i in range(best_clusterization['k'])])
    # Scrittura dei nomi dei log per ogni cluster
    for i in range(len(max(clusters, key=len))):
        row = [clusters[j][i] if i < len(clusters[j]) else '' for j in range(best_clusterization['k'])]
        writer.writerow(row)
