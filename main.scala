object main {
    def main(args:Array[String]) {
        println("Hello serverless world\n")
        println(s"Event data received: ${System.getenv("EVENT_DATA")}")
        // for (arg <- args) {
        //     println(s"argument received: $arg\n")
        // }
    }
}