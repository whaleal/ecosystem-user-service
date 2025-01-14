node {
	def app
	def image = 'careydevelopment/ecosystem-user-service'
	def branch = scm.branches[0].name.substring(2)
	
	try {
	    stage('Cleanup') {
            sh 'sudo docker rm --force ecosystem-user-service || true'
            sh 'sudo docker rmi --force ecosystem-user-service || true'
        }
        
		stage('Clone repository') {
	    	git branch: branch,
	        	credentialsId: 'GitHub Credentials',
	        	url: 'https://github.com/careydevelopment/ecosystem-user-service.git'
	    } 
	
	    stage('Copy properties files') {
	       sh 'cp ../config/ecosystem-user-service/application.properties ./src/main/resources'
	       
	       //for recaptcha
	       sh 'cp ../config/ecosystem-user-service/carey-development-service-config.json .'   
        } 
	
		stage('Build JAR') {
	    	docker.image('maven:3.6.3-jdk-11').inside('-v /root/.m2:/root/.m2') {
	        	sh 'mvn -B clean package'
	        	stash includes: '**/target/ecosystem-user-service.jar', name: 'jar'
	    	}
	    }
	     
	    stage('Build Image') {
	    	unstash 'jar'
			app = docker.build image
	    }
	    
	    //stage('Push') {
	    //	docker.withRegistry('https://registry.hub.docker.com', 'docker-hub') {            
		//		app.push("${env.BUILD_NUMBER}")
		//		app.push("latest")
	    //    }    
	    //}
      
        stage ('Docker Run') {
            sh 'docker run -d -t -p 32010:32010 -v /etc/careydevelopment:/etc/careydevelopment --name ecosystem-user-service careydevelopment/ecosystem-user-service:latest'
        }
	} catch (e) {
		echo 'Error occurred during build process!'
		echo e.toString()
		currentBuild.result = 'FAILURE'
	} finally {
		//skipping tests as we need environment setup (e.g., remote properties files)
		//will get to it later
        //junit '**/target/surefire-reports/TEST-*.xml'		
	}
}