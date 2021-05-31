  @Library('common-shared') _

  pipeline {

    environment {
      APP_NAME = 'geoip-rest-api'
      NAMESPACE = 'foundation-internal-webdev-apps'
      IMAGE_NAME = 'eclipsefdn/geoip-rest-api'
      CONTAINER_NAME = 'app'
      ENVIRONMENT = sh(
        script: """
          if [ "${env.BRANCH_NAME}" = "master" ]; then
            printf "production"
          else
            printf "${env.BRANCH_NAME}"
          fi
        """,
        returnStdout: true
      )
      TAG_NAME = sh(
        script: """
          GIT_COMMIT_SHORT=\$(git rev-parse --short ${env.GIT_COMMIT})
          if [ "${env.ENVIRONMENT}" = "" ]; then
            printf \${GIT_COMMIT_SHORT}-${env.BUILD_NUMBER}
          else
            printf ${env.ENVIRONMENT}-\${GIT_COMMIT_SHORT}-${env.BUILD_NUMBER}
          fi
        """,
        returnStdout: true
      )
    }

    options {
      buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers { 
      // build once a week to keep up with parents images updates
      cron('H H * * H') 
    }

    stages {
      stage('Build Java code') {
        steps {
          readTrusted './bin/maxmind.sh'
          readTrusted 'mvnw'
          readTrusted '.mvn/wrapper/MavenWrapperDownloader.java'
          readTrusted 'pom.xml'

          withCredentials([string(credentialsId: 'maxmind-license-key', variable: 'MAXMIND_LICENSE_KEY')]) {
            sh 'mkdir -p /tmp/maxmind && ./bin/maxmind.sh /tmp/maxmind'
          }

          sh './mvnw -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn --batch-mode package'
          stash includes: 'target/', name: 'target'
          dir('/tmp') {
            stash includes: 'maxmind/', name: 'maxmind'
          }
        }
      }

      stage('Build docker image') {
        agent {
          label 'docker-build'
        }
        steps {
          readTrusted 'src/main/docker/Dockerfile.jvm'

          unstash 'target'
          unstash 'maxmind'

          sh 'docker build -f src/main/docker/Dockerfile.jvm --no-cache -t ${IMAGE_NAME}:${TAG_NAME} -t ${IMAGE_NAME}:latest .'
        }
      }

      stage('Push docker image') {
        agent {
          label 'docker-build'
        }
        when {
          anyOf {
            environment name: 'ENVIRONMENT', value: 'production'
            environment name: 'ENVIRONMENT', value: 'staging'
          }
        }
        steps {
          withDockerRegistry([credentialsId: '04264967-fea0-40c2-bf60-09af5aeba60f', url: 'https://index.docker.io/v1/']) {
            sh '''
              docker push ${IMAGE_NAME}:${TAG_NAME}
              docker push ${IMAGE_NAME}:latest
            '''
          }
        }
      }

      stage('Deploy to cluster') {
        agent {
          kubernetes {
            label 'kubedeploy-agent'
            yaml '''
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: kubectl
                image: eclipsefdn/kubectl:okd-c1
                command:
                - cat
                tty: true
            '''
          }
        }

        when {
          anyOf {
            environment name: 'ENVIRONMENT', value: 'production'
            environment name: 'ENVIRONMENT', value: 'staging'
          }
        }
        steps {
          container('kubectl') {
            updateContainerImage([
              namespace: "${env.NAMESPACE}",
              selector: "app=${env.APP_NAME},environment=${env.ENVIRONMENT}",
              containerName: "${env.CONTAINER_NAME}",
              newImageRef: "${env.IMAGE_NAME}:${env.TAG_NAME}"
            ])
          }
        }
      }
    }

    post {
      always {
        deleteDir() /* clean up workspace */
        sendNotifications currentBuild
      }
    }
  }
