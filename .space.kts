/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/


job("Build and push Docker") {
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
    }

    docker {     
		build {
            context = "./"
            labels["build"] = "centralhardware.jetbrains.space"
            file = "dockerfile"
        }
        
        push("registry.centralhardware.synology.me/studyrussianbot") {
            tag = "\$JB_SPACE_EXECUTION_NUMBER"
        }
	}

    container("openjdk:11") {
        resources {
            cpu = 372.mcpu
            memory = 256.mb
        }
        kotlinScript { api ->
            api.space().chats.channels.messages.sendTextMessage(
                channelId = "3MVaBr2rDLSR",
                text = "build image registry.centralhardware.synology.me/studyrussianbot:${api.executionNumber()} ")
        }
    }
}
