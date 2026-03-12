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

The pipeline supports branch selection using the Git Parameter plugin, giving you a dropdown of available branches.

**Prerequisites:**
1. **Git Parameter Plugin** must be installed (see installation steps below)
2. **Jenkinsfile must declare the Git Parameter** via a top-level `properties([...])` call (already included in the project)

**How It Works:**
- Jenkinsfile declares the `BRANCH_NAME` Git Parameter via a top-level `properties([parameters([gitParameter(...)])])` call, placed **before** the `pipeline { }` block
- On the first build, Jenkins reads this declaration and automatically registers the branch dropdown — no manual UI configuration is needed
- You select which branch to test from the dropdown
- Pipeline checks out and tests the selected branch

---

### Installing Git Parameter Plugin

**Step 1: Install Plugin**
1. Go to **Manage Jenkins** (left sidebar)
2. Click **Plugins**
3. Click **Available plugins** tab
4. Search for: `Git Parameter`
5. Check the box next to "Git Parameter"
6. Click **Install**
7. After installation: **Manage Jenkins** → **Restart Safely**

**Step 2: No Manual UI Configuration Needed**

Because the Jenkinsfile uses a top-level `properties([parameters([gitParameter(...)])])` call, Jenkins automatically registers the `BRANCH_NAME` branch dropdown after the first build. You do **not** need to manually check "This project is parameterized" or configure the Git Parameter in the Jenkins UI — the Jenkinsfile handles this for you.

**Step 3: Verify Parameter Settings (Optional)**

After the first build you can confirm the parameter was registered correctly by going to **Configure** on the job:

- **Name**: `BRANCH_NAME`
- **Parameter Type**: `Branch`
- **Default Value**: `master`
- **Branch Filter**: `origin/(.*)`
- **Selected Value**: `DEFAULT`

These values are sourced directly from the `gitParameter(...)` call in the Jenkinsfile and should not need manual adjustment.

**Step 4: Configure Pipeline Section**

Scroll to **Pipeline** section:
- **Definition**: `Pipeline script from SCM`
- **SCM**: `Git`
- **Repository URL**: Your Git repository URL
- **Branch Specifier**: `*/jenkins-setup` (or whatever branch contains your Jenkinsfile)
  - ⚠️ This must be a FIXED branch where Jenkins reads the Jenkinsfile
  - Do NOT use `${BRANCH_NAME}` here - that's for testing branches, not for finding the Jenkinsfile
- **Script Path**: `Jenkinsfile`

**Step 5: Jenkinsfile `properties([...])` Declaration**

The Jenkinsfile uses a top-level `properties([parameters([gitParameter(...)])])` call **outside** the `pipeline { }` block to declare the branch parameter. This is already present in the project:

```groovy
// Declare the Git Parameter BEFORE the pipeline block.
// Jenkins reads this on every run and keeps the parameter configuration
// in sync with the Jenkinsfile, preventing it from being lost between builds.
properties([
    parameters([
        gitParameter(
            name: 'BRANCH_NAME',
            type: 'PT_BRANCH',
            defaultValue: 'master',
            description: 'Select branch to build',
            branchFilter: 'origin/(.*)',
            selectedValue: 'DEFAULT'
        )
    ])
])

pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    // Git Parameter returns format like "origin/jenkins-setup"
                    // Extract just the branch name part after the last /
                    def branchName = params.BRANCH_NAME.contains('/') ? 
                        params.BRANCH_NAME.substring(params.BRANCH_NAME.lastIndexOf('/') + 1) : 
                        params.BRANCH_NAME
                    
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${branchName}"]],
                        userRemoteConfigs: scm.userRemoteConfigs
                    ])
                }
            }
        }
    }
}
```

**Why `properties([...])` instead of a `parameters { }` block inside `pipeline { }`:**
- The top-level `properties([...])` call is the correct way to register the Git Parameter plugin's `gitParameter()` step in a Declarative Pipeline
- It runs on every build and keeps the parameter definition in sync with the Jenkinsfile, so the branch dropdown is never lost between builds
- A plain `parameters { string(...) }` block inside `pipeline { }` does not support `gitParameter()` — it only handles built-in parameter types like `string`, `choice`, etc.

**Step 6: Save and Test**

1. Click **Save**
2. Click **Build Now** once (first build uses default branch)
3. After first build, **Build with Parameters** button appears
4. Click it - you should see dropdown with all branches:
   - `master`
   - `jenkins-setup`
   - `mcp-tests`
   - etc.
5. Select a branch and click **Build**

---

### How Branch Selection Works

**Two Different Branches in Play:**

1. **Branch Specifier** (`*/jenkins-setup` in Pipeline config):
   - Fixed branch where Jenkins READS the Jenkinsfile
   - Doesn't change when you select different branches to test
   - Example: Always use `jenkins-setup` branch for the pipeline definition

2. **BRANCH_NAME Parameter** (dropdown selection):
   - Branch you want to TEST
   - User selects from dropdown
   - Pipeline checks out this branch in the Checkout stage
   - Example: You can test `master`, `mcp-tests`, or any other branch

**Workflow:**
```
1. User clicks "Build with Parameters"
2. User selects "mcp-tests" from BRANCH_NAME dropdown
3. Jenkins reads Jenkinsfile from "jenkins-setup" branch (Branch Specifier)
4. Checkout stage checks out "mcp-tests" branch (BRANCH_NAME parameter)
5. Tests run on "mcp-tests" branch
```

This allows you to keep your pipeline definition in one branch (e.g., `jenkins-setup`) while testing any other branch.

**Adding Additional Custom Parameters**

Beyond the branch selection, you can add more parameters in the Jenkinsfile:

