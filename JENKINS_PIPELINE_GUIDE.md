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
   - **Important**: If using Git Parameter (see below), change this to `${BRANCH_NAME}`
5. **Repository browser**: (Auto)
6. **Script Path**: `Jenkinsfile` (case-sensitive - must match filename exactly)

**Advanced Options** (click to expand if needed):
- **Lightweight checkout**: ✅ Checked (faster, recommended)

---

#### Adding Build Parameters (Recommended)

The pipeline includes built-in parameters defined in the Jenkinsfile itself. No additional Jenkins configuration required!

**Built-in Parameters** (automatically available):

1. **BRANCH_NAME** (String Parameter)
   - Default Value: `main`
   - Description: `Git branch name to checkout and test`
   - **How to use**: Enter just the branch name (e.g., `jenkins-setup`, `develop`, `feature/new-api`)
   - **⚠️ IMPORTANT**: Do NOT include "origin/" prefix - just the branch name

2. **MAVEN_PROFILE** (Choice Parameter)
   - Choices: `default`, `dev`, `prod`
   - Description: `Maven profile to use for the build`
   - Default: `default`

**How It Works**:
- Parameters are defined directly in `Jenkinsfile` using the `parameters` block
- On first build, Jenkins uses default values
- From second build onwards, **Build with Parameters** button appears
- No manual parameter configuration needed in Jenkins UI!

**Example Jenkinsfile parameters block**:
```groovy
pipeline {
    parameters {
        string(
            name: 'BRANCH_NAME',
            defaultValue: 'main',
            description: 'Git branch name to checkout and test (e.g., main, develop, feature/new-api). Do NOT include "origin/" prefix.'
        )
        choice(
            name: 'MAVEN_PROFILE',
            choices: ['default', 'dev', 'prod'],
            description: 'Maven profile to use for the build'
        )
    }
    // ... rest of pipeline
}
```

**Adding Custom Parameters**:

If you need additional parameters, edit the `parameters` block in your Jenkinsfile:

**String Parameter**:
```groovy
string(
    name: 'VERSION',
    defaultValue: '1.0.0',
    description: 'Application version number'
)
```

**Choice Parameter**:
```groovy
choice(
    name: 'ENVIRONMENT',
    choices: ['development', 'staging', 'production'],
    description: 'Target environment for deployment'
)
```

**Boolean Parameter**:
```groovy
booleanParam(
    name: 'SKIP_TESTS',
    defaultValue: false,
    description: 'Skip unit and integration tests'
)
```

**⚠️ Git Parameter Plugin NOT Recommended**:
- While Git Parameter plugin offers a dropdown of branches, it has complex configuration
- It causes issues with branch name formatting (origin/ prefix problems)
- Simple String parameter is more reliable and user-friendly
- Users can easily copy-paste branch names from Git/GitHub

**Pipeline Configuration for Branch Parameter**:

The Jenkinsfile includes a custom checkout stage that handles branch selection:

```groovy
stage('Checkout') {
    steps {
        echo "📦 Checking out branch: ${params.BRANCH_NAME}"
        script {
            // Clean branch name (remove origin/ prefix if present)
            def branchName = params.BRANCH_NAME.replaceAll(/^origin\//, '')
            checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${branchName}"]],
                userRemoteConfigs: scm.userRemoteConfigs,
                extensions: [[$class: 'CloneOption', depth: 0, noTags: false, shallow: false]]
            ])
        }
        sh 'git log -1 --oneline'
        sh "echo 'Testing branch: ${params.BRANCH_NAME}'"
    }
}
```

This stage:
- Accepts branch name with or without "origin/" prefix
- Automatically cleans the branch name
- Performs a full clone for reliable checkout
- Displays the commit being tested

**Using Parameters in Jenkinsfile**

Access parameters in your Jenkinsfile with `${params.PARAMETER_NAME}`:

