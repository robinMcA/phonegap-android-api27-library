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
  def scriptDir = "/home/builder/scripts/${config.builder}"

  final runScript = { cmd ->
    ansiColor('xterm') {
      dir(config.baseDir) {
        sh "ARTIFACT_DIR=${artifactDir} TEST_RESULT_DIR=${testResultDir} ${scriptDir}/${cmd}.sh"
      }
    }
  }
  
  container("${config.builder}-builder") {

    stage('Build Details') {
      echo "Project:   ${config.project}"
      echo "Component: ${config.component}"
      echo "BuildNumber: ${config.buildNumber}"
    }

    stage('Prepare') {
      runScript "prepare"
    }

    stage('Test') {
      runScript "test"
      junit "${testDir}/*.xml"
    }

  }

  if(config.stage == 'dist') {

    container('node012-builder') {
      stage('Build') {
        runScript "build"
      }

      stage('Package') {
        runScript "package"
      }
    }

    stage('Archive to Jenkins') {
      def tarName = "${config.project}-${config.component}-${config.buildNumber}.tar.gz"
      sh "tar -czvf \"${tarName}\" -C \"${artifactDir}\" ."
      archiveArtifacts tarName
    }

  }

}
