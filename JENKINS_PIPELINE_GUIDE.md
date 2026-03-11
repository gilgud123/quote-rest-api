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
2. Click **+ New Item** (left sidebar)
3. Enter job name: `quote-rest-api-pipeline`
4. Select **Pipeline**
5. Click **OK**

### Step 2: Configure Job

#### General Settings

- ✅ **Description**: `CI/CD pipeline for Quote REST API`
- ✅ Check **Discard old builds**
  - Strategy: **Log Rotation**
  - Days to keep builds: (leave blank)
  - Max # of builds to keep: `10`

#### Build Triggers

Choose one or both:

**Option A: Poll SCM** (Check for changes every 5 minutes)
- ✅ Check **Poll SCM**
- Schedule: `H/5 * * * *`

**Option B: GitHub hook trigger** (Trigger on push - requires GitHub plugin)
- ✅ Check **GitHub hook trigger for GITScm polling**
- Then configure webhook in GitHub repository settings:
  - Go to repository → Settings → Webhooks → Add webhook
  - Payload URL: `http://your-jenkins-url:8090/github-webhook/`
  - Content type: `application/json`
  - Select: "Just the push event"

#### Pipeline Configuration

**Note**: If "Pipeline script from SCM" doesn't show SCM options, you may need to install the Git plugin first. See troubleshooting below.

1. **Definition**: Select `Pipeline script from SCM` from the dropdown
2. **SCM**: Select `Git` (this dropdown should appear)
3. **Repositories**:
   - **Repository URL**: Enter your Git repository URL
     - For local: `file:///workspace` (if using Docker volume)
     - For GitHub: `https://github.com/yourusername/quote-rest-api.git`
   - **Credentials**: Select "- none -" for public repos, or add credentials for private repos
4. **Branches to build**:
   - Branch Specifier (blank for 'any'): `*/main` (or `*/master`)
5. **Repository browser**: (Auto)
6. **Script Path**: `Jenkinsfile`

**Advanced Options** (click to expand if needed):
- **Lightweight checkout**: ✅ Checked (faster, recommended)

---

#### Adding Build Parameters (Optional)

Parameters allow you to customize builds at runtime, such as selecting which branch to build or passing configuration values.

**Step 1: Enable Parameters**

In the **General** section:
1. ✅ Check **This project is parameterized**
2. Click **Add Parameter** to add one or more parameters

**Step 2: Add Parameters**

Common parameter types:

**String Parameter** (for branch names, versions, etc.):
- Name: `BRANCH_NAME`
- Default Value: `main`
- Description: `Git branch to build`

**Choice Parameter** (dropdown selection):
- Name: `ENVIRONMENT`
- Choices (one per line):
  ```
  development
  staging
  production
  ```
- Description: `Target environment for deployment`

**Boolean Parameter** (checkbox):
- Name: `RUN_TESTS`
- Default: ✅ Checked
- Description: `Run unit and integration tests`

**Step 3: Use Parameters in Pipeline**

To use the branch parameter, update your **Branches to build** in Pipeline Configuration:
- Branch Specifier: `*/${BRANCH_NAME}` (instead of `*/main`)

The parameter will now appear when you click **Build with Parameters**.

**Example: Multiple Parameters**

You can add several parameters for flexible builds:

1. **BRANCH_NAME** (String): `main` - Branch to build
2. **SKIP_TESTS** (Boolean): unchecked - Skip test execution
3. **DOCKER_TAG** (String): `latest` - Docker image tag
4. **DEPLOY** (Boolean): unchecked - Deploy after build

**Using Parameters in Jenkinsfile**

Access parameters in your Jenkinsfile with `${params.PARAMETER_NAME}`:

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                echo "Building branch: ${params.BRANCH_NAME}"
                sh './mvnw clean package'
            }
        }
    }
}
```

**Building with Parameters**

Once parameters are added:
1. The **Build Now** button changes to **Build with Parameters**
2. Click it to see a form with your parameters
3. Modify values as needed
4. Click **Build** to start

---

#### Troubleshooting: No SCM Options Appear

If you don't see SCM/Repository fields after selecting "Pipeline script from SCM":

**Problem**: Git plugin not installed or not loaded

**Solution**:
1. Go to **Manage Jenkins** → **Plugins**
2. Click **Available plugins** tab
3. Search for "Git plugin"
4. Install if not present, or click **Restart Jenkins** if recently installed
5. Return to job configuration and try again

Alternatively, use **Pipeline script** option (inline):
- Select **Pipeline script** instead
- Paste your Jenkinsfile content directly
- This works without additional plugins

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
- ✅ **Green checkmark** (or blue ball in classic UI): Successful build
- ❌ **Red X** (or red ball in classic UI): Failed build  
- ⚠️ **Yellow warning** (or yellow ball in classic UI): Unstable (tests failed)
- ⚪ **Gray circle**: Not built yet
- 🔵 **Blue ball**: Running build (with progress bar)

### Test Results

Click on a build number → **Test Result** (left sidebar)

Shows:
- Total tests run
- Passed / Failed / Skipped
- Test duration and trends over time
- Package and class-level test details
- Detailed failure messages and stack traces

### Code Coverage

Click on a build number → **JaCoCo** (left sidebar)

Shows:
- Line coverage percentage
- Branch coverage percentage
- Instruction and complexity coverage
- Coverage trends over builds
- Detailed package and class-level coverage with drill-down

### Playwright Report

Click on a build number → **Playwright Test Report** (left sidebar)

Shows:
- HTML report with test details
- Test execution timeline
- Screenshots and videos (if any failures)
- Detailed test execution logs and traces
- Filter by passed/failed/skipped tests

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
- Click on build number → **Console Output** (left sidebar)
- Look for test failure details in the logs
- Check **Test Result** link for detailed failure messages
- Fix failing tests locally first
- Click **Build Now** to re-run

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

- Check **Console Output** (left sidebar) for detailed build logs
- Review **Test Result** (left sidebar) for test failures
- View **JaCoCo** (left sidebar) for code coverage reports
- Check **Pipeline Steps** for stage-by-stage execution details
- Check Jenkins container logs: `.\scripts\jenkins\jenkins-docker.ps1 logs`
