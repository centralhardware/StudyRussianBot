/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("execute test") {
    container("maven:3.6-jdk-14") {
        shellScript {
            content = """
                mvn test
            """
        }
    }
}
