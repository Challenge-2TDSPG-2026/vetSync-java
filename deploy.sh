#!/bin/bash
# ============================================================
# Script Azure CLI — JornadaPet (FIAP DevOps Sprint 1)
# Provisiona VM, instala Docker e sobe a aplicação
# ============================================================

# --- Variáveis ---
RESOURCE_GROUP="rg-jornadapet"
LOCATION="brazilsouth"
VM_NAME="vm-jornadapet"
VM_IMAGE="Ubuntu2204"
VM_SIZE="Standard_B1s"
ADMIN_USER="azureuser"
NSG_NAME="nsg-jornadapet"
REPO_URL="https://github.com/felipeflosi/JornadaPet-java-Sprint1"

# --- 1. Criar Resource Group ---
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION

# --- 2. Criar VM Linux ---
az vm create \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --image $VM_IMAGE \
  --size $VM_SIZE \
  --admin-username $ADMIN_USER \
  --generate-ssh-keys \
  --output json

# Capturar IP público
PUBLIC_IP=$(az vm show \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --show-details \
  --query publicIps \
  --output tsv)

echo "IP da VM: $PUBLIC_IP"

# --- 3. Abrir portas necessárias ---
# Porta 8080 — API Spring Boot
az vm open-port \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --port 8080 \
  --priority 1001

# Porta 22 — SSH (já aberta por padrão, mas explícito para o script)
az vm open-port \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --port 22 \
  --priority 1000

# --- 4. Instalar Docker, Git e ferramentas na VM ---
az vm run-command invoke \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --command-id RunShellScript \
  --scripts "
    # Atualizar pacotes
    apt-get update -y

    # Instalar utilitários
    apt-get install -y git nano curl wget unzip

    # Instalar Docker
    curl -fsSL https://get.docker.com | sh

    # Adicionar azureuser ao grupo docker (rodar sem sudo)
    usermod -aG docker $ADMIN_USER

    # Instalar Docker Compose plugin
    apt-get install -y docker-compose-plugin

    # Habilitar Docker no boot
    systemctl enable docker
    systemctl start docker

    echo 'Instalação concluída'
  "

# --- 5. Clonar repositório e subir aplicação ---
az vm run-command invoke \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --command-id RunShellScript \
  --scripts "
    cd /home/$ADMIN_USER

    # Clonar projeto
    git clone $REPO_URL app
    cd app

    # Subir em background com Docker Compose
    docker compose up -d --build

    echo 'Aplicação no ar!'
    docker compose ps
  "

echo ""
echo "=============================="
echo "Deploy concluído!"
echo "API:        http://$PUBLIC_IP:8080"
echo "Swagger:    http://$PUBLIC_IP:8080/swagger-ui.html"
echo "H2 Console: http://$PUBLIC_IP:8080/h2-console"
echo "=============================="

# --- FIM ---
# Para deletar tudo ao final da apresentação:
# az group delete --name $RESOURCE_GROUP --yes --no-wait
