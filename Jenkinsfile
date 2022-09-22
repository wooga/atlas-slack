#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([
                string(credentialsId: 'atlas_plugins_sonar_token', variable: 'sonar_token'),
                string(credentialsId: 'snyk-wooga-frontend-integration-token', variable: 'SNYK_TOKEN')]) {
  buildGradlePlugin platforms: ['osx','windows','linux'], sonarToken: sonar_token
}
