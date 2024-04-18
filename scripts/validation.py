# Script utilizzato per la validazione
# Prende in input due parametri:
# --> distanceMatrix di training
# --> distanceMatrix di ground
import sys
import pandas as pd
import numpy as np
from sklearn_extra.cluster import KMedoids
from sklearn import metrics
import csv
import matplotlib.pyplot as plt
import seaborn as sns


# Input da linea di comando
file_training = sys.argv[1]
file_ground = sys.argv[2]

# Estrazione del nome del file
matrix_name_t = file_training[:-4]
matrix_name_g = file_ground[:-4]

# Lettura del file csv
csv_training = pd.read_csv(file_training, delimiter=',')
csv_ground = pd.read_csv(file_ground, delimiter=',')

# Estrazione dei nomi dei log
object_names = csv_ground.iloc[:, 0].values

# Conversione in array numpy (può essere fatto con meno righe evitando la conversione intermedia in DataFrame)
arrayTraining = csv_training.iloc[:, 1:]
arrayTraining = arrayTraining.to_records(index=False)
arrayTraining = arrayTraining.tolist()
arrayTraining = np.array(arrayTraining, dtype=object)
arrayGround = csv_ground.iloc[:, 1:]
arrayGround = arrayGround.to_records(index=False)
arrayGround = arrayGround.tolist()
arrayGround = np.array(arrayGround, dtype=object)

# Esecuzione del clustering sui dati di training
k_max = 0
best_clusterization = {}
silhouettes = []
for k in range(2, len(arrayTraining)):
    k_medoid_instance = KMedoids(n_clusters=k, metric="precomputed", method="pam", init="heuristic", max_iter=30000,
                                 random_state=len(arrayTraining) - 1).fit(arrayTraining)
    score = metrics.silhouette_score(arrayTraining, k_medoid_instance.labels_, metric="precomputed")
    silhouettes.append(score)
    if score > best_clusterization.get('silhouette', -1):
        best_clusterization['silhouette'] = score
        best_clusterization['k'] = k
        best_clusterization['labels'] = k_medoid_instance.labels_

k_medoids_ground = KMedoids(n_clusters=best_clusterization['k'], metric="precomputed", method="pam", init="heuristic", max_iter=30000,
                            random_state=len(arrayTraining) - 1).fit(arrayGround)

# max_silhouette = max(silhouettes)
# max_k = silhouettes.index(max_silhouette) + 2
#
# plt.plot(np.arange(2, len(arrayTraining)), silhouettes)
# plt.axvline(x=max_k, color='r', linestyle='--', label=f'Numero di clusters: {max_k}')
# plt.xlabel('Numero di Clusters (k)')
# plt.ylabel('Silhouette Score')
# plt.title('Silhouette Score vs. Numero di Clusters')
# plt.suptitle(matrix_name_t)
# plt.grid()
# plt.legend()
# plt.savefig(f".\\silhouette_of_{matrix_name_t}.png")

plt.figure()
ax = sns.heatmap(arrayGround.astype(float), cmap='magma')
xticks = np.arange(0, arrayGround.shape[0], 16)
yticks = np.arange(0, arrayGround.shape[0], 16)
ax.set_xticks(xticks)
ax.set_yticks(yticks)
# Aggiungi i numeri sulle etichette delle assi
plt.gca().set_xticklabels(xticks)
plt.gca().set_yticklabels(yticks)
plt.title(f'Heatmap - {matrix_name_g[15:-3]}')
plt.savefig(f"heatmap_repeating_{matrix_name_g}.png")

# Creazione dei cluster vuoti
clusters = [[] for _ in range(len(k_medoids_ground.labels_))]
# Popolamento dei cluster con i nomi dei log corrispondenti
for i, label in enumerate(k_medoids_ground.labels_):
    clusters[label].append(object_names.tolist()[i])
# Nome del file di output
output_file = matrix_name_t + '_clusters.csv'
# Apertura del file in scrittura
with open(output_file, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    # Scrittura dei nomi dei cluster come header
    writer.writerow([f'Cluster {i+1}' for i in range(best_clusterization['k'])])
    # Scrittura dei nomi dei log per ogni cluster
    for i in range(len(max(clusters, key=len))):
        row = [clusters[j][i] if i < len(clusters[j]) else '' for j in range(best_clusterization['k'])]
        writer.writerow(row)
