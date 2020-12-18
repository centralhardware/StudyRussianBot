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
