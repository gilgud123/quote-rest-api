# Jenkins CI/CD Setup Guide

This guide provides step-by-step instructions for setting up Jenkins for the Quote REST API project.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Initial Jenkins Setup](#initial-jenkins-setup)
4. [Plugin Installation](#plugin-installation)
5. [Verification](#verification)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

✅ **Docker Desktop** installed and running on Windows  
✅ **Docker Compose** available (included with Docker Desktop)  
✅ **Git** installed  
✅ **PowerShell 5.1+** for management scripts

---

## Quick Start

### 1. Start Jenkins Container

From the project root directory, run:

```powershell
# Start Jenkins
.\scripts\jenkins\jenkins-docker.ps1 start
```

Jenkins will start and be available at **http://localhost:8090**

Initial startup takes **1-2 minutes**.

### 2. Get Initial Admin Password

```powershell
# Retrieve the initial admin password
.\scripts\jenkins\jenkins-docker.ps1 password
```

Copy the password shown in the output.

### 3. Access Jenkins Web UI

1. Open your browser and navigate to: **http://localhost:8090**
2. Paste the initial admin password
3. Click **Continue**

---

## Initial Jenkins Setup

### Step 1: Unlock Jenkins

1. Enter the initial admin password you retrieved
2. Click **Continue**

### Step 2: Customize Jenkins

You'll see two options:

- **Install suggested plugins** (Recommended)
- **Select plugins to install**

**Choose**: **Install suggested plugins**

This will install essential plugins including:
- Git plugin
- Pipeline plugin
- GitHub plugin
- Credentials plugin
- SSH Build Agents plugin
- And more...

The installation takes **3-5 minutes**.

### Step 3: Create First Admin User

Fill in the form to create your admin account:

- **Username**: `admin` (or your preferred username)
- **Password**: Choose a strong password
- **Full name**: Your name
- **Email**: Your email address

Click **Save and Continue**.

> **Tip**: Write down your credentials in a secure location.

### Step 4: Instance Configuration

Jenkins will suggest a URL like `http://localhost:8090/`

Keep the default and click **Save and Finish**.

### Step 5: Start Using Jenkins

Click **Start using Jenkins**.

You're now on the Jenkins Dashboard! 🎉

---

## Plugin Installation

After completing the initial setup, install additional plugins required for the Quote REST API pipeline.

### Required Additional Plugins

Navigate to **Dashboard** → **Manage Jenkins** → **Plugins** → **Available plugins**

Search for and install the following plugins:

#### Git & GitHub (usually pre-installed)
- ✅ **Git Plugin** - Git SCM support
- ✅ **GitHub Plugin** - GitHub integration (includes webhook support)
- ✅ **GitHub Branch Source Plugin** - Multi-branch pipeline support

#### Maven & Build Tools
- ✅ **Maven Integration Plugin** - Maven project support
- ✅ **Pipeline Maven Integration Plugin** - Maven support in pipelines
- ✅ **Config File Provider** - Manage Maven settings

#### Docker
- ✅ **Docker Plugin** - Docker integration
- ✅ **Docker Pipeline** - Docker commands in pipeline
- ✅ **Docker Commons Plugin** - Docker common functionality

#### Testing & Reporting
- ✅ **JUnit Plugin** - Publish JUnit test results (usually pre-installed)
- ✅ **Code Coverage API Plugin** - Code coverage visualization (replaces deprecated JaCoCo plugin)
- ✅ **HTML Publisher Plugin** - Publish HTML reports
- ✅ **Test Results Analyzer Plugin** - Detailed test analysis

#### Build Parameters
- ✅ **Git Parameter Plugin** - Allow users to select Git branches/tags as build parameters

#### Optional but Recommended
- ✅ **Blue Ocean** - Modern Jenkins UI
- ✅ **Timestamper Plugin** - Timestamps in console output
- ✅ **Build Timeout Plugin** - Abort builds after timeout
- ✅ **Workspace Cleanup Plugin** - Clean workspace before builds
- ✅ **AnsiColor Plugin** - ANSI color in console output

### Installation Steps

1. **Dashboard** → **Manage Jenkins** → **Plugins**
2. Click **Available plugins** tab
3. Use the search box to find each plugin (note: plugins marked "usually pre-installed" may already be installed)
4. Check the box next to each plugin that isn't already installed
5. Click **Install** button (top right)
6. On the installation page, check **Restart Jenkins when installation is complete and no jobs are running**
7. Wait for installation to complete and Jenkins to restart

> **Note**: GitHub webhook functionality is included in the **GitHub Plugin**, not a separate plugin. The Git and GitHub plugins are usually installed automatically when you select "Install suggested plugins" during initial setup.

---

## Configuration

### Configure JDK

**Dashboard** → **Manage Jenkins** → **Tools** → **JDK installations**

1. Click **Add JDK**
2. Name: `JDK-17`
3. Uncheck **Install automatically**
4. JAVA_HOME: `/opt/java/openjdk` (Jenkins container uses this path)
5. Click **Save**

### Configure Maven

**Dashboard** → **Manage Jenkins** → **Tools** → **Maven installations**

1. Click **Add Maven**
2. Name: `Maven-3.9`
3. Check **Install automatically**
4. Version: Select latest `3.9.x`
5. Click **Save**

### Configure Docker

**Dashboard** → **Manage Jenkins** → **Tools** → **Docker installations**

1. Click **Add Docker**
2. Name: `Docker`
3. Uncheck **Install automatically**
4. Docker installation root: Leave empty (uses Docker from host)
5. Click **Save**

---

## Helper Scripts

The project includes PowerShell and Bash scripts for managing Jenkins:

### PowerShell Scripts (Windows)

```powershell
# Start Jenkins
.\scripts\jenkins\jenkins-docker.ps1 start

# Stop Jenkins
.\scripts\jenkins\jenkins-docker.ps1 stop

# Restart Jenkins
.\scripts\jenkins\jenkins-docker.ps1 restart

# View logs
.\scripts\jenkins\jenkins-docker.ps1 logs

# Check status
.\scripts\jenkins\jenkins-docker.ps1 status

# Get admin password
.\scripts\jenkins\jenkins-docker.ps1 password
```

### Bash Scripts (Git Bash / WSL / Linux)

```bash
# Wait for services to be ready
./scripts/jenkins/wait-for-services.sh postgres app keycloak

# Cleanup Docker resources
./scripts/jenkins/cleanup-docker.sh
```

---

## Verification

### Check Jenkins is Running

```powershell
.\scripts\jenkins\jenkins-docker.ps1 status
```

Expected output:
```
Jenkins Container Status:
NAMES           STATUS              PORTS
quote-jenkins   Up 5 minutes        0.0.0.0:8090->8080/tcp, 50000/tcp

Jenkins is RUNNING
Access at: http://localhost:8090
```

### Verify Installed Plugins

1. Navigate to **Dashboard** → **Manage Jenkins** → **Plugins**
2. Click **Installed plugins** tab
3. Search for each required plugin
4. Verify all are installed and up-to-date

### Test Jenkins Configuration

1. Go to **Dashboard** → **Manage Jenkins** → **System Information**
2. Verify:
   - `JAVA_HOME` is set
   - `MAVEN_HOME` is set (after configuration)
   - Docker is accessible

---

## Troubleshooting

### Jenkins Container Won't Start

**Problem**: `docker-compose up jenkins` fails or container exits immediately

**Solutions**:
```powershell
# Check Docker is running
docker ps

# Check Docker Compose file syntax
docker-compose config

# View Jenkins logs
docker logs quote-jenkins

# Remove old container and start fresh
docker rm -f quote-jenkins
docker volume rm quote-jenkins-data
.\scripts\jenkins\jenkins-docker.ps1 start
```

### Can't Access Jenkins at localhost:8090

**Problem**: Browser shows "Site can't be reached"

**Solutions**:
1. Wait 1-2 minutes for Jenkins to fully start
2. Check container is running: `docker ps | grep jenkins`
3. Check port is not in use: `netstat -ano | findstr :8090`
4. Try `http://127.0.0.1:8090` instead

### Initial Admin Password Not Found

**Problem**: Password retrieval fails

**Solutions**:
```powershell
# Wait for Jenkins to initialize (30-60 seconds)
Start-Sleep -Seconds 60

# Try again
.\scripts\jenkins\jenkins-docker.ps1 password

# Manual retrieval
docker exec quote-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Docker Socket Permission Denied

**Problem**: Pipeline fails with "permission denied" when accessing Docker

**Solutions**:
1. Ensure Docker Desktop is running with admin privileges
2. Verify Docker socket is mounted in docker-compose.yml:
   ```yaml
   volumes:
     - /var/run/docker.sock:/var/run/docker.sock
   ```
3. On Linux, add Jenkins user to docker group (not needed on Windows)

### Plugin Installation Fails

**Problem**: Plugin installation hangs or fails

**Solutions**:
1. Check internet connection
2. **Dashboard** → **Manage Jenkins** → **Plugins** → **Advanced settings**
3. Scroll down to "Update Site" section
4. Verify URL is: `https://updates.jenkins.io/update-center.json`
5. Click **Submit** then click **Check now** button
6. Try installing plugins again

### Out of Memory Errors

**Problem**: Jenkins becomes slow or crashes

**Solutions**:
1. Edit `docker-compose.yml` to increase memory:
   ```yaml
   jenkins:
     environment:
       JAVA_OPTS: "-Xmx2048m -Djenkins.install.runSetupWizard=false"
   ```
2. Restart Jenkins:
   ```powershell
   .\scripts\jenkins\jenkins-docker.ps1 restart
   ```

---

## Data Persistence

Jenkins data is stored in a Docker volume:

- **Volume name**: `quote-jenkins-data`
- **Contains**: 
  - Jenkins configuration
  - Job configurations
  - Build history
  - Plugins
  - User accounts

### Backup Jenkins Data

```powershell
# Create backup
docker run --rm -v quote-jenkins-data:/data -v ${PWD}:/backup alpine tar czf /backup/jenkins-backup.tar.gz -C /data .

# Restore backup
docker run --rm -v quote-jenkins-data:/data -v ${PWD}:/backup alpine tar xzf /backup/jenkins-backup.tar.gz -C /data
```

---

## Next Steps

✅ Jenkins is installed and running  
⏭️ Configure global tools (JDK, Maven, Docker)  
⏭️ Create your first pipeline job  
⏭️ Integrate with Git repository  
⏭️ Set up automated builds

Continue to the next section: **[Creating the Jenkinsfile](./JENKINSFILE_GUIDE.md)**

---

## Useful Links

- **Jenkins Dashboard**: http://localhost:8090
- **Jenkins Documentation**: https://www.jenkins.io/doc/
- **Pipeline Syntax Reference**: https://www.jenkins.io/doc/book/pipeline/syntax/
- **Plugin Index**: https://plugins.jenkins.io/

---

## Quick Reference

| Command | Description |
|---------|-------------|
| `.\scripts\jenkins\jenkins-docker.ps1 start` | Start Jenkins |
| `.\scripts\jenkins\jenkins-docker.ps1 stop` | Stop Jenkins |
| `.\scripts\jenkins\jenkins-docker.ps1 status` | Check status |
| `.\scripts\jenkins\jenkins-docker.ps1 password` | Get admin password |
| `.\scripts\jenkins\jenkins-docker.ps1 logs` | View logs |
| `docker exec -it quote-jenkins bash` | Access container shell |
| `docker logs -f quote-jenkins` | Follow logs |

---

**Need Help?**

- Check the Troubleshooting section above
- View Jenkins logs: `.\scripts\jenkins\jenkins-docker.ps1 logs`
- Restart Jenkins: `.\scripts\jenkins\jenkins-docker.ps1 restart`
- Check Docker status: `docker ps`
