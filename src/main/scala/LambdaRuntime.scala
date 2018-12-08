import com.softwaremill.sttp._
import argonaut._, Argonaut._

object LambdaRuntime {
  def serve[I,O](f:Function2[I,Map[String,String],O])(implicit i:CodecJson[I], o:CodecJson[O]):Unit = {
    implicit val backend: SttpBackend[Id, Nothing] = CurlBackend(verbose = true)

    val runtimeApi = System.getenv("AWS_LAMBDA_RUNTIME_API")
    println(s"Runtime API endpoint: $runtimeApi")
    while (true) {
      val poll_resp = sttp.get(uri"http://${runtimeApi}/2018-06-01/runtime/invocation/next").send()
      println(s"${poll_resp.code} ${poll_resp.headers}")
      val requestId = poll_resp.headers("Lambda-Runtime-Aws-Request-Id")
      val eventData:String = poll_resp.unsafeBody

      val parsed = Parse.decodeOption[I](eventData)
      println(s"parsed: ${parsed}")

      val response = f(parsed.get,poll_resp.headers.toMap)
      println(response)
      val respUrl = uri"http://${runtimeApi}/2018-06-01/runtime/invocation/${requestId}/response"
      println(s"resp Url: '${respUrl.toString}'")
      val rendered = response.asJson.spaces2 + "\n"
      val result_resp = sttp.post(respUrl).body(rendered).send()
      println(s"GOT POST RESPONSE: ${result_resp.code}")
    }
  }
}