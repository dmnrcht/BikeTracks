node {

  try {
    notifyBuild('STARTED')

    stage ('Checkout') {
        checkout scm
        sh 'chmod a+x ./gradlew'
    }

    stage ('Build') {
        sh './gradlew clean assemble'
    }

    stage ('Unit tests') {
        sh './gradlew app:test --info'
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'app/build/reports/tests/testReleaseUnitTest', reportFiles: 'index.html', reportName: 'Unit test report', reportTitles: ''])
    }

    //  stage ('Instrumental tests') {
    //    sh 'echo /Users/sebastien/Library/Android/sdk/tools/emulator -avd Nexus_5_API_26 -no-boot-anim > startemulator.command'
    //    sh 'chmod +x startemulator.command'
    //    sh 'open startemulator.command'
    //    sh 'serialno=$(/Users/sebastien/Library/Android/sdk/platform-tools/adb get-serialno)'
    //    sh './gradlew app:connectedAndroidTest'
    //    sh '/Users/sebastien/Library/Android/sdk/platform-tools/adb -s $serialno emu kill'
    //    sh 'rm startemulator.command'
    //    sh 'serialno='
    //  }

    stage ('Publish') {
        signAndroidApks (
          keyStoreId: "a4f0698c-9933-42be-b10e-40fba29161d7",
          apksToSign: "**/*-unsigned.apk",
          archiveSignedApks: true
        )
    //      androidApkUpload (
    //        googleCredentialsId: 'BikeTracks',
    //        apkFilesPattern: 'app/build/outputs/apk/release/*-release.apk',
    //        trackName: 'alpha'
    //     )
    }

  } catch (e) {
    // If there was an exception thrown, the build failed
    currentBuild.result = "FAILED"
    throw e
  } finally {
    // Success or failure, always send notifications
    notifyBuild(currentBuild.result)
  }
}

def notifyBuild(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  // Send notifications
  //  slackSend (color: colorCode, message: summary)
}
