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
      val poll_resp = sttp.get(uri"http://${runtimeApi}/2018-06-01/runtime/invocation/next").send()
      println(s"${poll_resp.code} ${poll_resp.headers}")
      val requestId = poll_resp.headers("Lambda-Runtime-Aws-Request-Id")
      val eventData:String = poll_resp.unsafeBody

      val parsed = Parse.decodeOption[TestEvent](eventData)
      println(s"parsed: ${parsed}")

      val response = handle(parsed,poll_resp.headers.toMap)
      println(response)
      val respUrl = uri"http://${runtimeApi}/2018-06-01/runtime/invocation/${requestId}/response"
      println(s"resp Url: '${respUrl.toString}'")
      val result_resp = sttp.post(respUrl).body(response).send()
      println(s"GOT POST RESPONSE: ${result_resp.code}")
    }
  }

  def handle[T](eventData:T, headers:Map[String,String]):String = {
    val requestId = headers("Lambda-Runtime-Aws-Request-Id")
    s"GOT REQUEST ${requestId} with event data: \n${eventData}\n"
  }
}