**Boolean Parameter**:
```groovy
parameters {
    string(name: 'BRANCH_NAME', defaultValue: 'master', description: 'Branch to test')
    booleanParam(
        name: 'SKIP_TESTS',
        defaultValue: false,
        description: 'Skip unit and integration tests'
    )
}
```

**Choice Parameter**:
```groovy
choice(
    name: 'ENVIRONMENT',
    choices: ['development', 'staging', 'production'],
    description: 'Target environment for deployment'
)
```

These additional parameters will appear alongside the Git Parameter dropdown.

**Using Parameters in Jenkinsfile**

Access parameters in your Jenkinsfile with `${params.PARAMETER_NAME}`:

```groovy
pipeline {
    agent any
    
    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'master', description: 'Branch to test')
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip tests')
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    // Extract branch name from parameter
                    def branchName = params.BRANCH_NAME.contains('/') ? 
                        params.BRANCH_NAME.substring(params.BRANCH_NAME.lastIndexOf('/') + 1) : 
                        params.BRANCH_NAME
                    echo "Testing branch: ${branchName}"
                }
            }
        }
        
        stage('Build') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                sh './mvnw clean package'
            }
        }
    }
}
```

**Building with Parameters**

**First Build**:
1. Click **Build Now** - uses default parameter value
2. After first build completes, the button changes to **Build with Parameters**

**Subsequent Builds**:
1. Click **Build with Parameters**
2. A form appears with your parameters:
   - **BRANCH_NAME**: Dropdown with all available branches
   - Select the branch you want to test (e.g., `jenkins-setup`, `master`, `mcp-tests`)
3. Modify any other parameters as needed
4. Click **Build** to start

**The dropdown will show branch names like:**
- `master`
- `jenkins-setup`
- `mcp-tests`
- `feature/new-feature`

Just select the branch from the dropdown - no need to type anything!

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

The exact stages and their order are defined in the `Jenkinsfile` in this repository. Open that file in your editor or via the Git hosting UI to see the authoritative list of stages that will run in your pipeline.

Below is a typical high-level breakdown of what the pipeline does:

| Stage (typical)        | Purpose                                  |
|------------------------|------------------------------------------|
| 📦 Checkout            | Clone repository and fetch source code   |
| 🔨 Build & Unit Tests  | Compile with Maven and run unit tests    |
| ✨ Code Quality        | Apply formatting / static checks         |
| 🔧 Integration Tests   | Run integration tests (e.g. Testcontainers) |
| 📊 Code Coverage       | Generate JaCoCo or similar coverage report |
| 📦 Package / Docker    | Package application and/or build images  |

> ℹ️ Refer to the `Jenkinsfile` for the **current, exact** stage names and any additional stages (such as service startup, end-to-end tests, or deployment).

---

## Viewing Results

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

1. **Branch Specifier uses parameter instead of fixed branch**
   - Go to job → **Configure** → **Pipeline** section
   - Check **Branches to build** → **Branch Specifier**
   - Should be a fixed branch like `*/jenkins-setup` or `*/master`
   - Should NOT be `${BRANCH_NAME}` or `*/${BRANCH_NAME}`
   - Click **Save**

2. **Jenkinsfile doesn't exist in the specified branch**
   - Verify which branch contains the Jenkinsfile:
     ```bash
     git ls-tree -r --name-only origin/master | grep Jenkinsfile
     git ls-tree -r --name-only origin/jenkins-setup | grep Jenkinsfile
     ```
   - Update Branch Specifier to the correct branch
   - Or merge Jenkinsfile to your specified branch

3. **Wrong Script Path**
   - Script Path is case-sensitive
   - Default should be: `Jenkinsfile` (capital J)
   - Check if your file is named differently: `jenkinsfile`, `Jenkinsfile.groovy`, etc.
   - Update **Script Path** to match exact filename

4. **Git Parameter loses configuration after build**
   - Ensure Jenkinsfile has a `parameters` block (even if Git Parameter plugin overrides it)
   - This tells Jenkins to expect parameters and prevents them from being removed
   - See "Adding Build Parameters" section above

### ERROR: couldn't find remote ref refs/heads/origin/[branch]

**Problem**: Build fails with "fatal: couldn't find remote ref refs/heads/origin/jenkins-setup"

**Cause**: The Branch Specifier in Pipeline configuration is set to `${BRANCH_NAME}` or `*/${BRANCH_NAME}`, causing Jenkins to look for the wrong branch when loading the Jenkinsfile.

**Solution**:
1. Go to job → **Configure** → **Pipeline** section
2. Find **Branch Specifier** (under "Branches to build")
3. Change it to a **FIXED branch name** where your Jenkinsfile lives:
   - If Jenkinsfile is in `jenkins-setup`: Use `*/jenkins-setup`
   - If Jenkinsfile is in `master`: Use `*/master`
   - **Do NOT use `${BRANCH_NAME}` or `*/${BRANCH_NAME}` here**
4. Click **Save**

**Why This Happens:**
- **Branch Specifier** tells Jenkins where to FIND the Jenkinsfile to read the pipeline definition
- **BRANCH_NAME parameter** tells the pipeline which branch to TEST
- These are two different things!
- Jenkins needs a fixed location to find and read the Jenkinsfile before it can run and use parameters

**Correct Configuration:**
```
Pipeline Section:
- Branch Specifier: */jenkins-setup  ← Fixed branch containing Jenkinsfile

General Section (Git Parameter):
- Name: BRANCH_NAME  ← User selects which branch to test
- Parameter Type: Branch or Tag
- Default Value: master
```

**Workflow:**
1. Jenkins reads Jenkinsfile from fixed branch (Branch Specifier)
2. User selects branch to test from dropdown (BRANCH_NAME parameter)
3. Checkout stage uses BRANCH_NAME to checkout the selected branch
4. Tests run on the selected branch

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
