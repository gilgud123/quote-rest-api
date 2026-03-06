# Jenkins Pipeline Quick Start

This guide helps you create and run the Jenkins pipeline for the Quote REST API project.

## Prerequisites

✅ Jenkins installed and running (see [JENKINS_SETUP.md](./JENKINS_SETUP.md))  
✅ Required plugins installed  
✅ Docker accessible from Jenkins

---

## Create Pipeline Job

### Step 1: Create New Job

1. Open Jenkins at **http://localhost:8090**
2. Click **New Item** (top left)
3. Enter job name: `quote-rest-api-pipeline`
4. Select **Pipeline**
5. Click **OK**

### Step 2: Configure Job

#### General Settings

- ✅ **Description**: `CI/CD pipeline for Quote REST API`
- ✅ Check **Discard old builds**
  - Strategy: Log Rotation
  - Max # of builds to keep: `10`

#### Build Triggers

Choose one or both:

**Option A: Poll SCM** (Check for changes every 5 minutes)
- ✅ Check **Poll SCM**
- Schedule: `H/5 * * * *`

**Option B: GitHub Webhook** (Trigger on push)
- Configure webhook in GitHub repository settings
- Payload URL: `http://your-jenkins-url:8090/github-webhook/`

#### Pipeline Configuration

1. **Definition**: `Pipeline script from SCM`
2. **SCM**: `Git`
3. **Repository URL**: Enter your Git repository URL
   - For local: `file:///workspace` (if using Docker volume)
   - For GitHub: `https://github.com/yourusername/quote-rest-api.git`
4. **Branch Specifier**: `*/main` (or your default branch)
5. **Script Path**: `Jenkinsfile`

#### Advanced Options (Optional)

- **Lightweight checkout**: ✅ Checked (faster)

### Step 3: Save and Build

1. Click **Save**
2. Click **Build Now** to run the first build
3. Watch the build progress in real-time

---

## Pipeline Stages

The pipeline executes these stages in order:

| Stage | Duration | Description |
|-------|----------|-------------|
| 📦 Checkout | ~5s | Clone repository |
| 🔨 Build | ~30s | Maven compile |
| 🧪 Unit Tests | ~20s | Run unit tests |
| ✨ Code Quality | ~10s | Spotless formatting check |
| 🔧 Integration Tests | ~45s | Testcontainers tests |
| 📊 Code Coverage | ~15s | Generate JaCoCo report |
| 📦 Package | ~20s | Create JAR file |
| 🚀 Start Services | ~60s | Start postgres, keycloak, app |
| 🎭 Playwright Tests | ~45s | API end-to-end tests |
| 🐳 Docker Build | ~90s | Build Docker image |

**Total Duration**: ~8-12 minutes

---

## Viewing Results

### Build Status

On the job page, you'll see:
- ✅ **Blue ball**: Successful build
- ❌ **Red ball**: Failed build  
- ⚠️ **Yellow ball**: Unstable (tests failed)
- ⚪ **Gray ball**: Not built yet

### Test Results

Click on a build number → **Test Results**

Shows:
- Total tests run
- Passed / Failed / Skipped
- Test trends over time
- Individual test details

### Code Coverage

Click on a build number → **Coverage Report**

Shows:
- Line coverage percentage
- Branch coverage percentage
- Coverage trends
- Detailed class-level coverage

### Playwright Report

Click on a build number → **Playwright Test Report**

Shows:
- HTML report with test details
- Screenshots (if any failures)
- Detailed test execution logs

---

## Pipeline Configuration

### Environment Variables

Defined in `Jenkinsfile`:

```groovy
environment {
    MAVEN_OPTS = '-Xmx1024m'
    DOCKER_IMAGE = 'quote-rest-api'
    DOCKER_TAG = "${env.BUILD_NUMBER}"
}
```

### Customizing the Pipeline

Edit `Jenkinsfile` to customize:

#### Skip Certain Stages

Comment out stages you don't need:

```groovy
// stage('Playwright API Tests') {
//     steps {
//         // ... skipped
//     }
// }
```

