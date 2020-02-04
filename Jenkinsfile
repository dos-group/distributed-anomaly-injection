pipeline {
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    agent {
        docker {
            image 'teambitflow/python-docker:3.7-stretch'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    environment {
        registry = 'teambitflow/anomaly-injector-agent'
        registryController = 'teambitflow/anomaly-experiment-controller'
        registryCredential = 'dockerhub'
        dockerImage = '' // Variable must be declared here to allow passing an object between the stages.
        dockerImageARM32 = ''
        dockerImageController = ''
        dockerImageControllerARM32 = ''
    }
    stages {
        // no tests as of right now
        // stage('Test') {
        //    steps {
        //        dir('core') {
        //            sh 'pip install pytest pytest-cov'
        //            sh 'make init'
        //            sh 'make jenkins-test'
        //        }
        //    }
        //    post {
        //        always {
        //            junit 'core/tests/test-report.xml'
        //            archiveArtifacts 'core/tests/*-report.xml'
        //        }
        //    }
        //}
        stage('Git') {
            steps {
                dir('core') {
                    script {
                        env.GIT_COMMITTER_EMAIL = sh(
                            script: "git --no-pager show -s --format='%ae'",
                            returnStdout: true
                            ).trim()
                    }
                }
            }
        }
        stage('Build') {
            agent {
                docker {
                    image 'teambitflow/maven-docker:3.6-jdk-11'
                    args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                sh 'mvn clean test-compile -DskipTests=true -Dmaven.javadoc.skip=true -B -V'
            }
        }
        //stage('Test') {
        //    agent {
        //        docker {
        //            image 'teambitflow/maven-docker:3.6-jdk-11'
        //            args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
        //        }
        //    }
        //    steps {
        //        sh 'mvn test -B -V'
        //    }
        //    post {
        //        always {
        //            junit 'target/surefire-reports/TEST-*.xml'
        //            jacoco classPattern: 'target/classes,target/test-classes', execPattern: 'target/coverage-reports/*.exec', inclusionPattern: '**/*.class', sourcePattern: 'src/main/java,src/test/java'
        //            archiveArtifacts 'target/surefire-reports/TEST-*.xml'
        //            archiveArtifacts 'target/coverage-reports/*.exec'
        //        }
        //    }
        //}
        stage('Package') {
            agent {
                docker {
                    image 'teambitflow/maven-docker:3.6-jdk-11'
                    args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                sh 'mvn package -DskipTests=true -Dmaven.javadoc.skip=true -B -V'
            }
            post {
                success {
                    archiveArtifacts 'anomaly-experiment-controller/target/*.jar'
                }
            }
        }
        stage('SonarQube') {
            parallel {
                stage('anomaly-injector-agent') {
                    steps {
                        dir('anomaly-injector-agent') {
                            script {
                                // sonar-scanner which don't rely on JVM
                                def scannerHome = tool 'sonar-scanner-linux'
                                withSonarQubeEnv('CIT SonarQube') {
                                    sh """
                                        ${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=distributed-anomaly-injection -Dsonar.branch.name=$BRANCH_NAME \
                                            -Dsonar.sources=. \
                                            -Dsonar.inclusions="**/*.py" -Dsonar.exclusions="anomalies/c_src/*" \
                                            -Dsonar.test.reportPath=tests/test-report.xml
                                    """
                                }
                            }
                            timeout(time: 30, unit: 'MINUTES') {
                                waitForQualityGate abortPipeline: true
                            }
                        }
                    }
                }
                stage('anomaly-experiment-controller') {
                    agent {
                        docker {
                            image 'teambitflow/maven-docker:3.6-jdk-11'
                            args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
                        }
                    }
                    steps {
                        dir('anomaly-experiment-controller'){
                            withSonarQubeEnv('CIT SonarQube') {
                                // The find & paste command in the jacoco line lists the relevant files and prints them, separted by comma
                                // The jacoco reports must be given file-wise, while the junit reports are read from the entire directory
                                sh '''
                                    mvn sonar:sonar -B -V -Dsonar.projectKey=anomaly-experiment-controller -Dsonar.branch.name=$BRANCH_NAME \
                                        -Dsonar.sources=src/main/java \
                                        -Dsonar.inclusions="**/*.java" \
                                '''
                            }  
                            timeout(time: 10, unit: 'MINUTES') {
                                waitForQualityGate abortPipeline: true
                            }
                        }
                    }
                }
            }
        }
        stage('Maven Install') {
            agent {
                docker {
                    image 'teambitflow/maven-docker:3.6-jdk-11'
                    args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            when {
                branch 'master'
            }
            steps {
                // Only install the jar, that has been built in the previous stages. Do not re-compile or re-test.
                sh 'mvn jar:jar install:install -B -V'
            }
        }
        stage('Docker build') {
            agent {
                docker {
                    image 'teambitflow/maven-docker:3.6-jdk-11'
                    args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                dir('anomaly-injector-agent') {
                    script {
                        dockerImage = docker.build registry + ':$BRANCH_NAME-build-$BUILD_NUMBER', '-f slim.Dockerfile .'
                        dockerImageARM32 = docker.build registry + ':$BRANCH_NAME-build-$BUILD_NUMBER-arm32v7', '-f arm32v7.Dockerfile .'
                    }
                }
                dir('anomaly-experiment-controller') {
                    script {
                        dockerImageController = docker.build registryController + ':$BRANCH_NAME-build-$BUILD_NUMBER', '-f Dockerfile .'
                        dockerImageControllerARM32 = docker.build registryController + ':$BRANCH_NAME-build-$BUILD_NUMBER-arm32v7', '-f arm32v7.Dockerfile .'
                    }
                }
            }
        }

        stage('Docker push') {
            when {
                branch 'master'
            }
            steps {
                dir('anomaly-injector-agent') {
                    script {
                        docker.withRegistry('', registryCredential) {
                            dockerImage.push("build-$BUILD_NUMBER")
                            dockerImage.push("latest-amd64")
                            dockerImageARM32.push("build-$BUILD_NUMBER-arm32v7")
                            dockerImageARM32.push("latest-arm32v7")
                        }
                    }
                    withCredentials([
                      [
                        $class: 'UsernamePasswordMultiBinding',
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKERUSER',
                        passwordVariable: 'DOCKERPASS'
                      ]
                    ]) {
                        // Dockerhub Login
                        sh '''#! /bin/bash
                        echo $DOCKERPASS | docker login -u $DOCKERUSER --password-stdin
                        '''
                        // teambitflow/python-bitflow:latest manifest
                        sh "docker manifest create ${registry}:latest ${registry}:latest-amd64 ${registry}:latest-arm32v7"
                        sh "docker manifest annotate ${registry}:latest ${registry}:latest-arm32v7 --os linux --arch arm"
                        sh "docker manifest push --purge ${registry}:latest"
                    }  
                }
                dir('anomaly-experiment-controller') {
                    script {
                        docker.withRegistry('', registryCredential) {
                            dockerImageController.push("build-$BUILD_NUMBER")
                            dockerImageController.push("latest-amd64")
                            dockerImageControllerARM32.push("build-$BUILD_NUMBER-arm32v7")
                            dockerImageControllerARM32.push("latest-arm32v7")
                        }
                    }
                    withCredentials([
                      [
                        $class: 'UsernamePasswordMultiBinding',
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKERUSER',
                        passwordVariable: 'DOCKERPASS'
                      ]
                    ]) {
                        // Dockerhub Login
                        sh '''#! /bin/bash
                        echo $DOCKERPASS | docker login -u $DOCKERUSER --password-stdin
                        '''
                        // teambitflow/python-bitflow:latest manifest
                        sh "docker manifest create ${registryController}:latest ${registryController}:latest-amd64 ${registryController}:latest-arm32v7"
                        sh "docker manifest annotate ${registryController}:latest ${registryController}:latest-arm32v7 --os linux --arch arm"
                        sh "docker manifest push --purge ${registryController}:latest"
                    }  
                }
            }
        }
    }
    post {
        success {
            withSonarQubeEnv('CIT SonarQube') {
                slackSend channel: '#jenkins-builds-all', color: 'good',
                    message: "Build ${env.JOB_NAME} ${env.BUILD_NUMBER} was successful (<${env.BUILD_URL}|Open Jenkins>) (<${env.SONAR_HOST_URL}|Open SonarQube>)"
            }
        }
        failure {
            slackSend channel: '#jenkins-builds-all', color: 'danger',
                message: "Build ${env.JOB_NAME} ${env.BUILD_NUMBER} failed (<${env.BUILD_URL}|Open Jenkins>)"
        }
        fixed {
            withSonarQubeEnv('CIT SonarQube') {
                slackSend channel: '#jenkins-builds', color: 'good',
                    message: "Thanks to ${env.GIT_COMMITTER_EMAIL}, build ${env.JOB_NAME} ${env.BUILD_NUMBER} was successful (<${env.BUILD_URL}|Open Jenkins>) (<${env.SONAR_HOST_URL}|Open SonarQube>)"
            }
        }
        regression {
            slackSend channel: '#jenkins-builds', color: 'danger',
                message: "What have you done ${env.GIT_COMMITTER_EMAIL}? Build ${env.JOB_NAME} ${env.BUILD_NUMBER} failed (<${env.BUILD_URL}|Open Jenkins>)"
        }
    }
}
