import csv

data = [
    ['Name', 'Age', 'Country'],
    ['Alice', '30', 'USA'],
    ['Bob', '25', 'Canada'],
    ['Eva', '28', 'UK']
]

with open('CP1252.csv', 'w', newline='', encoding='cp1252') as f:
    writer = csv.writer(f)
    writer.writerows(data)

    