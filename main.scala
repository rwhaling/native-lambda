object main {
  def main(args:Array[String]) {
    println("Hello serverless world\n")
    val runtimeApi = System.getenv("AWS_LAMBDA_RUNTIME_API")
    println(s"Runtime API endpoint: $runtimeApi")
    while (true) {
      val (code, headers, eventData) = Curl.get(s"http://${runtimeApi}/2018-06-01/runtime/invocation/next")
      println(headers)
      val requestId = headers("Lambda-Runtime-Aws-Request-Id")
      val response = handle(eventData,headers)
      println(response)
      val respUrl = s"http://${runtimeApi}/2018-06-01/runtime/invocation/${requestId}/response"
      println(s"resp Url: '$respUrl'")
      val (postcode, postheaders, postresponse) = Curl.post(s"http://${runtimeApi}/2018-06-01/runtime/invocation/${requestId}/response", response)
      println(s"GOT POST RESPONSE: ${postresponse}")
    }
  }

  def handle(eventData:String, headers:Map[String,String]):String = {
    val requestId = headers("Lambda-Runtime-Aws-Request-Id")
    s"GOT REQUESTID ${requestId} with event data: ${eventData}"
  }
}
