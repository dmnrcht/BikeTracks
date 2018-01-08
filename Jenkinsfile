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
