/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("execute test") {
    container("ubuntu") {
        shellScript {
            content = """
                mvn test
            """
        }
    }
}
