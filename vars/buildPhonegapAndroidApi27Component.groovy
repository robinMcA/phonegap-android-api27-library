/*
 * Toolform-compatible Jenkins 2 Pipeline build step for building Android artefacts from Phonegap projects using the phonegap-android-api27 builder
 */

def call(Map config) {

  def artifactDir = "${config.project}-${config.component}-artifacts"
  def testOutput = "${config.project}-${config.component}-tests.xml"
  def phoneGapOutputDir = "platforms/android/app/build/outputs"

  final npm = { cmd ->
    ansiColor('xterm') {
      dir(config.baseDir) {
        sh "JEST_JUNIT_OUTPUT=${testOutput} npm ${cmd}"
      }
    }
  }

  container("phonegap-android-api27-builder") {

    stage('Build Details') {
      echo "Project:   ${config.project}"
      echo "Component: ${config.component}"
      echo "BuildNumber: ${config.buildNumber}"
    }

    stage('Install dependencies') {
      npm "install"
    }

    stage('Test') {
      npm 'run test -- --ci --testResultsProcessor="jest-junit"'
      junit allowEmptyResults: true, testResults: testOutput
    }

    stage('Build Debug') {
      npm "run phonegap -- --verbose build android"
    }

  }

  if(config.stage == 'dist') {

    container('phonegap-android-api27-builder') {
      stage('Build Release') {
        npm "run phonegap -- --verbose --release build android"
      }

      stage('Package') {
        sh "mkdir -p ${artifactDir}"
        sh "mv ${config.baseDir}/${phoneGapOutputDir}/apk/debug/* ${config.baseDir}/${phoneGapOutputDir}/apk/release/* ${artifactDir}"
      }
    }

    stage('Archive to Jenkins') {
      def tarName = "${config.project}-${config.component}-${config.buildNumber}.tar.gz"
      sh "tar -czvf \"${tarName}\" -C \"${artifactDir}\" ."
      archiveArtifacts tarName
    }

  }

}
