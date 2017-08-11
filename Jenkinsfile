def mvnHome = tool 'M3'

stage('build') {
  sh "${mvnHome}/bin/mvn -DMaven.test.failure.ignore clean package"
}
