FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# La imagen base trae el repositorio apt de Yarn, cuya clave de firma ya no es válida
# (NO_PUBKEY 62D54FD4003F6525). Con él, el apt-get update de la feature docker-in-docker
# falla y el Codespace queda en modo de recuperación. El proyecto no usa Yarn.
RUN rm -f /etc/apt/sources.list.d/yarn*
