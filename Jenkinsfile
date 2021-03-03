#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@add/optional_version') _

withCredentials([string(credentialsId: 'atlas_slack_coveralls_token', variable: 'coveralls_token')]) {
  buildGradlePlugin plaforms: ['osx','windows','linux'], coverallsToken: coveralls_token
}
