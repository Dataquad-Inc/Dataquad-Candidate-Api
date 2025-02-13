pipeline {
    environment {
    registryCredential = 'docker-hub'
    DOCKER_IMAGE_NAME = 'candidates'
    registry = "sandycis476/candidate"
    dockerImage = ''
  }
agent any
   stages {
    stage('Cloning Git') {
      steps {
        git([url: 'https://github.com/NaveenKumar-dataquad/Dstaquad-Candidate.git', branch: 'master', credentialsId: 'Naveen-DataQuad'])
      }
    }
    stage('Building image') {
      steps{
        script {
          dockerImage = docker.build registry + ":$BUILD_NUMBER"
        }
      }
    }
    stage('Deploy Image') {
      steps{
        script {
          docker.withRegistry( '', registryCredential ) {
            dockerImage.push()
          }
        }
      }
    }
  }
}
