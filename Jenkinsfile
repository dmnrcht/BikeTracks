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
  }

  stage ('Instrumental tests') {
  }

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
