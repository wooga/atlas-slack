#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'atlas_slack_coveralls_token', variable: 'coveralls_token'),
                string(credentialsId: 'atlas_plugins_sonar_token', variable: 'sonar_token'),
                string(credentialsId: 'atlas_plugins_snyk_token', variable: 'SNYK_TOKEN')]) {
  buildGradlePlugin platforms: ['osx','windows','linux'], coverallsToken: coveralls_token, sonarToken: sonar_token
}
