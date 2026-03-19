#!/bin/bash
# View logs for all frontend development services

echo "📋 Frontend Development Services Logs"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Press Ctrl+C to exit"
echo ""

# Follow logs from all services
docker-compose -f docker-compose-frontend.yml logs -f --tail=100
