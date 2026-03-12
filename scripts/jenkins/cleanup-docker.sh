#!/bin/bash
# Cleanup Docker resources after Jenkins build
# Removes dangling images, stopped containers, and unused networks

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Docker Cleanup ===${NC}\n"

# Remove dangling images (untagged images)
echo -e "${YELLOW}Removing dangling images...${NC}"
DANGLING=$(docker images -f "dangling=true" -q)
if [ -n "$DANGLING" ]; then
    docker rmi $DANGLING || echo -e "${YELLOW}Some images could not be removed${NC}"
    echo -e "${GREEN}✓ Dangling images removed${NC}"
else
    echo -e "${GREEN}✓ No dangling images found${NC}"
fi

# Remove stopped containers (older than 1 hour)
echo -e "\n${YELLOW}Removing old stopped containers...${NC}"
OLD_CONTAINERS=$(docker ps -a -f "status=exited" -f "status=created" --filter "until=1h" -q)
if [ -n "$OLD_CONTAINERS" ]; then
    docker rm $OLD_CONTAINERS || echo -e "${YELLOW}Some containers could not be removed${NC}"
    echo -e "${GREEN}✓ Old stopped containers removed${NC}"
else
    echo -e "${GREEN}✓ No old stopped containers found${NC}"
fi

# Remove unused networks (not attached to running containers)
echo -e "\n${YELLOW}Removing unused networks...${NC}"
UNUSED_NETWORKS=$(docker network ls -f "dangling=true" -q)
if [ -n "$UNUSED_NETWORKS" ]; then
    docker network rm $UNUSED_NETWORKS || echo -e "${YELLOW}Some networks could not be removed${NC}"
    echo -e "${GREEN}✓ Unused networks removed${NC}"
else
    echo -e "${GREEN}✓ No unused networks found${NC}"
fi

# Prune build cache (older than 24 hours)
echo -e "\n${YELLOW}Pruning old build cache...${NC}"
docker builder prune -a -f --filter "until=24h" > /dev/null 2>&1
echo -e "${GREEN}✓ Build cache pruned${NC}"

# Show disk usage after cleanup
echo -e "\n${YELLOW}=== Docker Disk Usage ===${NC}"
docker system df

echo -e "\n${GREEN}=== Cleanup Complete ===${NC}"
