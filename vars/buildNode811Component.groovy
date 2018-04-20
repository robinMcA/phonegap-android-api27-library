/*
 * Toolform-compatible Jenkins 2 Pipeline build step for NodeJS 8.11 apps using the node811 builder
 */

def call(Map config) {

  def artifactDir = "${config.project}-${config.component}-artifacts"
  def testOutput = "${config.project}-${config.component}-tests.xml"

  final yarn = { cmd ->
    ansiColor('xterm') {
      dir(config.baseDir) {
        sh "JEST_JUNIT_OUTPUT=${testOutput} yarn ${cmd}"
      }
    }
  }
  
  container("node811-builder") {

    stage('Build Details') {
      echo "Project:   ${config.project}"
      echo "Component: ${config.component}"
      echo "BuildNumber: ${config.buildNumber}"
    }

    stage('Install dependencies') {
      yarn "install"
    }

    stage('Test') {
      yarn 'test --ci --testResultsProcessor="jest-junit"'
      junit testOutput
    }

  }

  if(config.stage == 'dist') {

    container('node811-builder') {
      stage('Build') {
        yarn "build"
      }

      stage('Package') {
        sh "mkdir -p ${artifactDir}"

        yarn "install --production --ignore-scripts --prefer-offline"
        sh "mv node_modules dist package.json ${artifactDir}"

        // The static folder and application specific config files 
        // should also be staged if they exist.
        if(fileExists('static')) {
          sh "mv static ${artifactDir}"
        }

        if(fileExists('next.config.js')) {
          sh "mv next.config.json ${artifactDir}"
        }
      }
    }

    stage('Archive to Jenkins') {
      def tarName = "${config.project}-${config.component}-${config.buildNumber}.tar.gz"
      sh "tar -czvf \"${tarName}\" -C \"${artifactDir}\" ."
      archiveArtifacts tarName
    }

  }

}