```groovy
pipeline {
    agent any
    
    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'main', description: 'Branch to build')
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip tests')
    }
    
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

**First Build**:
1. Click **Build Now** - uses default parameter values
2. After first build completes, the button changes to **Build with Parameters**

**Subsequent Builds**:
1. Click **Build with Parameters**
2. A form appears with your parameters
3. **For BRANCH_NAME**: Enter just the branch name
   - ✅ Correct: `jenkins-setup`, `main`, `feature/new-api`
   - ❌ Wrong: `origin/jenkins-setup` (no origin/ prefix needed)
4. Modify other parameters as needed
5. Click **Build** to start

**Example Build Parameters**:
- **BRANCH_NAME**: `jenkins-setup` (to test jenkins-setup branch)
- **MAVEN_PROFILE**: `dev` (to use development profile)

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

### ERROR: Unable to find Jenkinsfile from git

**Problem**: Build fails with "Unable to find Jenkinsfile from git [repository-url]"

**Common Causes & Solutions**:

1. **Branch Specifier is incorrect when using Git Parameter**
   - Go to job → **Configure** → **Pipeline** section
   - Check **Branches to build** → **Branch Specifier**
   - If using Git Parameter, it MUST be: `${BRANCH_NAME}` (not `*/${BRANCH_NAME}`)
   - Click **Save**

2. **Jenkinsfile doesn't exist in the selected branch**
   - Verify which branch contains the Jenkinsfile:
     ```bash
     git ls-tree -r --name-only origin/main | grep Jenkinsfile
     git ls-tree -r --name-only origin/master | grep Jenkinsfile
     ```
   - Update Branch Specifier to the correct branch
   - Or merge Jenkinsfile to your main branch

3. **Wrong Script Path**
   - Script Path is case-sensitive
   - Default should be: `Jenkinsfile` (capital J)
   - Check if your file is named differently: `jenkinsfile`, `Jenkinsfile.groovy`, etc.
   - Update **Script Path** to match exact filename

4. **Default branch name mismatch**
   - Check repository default branch: `git remote show origin`
   - Update Branch Specifier to match (e.g., `*/master` instead of `*/main`)

### ERROR: couldn't find remote ref refs/heads/origin/[branch]

**Problem**: Build fails with "fatal: couldn't find remote ref refs/heads/origin/jenkins-setup"

**Cause**: Branch parameter includes "origin/" prefix when it shouldn't.

**Solution**:
1. When using **Build with Parameters**, enter ONLY the branch name:
   - ✅ Correct: `jenkins-setup`
   - ❌ Wrong: `origin/jenkins-setup`
   
2. The Jenkinsfile automatically adds the origin/ prefix during checkout

3. If you accidentally entered `origin/jenkins-setup`:
   - Click **Build with Parameters** again
   - Change `BRANCH_NAME` to just `jenkins-setup`
   - Click **Build**

**Why This Happens**:
- The parameter field accepts plain branch names
- The checkout stage automatically formats it as `*/jenkins-setup`
- Git resolves this to `refs/remotes/origin/jenkins-setup`
- Adding "origin/" manually causes double prefix: `origin/origin/jenkins-setup` ❌

**Verification**:
Check the Console Output - you should see:
```
📦 Checking out branch: jenkins-setup
Testing branch: jenkins-setup
```
Not:
```
📦 Checking out branch: origin/jenkins-setup  ❌
```

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

### docker-compose: not found

**Problem**: Build fails with "docker-compose: not found"

**Solution**:
The Jenkinsfile has been updated to use `docker compose` (v2 syntax) instead of `docker-compose` (v1).

If you're using an older Jenkinsfile:
1. Replace all instances of `docker-compose` with `docker compose` (note the space)
2. This is the modern Docker Compose v2 command that's built into Docker
3. No separate docker-compose installation needed

**Changes made in Jenkinsfile**:
```groovy
// Old (v1)
sh 'docker-compose up -d'
sh 'docker-compose down'

// New (v2)
sh 'docker compose up -d'
sh 'docker compose down'
```

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
