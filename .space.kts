/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("execute test") {
    container("maven:3.6-jdk-14") {
        resources {
            cpu = 1.cpu
            memory = 1024.mb
        }
        shellScript {
            content = """
                mvn test
            """
        }
    }
}

job("Build and push Docker") {
    container("maven:3.6.3-openjdk-15") {
        resources {
            cpu = 1.cpu
            memory = 1024.mb
        }
        shellScript {
            content = """
                mvn package
            """
        }
        kotlinScript { api -> 
			api.space().chats.channels.messages.sendTextMessage(
    			channelId = "3MVaBr2rDLSR",
    			text = "build image registry.centralhardware.synology.me/studyrussianbot:7.${api.executionNumber()} ")
    	}
    }

    docker {     
		build {
            context = "./"
            labels["build"] = "centralhardware.jetbrains.space"
            file = "dockerfile"
        }
        
        push("registry.centralhardware.synology.me/studyrussianbot") {
            tag = "7.\$JB_SPACE_EXECUTION_NUMBER"
        }
	}
}
