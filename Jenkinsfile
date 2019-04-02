#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'atlas_slack_coveralls_token', variable: 'coveralls_token')]) {
  buildGradlePlugin plaforms: ['osx','windows'], coverallsToken: coveralls_token
}
