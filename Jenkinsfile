node {
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
    androidApkUpload (
      googleCredentialsId: 'BikeTracks',
      apkFilesPattern: 'app/build/outputs/apk/release/*-release.apk',
      trackName: 'alpha'
    )
  }
}