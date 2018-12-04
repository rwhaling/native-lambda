import com.softwaremill.sttp._
import argonaut._, Argonaut._

case class TestEvent(text:String)

object TestEvent {
  implicit def decode:CodecJson[TestEvent] = 
    casecodec1(TestEvent.apply, TestEvent.unapply)("text")
}

object main {
  def main(args:Array[String]) {
    println("Hello serverless world\n")

    implicit val backend: SttpBackend[Id, Nothing] = CurlBackend(verbose = true)

    val runtimeApi = System.getenv("AWS_LAMBDA_RUNTIME_API")
    println(s"Runtime API endpoint: $runtimeApi")
    while (true) {

      val (code, headers, eventData) = Curl.get(s"http://${runtimeApi}/2018-06-01/runtime/invocation/next")
      // need to find a newer libcurl before sttp will work
      // val resp = sttp.get(uri"https://postman-echo.com/get?foo1=bar1&foo2=bar2").send()

      println(headers)
      val requestId = headers("Lambda-Runtime-Aws-Request-Id")
      val parsed = Parse.decodeOption[TestEvent](eventData)
      println(s"parsed: ${parsed}")
      val response = handle(parsed,headers)
      println(response)
      val respUrl = s"http://${runtimeApi}/2018-06-01/runtime/invocation/${requestId}/response"
      println(s"resp Url: '$respUrl'")
      val (postcode, postheaders, postresponse) = Curl.post(s"http://${runtimeApi}/2018-06-01/runtime/invocation/${requestId}/response", response)
      println(s"GOT POST RESPONSE: ${postresponse}")
    }
  }

  def handle[T](eventData:T, headers:Map[String,String]):String = {
    val requestId = headers("Lambda-Runtime-Aws-Request-Id")
    s"GOT REQUESTID ${requestId} with event data: ${eventData}"
  }
}
