/*
 * Toolform-compatible Jenkins 2 Pipeline build step for docker based builders
 * Expects some or all of the following scripts in /home/builder/scripts/<builder>:
 *  - prepare.sh
 *  - test.sh
 *  - build.sh
 *  - bundle.sh
 * 
 * Output artifacts should be in $ARTIFACT_DIR
 * Test results should be in $TEST_RESULT_DIR
 */

def call(Map config) {

  def artifactDir = "${config.project}-${config.component}-artifacts"
  def testResultDir = "${config.project}-${config.component}-tests"

  final yarn = { cmd ->
    ansiColor('xterm') {
      dir(config.baseDir) {
        sh "JEST_JUNIT_OUTPUT=${testResultDir} yarn ${cmd}"
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
      sh "mkdir -p ${testResultDir}"
      yarn 'test --ci --testResultsProcessor="jest-junit"'
      junit "${testResultDir}/*.xml"
    }

  }

  if(config.stage == 'dist') {

    container('node012-builder') {
      stage('Build') {
        yarn "build"
      }

      stage('Package') {
        sh "mkdir -p ${artifactDir}"

        yarn "install --production --ignore-scripts --prefer-offline"
        sh "mv node_modules dist package.json config.js ${artifactdir}"
      }
    }

    stage('Archive to Jenkins') {
      def tarName = "${config.project}-${config.component}-${config.buildNumber}.tar.gz"
      sh "tar -czvf \"${tarName}\" -C \"${artifactDir}\" ."
      archiveArtifacts tarName
    }

  }

}
