  @Library('common-shared') _

  pipeline {
    agent {
      kubernetes {
        label 'buildpack-agent-' + env.JOB_NAME.replaceAll("/", "-")
        yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: buildpack
            image: buildpack-deps:stable
            command:
            - cat
            tty: true
            resources:
              limits:
                memory: "2Gi"
                cpu: "1"
              requests:
                memory: "2Gi"
                cpu: "1"
            env:
            - name: LICENSE_KEY_FILE
              value: /run/secrets/maxmind/license_key
            volumeMounts:
            - name: tmp
              mountPath: /tmp
            - name: maxmind-license-key
              mountPath: /run/secrets/maxmind/
              readOnly: true
          - name: jnlp
            resources:
              limits:
                memory: "2Gi"
                cpu: "1"
              requests:
                memory: "2Gi"
                cpu: "1"
            volumeMounts:
            - name: mvnw
              mountPath: /home/jenkins/.m2/wrapper
              readOnly: false
            - name: m2-repo
              mountPath: /home/jenkins/.m2/repository
            - name: settings-xml
              mountPath: /home/jenkins/.m2/settings.xml
              subPath: settings.xml
              readOnly: true
            - name: tmp
              mountPath: /tmp
          volumes:
          - name: mvnw
            emptyDir: {}
          - name: m2-repo
            emptyDir: {}
          - name: tmp
            emptyDir: {}
          - name: settings-xml
            secret:
              secretName: m2-secret-dir
              items:
              - key: settings.xml
                path: settings.xml
          - name: maxmind-license-key
            secret:
              secretName: maxmind-license-key
        '''
      }
    }

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
          container('buildpack') {
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
            label 'kubedeploy-agent-' + env.JOB_NAME.replaceAll("/", "-")
            yaml '''
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: kubectl
                image: eclipsefdn/kubectl:1.9-alpine
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
            withKubeConfig([credentialsId: '1d8095ea-7e9d-4e94-b799-6dadddfdd18a', serverUrl: 'https://console-int.c1-ci.eclipse.org']) {
              sh '''
                DEPLOYMENT="$(k8s getFirst deployment "${NAMESPACE}" "app=${APP_NAME},environment=${ENVIRONMENT}")"
                if [[ $(echo "${DEPLOYMENT}" | jq -r 'length') -eq 0 ]]; then
                  echo "ERROR: Unable to find a deployment to patch matching 'app=${APP_NAME},environment=${ENVIRONMENT}' in namespace ${NAMESPACE}"
                  exit 1
                else 
                  DEPLOYMENT_NAME="$(echo "${DEPLOYMENT}" | jq -r '.metadata.name')"
                  kubectl set image "deployment.v1.apps/${DEPLOYMENT_NAME}" -n "${NAMESPACE}" "${CONTAINER_NAME}=${IMAGE_NAME}:${TAG_NAME}" --record=true
                  if ! kubectl rollout status "deployment.v1.apps/${DEPLOYMENT_NAME}" -n "${NAMESPACE}"; then
                    # will fail if rollout does not succeed in less than .spec.progressDeadlineSeconds
                    kubectl rollout undo "deployment.v1.apps/${DEPLOYMENT_NAME}" -n "${NAMESPACE}"
                    exit 1
                  fi
                fi
              '''
            }
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
