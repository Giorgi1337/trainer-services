#!/bin/bash

# logs.sh
# Script to follow Docker logs or open shell in running containers

# Define your containers
CONTAINERS=(
  "eureka-server"
  "training-service"
  "workload-service"
  "activemq"
  "mongodb"
)

# Function to display logs
function follow_logs() {
    echo "Following logs for all containers..."
    for container in "${CONTAINERS[@]}"; do
        echo "==== Logs for $container ===="
        docker logs -f "$container" &
    done
    wait
}

# Function to open shell in a container
function shell_container() {
    echo "Available containers:"
    for container in "${CONTAINERS[@]}"; do
        echo "- $container"
    done
    read -p "Enter container name to open shell: " target
    if [[ " ${CONTAINERS[*]} " =~ " $target " ]]; then
        echo "Opening shell in $target..."
        docker exec -it "$target" sh
    else
        echo "Container '$target' not found."
    fi
}

# Main menu
echo "Choose an option:"
echo "1) Follow logs for all containers"
echo "2) Open shell in a container"
read -p "Enter choice (1 or 2): " choice

case "$choice" in
    1)
        follow_logs
        ;;
    2)
        shell_container
        ;;
    *)
        echo "Invalid choice"
        ;;
esac
