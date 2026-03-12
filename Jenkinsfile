// Jenkinsfile for Quote REST API
// Declarative Pipeline for CI/CD

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
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v maven-repo:/root/.m2 -u root'
            // Note: Docker CLI must be installed in the Maven container for docker compose commands
            // Network connectivity to services (Keycloak, PostgreSQL) handled via host networking
        }
    }

    environment {
        // Maven options (MaxPermSize removed - not needed in Java 17+)
        MAVEN_OPTS = '-Xmx1024m'
        MAVEN_CLI_OPTS = '--batch-mode --errors --fail-at-end --show-version'
        
        // Docker image settings
        DOCKER_IMAGE = 'quote-rest-api'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        
        // Test configuration
        TEST_REPORT_DIR = 'target/surefire-reports'
        INTEGRATION_TEST_DIR = 'target/failsafe-reports'
        JACOCO_REPORT_DIR = 'target/site/jacoco'
    }

    options {
        // Keep last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timeout after 30 minutes
        timeout(time: 30, unit: 'MINUTES')
        // Timestamps in console output
        timestamps()
        // Disable concurrent builds
        disableConcurrentBuilds()
    }

    stages {
        stage('Setup') {
            steps {
                echo '⚙️ Setting up environment...'
                sh '''
                    # Install Docker CLI if not present
                    if ! command -v docker &> /dev/null; then
                        echo "Installing Docker CLI..."
                        apt-get update -qq
                        apt-get install -y -qq ca-certificates curl
                        install -m 0755 -d /etc/apt/keyrings
                        curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
                        chmod a+r /etc/apt/keyrings/docker.asc
                        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian bookworm stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
                        apt-get update -qq
                        apt-get install -y -qq docker-ce-cli docker-compose-plugin
                        echo "Docker CLI installed successfully"
                    else
                        echo "Docker CLI already installed"
                    fi
                '''
            }
        }
        
        stage('Checkout') {
            steps {
                echo "📦 Checking out branch: ${params.BRANCH_NAME}"
                script {
                    // Git Parameter returns format like "origin/jenkins-setup"
                    // Extract just the branch name part after the last /
                    def branchName = params.BRANCH_NAME.contains('/') ? 
                        params.BRANCH_NAME.substring(params.BRANCH_NAME.lastIndexOf('/') + 1) : 
                        params.BRANCH_NAME
                    
                    echo "Using branch name: ${branchName}"
                    
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${branchName}"]],
                        userRemoteConfigs: scm.userRemoteConfigs,
                        extensions: [[$class: 'CloneOption', depth: 0, noTags: false, shallow: false]]
                    ])
                }
                sh 'git log -1 --oneline'
                sh "echo 'Successfully checked out: ${params.BRANCH_NAME}'"
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building application...'
                sh """
                    mvn ${MAVEN_CLI_OPTS} clean compile \
                        -DskipTests
                """
            }
        }

        stage('Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                sh """
                    mvn ${MAVEN_CLI_OPTS} test \
                        -Dtest=!*IntegrationTest
                """
            }
            post {
                always {
                    // Publish JUnit test results
                    junit testResults: "${TEST_REPORT_DIR}/**/*.xml", allowEmptyResults: true
                }
            }
        }

        stage('Code Quality - Spotless') {
            steps {
                echo '✨ Checking code formatting with Spotless...'
                sh """
                    mvn ${MAVEN_CLI_OPTS} spotless:check
                """
            }
        }

        stage('Integration Tests') {
            steps {
                echo '🔧 Running integration tests with Testcontainers...'
                sh """
                    mvn ${MAVEN_CLI_OPTS} verify \
                        -DskipUnitTests=true \
                        -Dtest=*IntegrationTest \
                        -Dsurefire.failIfNoSpecifiedTests=false
                """
            }
            post {
                always {
                    // Publish integration test results
                    junit testResults: "${TEST_REPORT_DIR}/**/*.xml", allowEmptyResults: true
                }
            }
        }

        stage('Code Coverage') {
            steps {
                echo '📊 Generating code coverage report...'
                sh """
                    mvn ${MAVEN_CLI_OPTS} jacoco:report
                """
            }
            post {
                always {
                    // Publish JaCoCo coverage report via Code Coverage API
                    recordCoverage(
                        tools: [
                            jacoco(
                                pattern: 'target/site/jacoco/jacoco.xml',
                                skipNoReports: false
                            )
                        ]
                    )
                }
            }
        }

        stage('Package') {
            steps {
                echo '📦 Packaging application...'
                sh """
                    mvn ${MAVEN_CLI_OPTS} package \
                        -DskipTests
                """
            }
            post {
                success {
                    // Archive the JAR file
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
            post {
                success {
                    echo "✅ Docker image built: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
    }

    post {
        always {
            script {
                echo '🧹 Cleaning up...'
                // Stop all Docker Compose services
                sh 'docker compose down || true'
            }

            // Clean up build artifacts (optional, no extra plugins required)
            script {
                // Remove the Maven target directory using built-in steps
                dir('target') {
                    deleteDir()
                }
            }
        }
        success {
            echo '✅ Pipeline completed successfully!'
            // Can add notifications here (email, Slack, etc.)
        }
        failure {
            echo '❌ Pipeline failed!'
            // Can add failure notifications here
        }
        unstable {
            echo '⚠️ Pipeline is unstable (tests failed but build succeeded)'
        }
    }
}
