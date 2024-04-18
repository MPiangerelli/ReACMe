import matplotlib.pyplot as plt

import numpy as np
import pandas as pd

title = "Jaccard - 5Grams"
error_columns = ["1%", "3%", "5%", "10%", "20%", "30%", "50%", "70%", "90%", "100%"]
configuration_rows = ["0_100_100", "0_100_101", "0_5_100_100", "0_5_100_101",
                      "0_5_101_100", "05_101_101", "1_100_100", "1_101_100"]
data = np.array([
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
    [100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00, 100.00],
])

dataFrame = pd.DataFrame(data, index=configuration_rows, columns=error_columns)

fig, ax = plt.subplots(figsize=(12, 5))
im = ax.imshow(data, cmap="RdYlGn", aspect="auto")
ax.set_xticks(np.arange(len(error_columns)), labels=error_columns)
ax.set_yticks(np.arange(len(configuration_rows)), labels=configuration_rows)
for i in range(len(error_columns)):
    for j in range(len(configuration_rows)):
        text = ax.text(i, j, data[j, i], ha="center", va="center")

ax.spines[:].set_visible(True)
ax.spines[:].set_color('white')
ax.set_xticks(np.arange(data.shape[1] + 1) - .5, minor=True)
ax.set_yticks(np.arange(data.shape[0] + 1) - .5, minor=True)
ax.grid(which="minor", color="w", linestyle="-", linewidth=2)
ax.set_title(title)
ax.tick_params(axis='both', which="major", labelsize=12)
im.set_clim(vmin=0, vmax=100)
fig.set_size_inches(12, 6)
fig.colorbar(im, ticks=[0, 20, 40, 60, 80, 100])
fig.tight_layout()
fig.show()
