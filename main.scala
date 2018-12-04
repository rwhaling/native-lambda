object main {
    def main(args:Array[String]) {
        println("Hello serverless world\n")
        val runtimeApi = System.getenv("AWS_LAMBDA_RUNTIME_API")
        println(s"Runtime API endpoint: $runtimeApi")
        val (code, headers, eventData) = Curl.get(s"http://${runtimeApi}/2018-06-01/runtime/invocation/next")
        // val (code, headers, eventData) = Curl.get("https://postman-echo.com/get?foo1=bar1&foo2=bar2")
        println(headers)
        println(eventData)
        val requestId = headers("Lambda-Runtime-Aws-Request-Id")

        val response = s"got event data: ${eventData}"
        val (postcode, postheaders, postresponse) = Curl.post(s"http://${runtimeApi}/2018-06-01/runtime/invocation/${requestId}/response", response)
        // val (postcode, postheaders, postresponse) = Curl.post("https://postman-echo.com/post", "foo\nbar\bbaz...")
        println(postheaders)
        println(postresponse)
        // for (arg <- args) {
        //     println(s"argument received: $arg\n")
        // }
    }
}