#### Change Maven Options

```groovy
MAVEN_CLI_OPTS = '--batch-mode --errors --fail-at-end'
```

#### Adjust Timeouts

```groovy
options {
    timeout(time: 45, unit: 'MINUTES')  // Increase if builds are slow
}
```

#### Add Notifications

In `post` section:

```groovy
post {
    success {
        emailext (
            to: 'team@example.com',
            subject: "Build ${env.BUILD_NUMBER} - SUCCESS",
            body: "Build completed successfully!"
        )
    }
}
```

---

## Troubleshooting

### Build Fails at "Checkout" Stage

**Problem**: Can't clone repository

**Solution**:
- Verify Git repository URL is correct
- Check credentials if private repository
- Ensure Git is installed in Jenkins container

### Build Fails at "Unit Tests" Stage

**Problem**: Tests fail

**Solution**:
- Click on build → **Console Output**
- Look for test failure details
- Fix failing tests locally first
- Re-run build

### Build Fails at "Start Services" Stage

**Problem**: Docker containers won't start

**Solution**:
```bash
# Check if services are already running
docker ps | grep quote

# Stop existing services
docker-compose down

# Clean up and retry
docker system prune -f
```

### Playwright Tests Timeout

**Problem**: Tests hang or timeout

**Solution**:
- Increase wait time in `wait-for-services.sh`
- Check services are healthy: `docker ps`
- Verify network connectivity between containers

### Docker Build Fails

**Problem**: Can't build Docker image

**Solution**:
- Ensure Docker socket is mounted in Jenkins
- Check Dockerfile exists and is valid
- Verify disk space: `docker system df`

### Pipeline Hangs

**Problem**: Build never completes

**Solution**:
- Check Jenkins logs: `.\scripts\jenkins\jenkins-docker.ps1 logs`
- Abort the build manually
- Increase timeout in pipeline options
- Check for zombie containers: `docker ps -a`

---

## Best Practices

### 1. Keep Builds Fast
- Run unit tests before slow integration tests
- Use Docker layer caching
- Parallelize independent stages (if needed)

### 2. Fail Fast
- Run code quality checks early
- Stop build on first major failure

### 3. Clean Workspace
- Use `cleanWs()` in post section
- Remove build artifacts after archiving

### 4. Monitor Trends
- Check test result trends
- Monitor code coverage changes
- Review build duration trends

### 5. Secure Credentials
- Never hardcode passwords in Jenkinsfile
- Use Jenkins credentials store
- Use environment variables for secrets

---

## Useful Commands

### Jenkins CLI

```powershell
# Trigger build from command line (if CLI installed)
java -jar jenkins-cli.jar -s http://localhost:8090/ build quote-rest-api-pipeline

# Check build status
java -jar jenkins-cli.jar -s http://localhost:8090/ get-job quote-rest-api-pipeline
```

### Docker

```powershell
# View running containers
docker ps

# Stop all containers
docker-compose down

# Clean up resources
.\scripts\jenkins\cleanup-docker.sh
```

### Maven

```powershell
# Run locally (same as Jenkins)
./mvnw clean verify
./mvnw spotless:check
./mvnw jacoco:report
```

---

## Next Steps

✅ Pipeline created and running  
⏭️ Set up build notifications  
⏭️ Configure deployment stages  
⏭️ Add security scanning (OWASP, SonarQube)  
⏭️ Implement blue-green deployment  

---

## Resources

- **Jenkinsfile**: [Pipeline syntax documentation](https://www.jenkins.io/doc/book/pipeline/syntax/)
- **Plugins**: [Jenkins plugin index](https://plugins.jenkins.io/)
- **Best Practices**: [Jenkins best practices guide](https://www.jenkins.io/doc/book/pipeline/pipeline-best-practices/)

---

**Need Help?**

- Check **Console Output** for detailed logs
- Review **Test Results** for test failures
- View **Coverage Report** for code coverage
- Check Jenkins logs: `.\scripts\jenkins\jenkins-docker.ps1 logs`
