#!/bin/bash
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="backup_${DB_NAME}_${TIMESTAMP}.sql.gz"

echo "Iniciando backup de la base de datos ${DB_NAME}..."
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME | gzip > $BACKUP_FILE

if [ $? -eq 0 ]; then
  echo "Backup exitoso: $BACKUP_FILE"
else
  echo "Error en el backup"
  exit 1
fi
