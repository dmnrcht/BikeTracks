node {
  stage ('Checkout') {
    checkout scm
  }

  stage ('Build') {
    sh 'chmod a+x ./gradlew'
    sh './gradlew clean assembleRelease'
  }

  stage ('Unit tests') {
    sh './gradlew app:test --info'
  }

  stage ('Instrumental tests') {
    sh 'echo /Users/sebastien/Library/Android/sdk/tools/emulator -avd Nexus_5_API_26 -no-boot-anim > startemulator.command'
    sh 'chmod +x startemulator.command'
    sh 'open startemulator.command'
    sh 'serialno=$(/Users/sebastien/Library/Android/sdk/platform-tools/adb get-serialno)'
    sh './gradlew app:connectedAndroidTest'
    sh '/Users/sebastien/Library/Android/sdk/platform-tools/adb -s $serialno emu kill'
    sh 'rm startemulator.command'
    sh 'serialno='
  }

  stage ('Publish') {
    signAndroidApks (
      keyStoreId: "a4f0698c-9933-42be-b10e-40fba29161d7",
      apksToSign: "**/*-unsigned.apk",
      archiveSignedApks: true
    )
    androidApkUpload googleCredentialsId: 'BikeTracks', apkFilesPattern: '**/*.apk', trackName: 'alpha'
  }
}
