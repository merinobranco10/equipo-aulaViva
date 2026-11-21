#!/bin/bash
# Uso: ./restore.sh <archivo_backup.sql.gz>
BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
  echo "Por favor, proporciona el archivo de backup."
  exit 1
fi

echo "Restaurando base de datos desde ${BACKUP_FILE}..."
gunzip -c $BACKUP_FILE | psql -h $DB_HOST -U $DB_USER -d $DB_NAME

if [ $? -eq 0 ]; then
  echo "Restauración completada con éxito."
else
  echo "Error durante la restauración."
  exit 1
fi
