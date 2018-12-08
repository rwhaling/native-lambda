import argonaut._, Argonaut._

object Main {
  def main(args:Array[String]) {
    LambdaRuntime.serve[TestEvent,TestResult] { (event, headers) =>
      val requestId = headers("Lambda-Runtime-Aws-Request-Id")
      TestResult(s"GOT REQUEST ${requestId} with event data: ${event}")
    }
  }
}

case class TestEvent(text:String)
case class TestResult(status:String)

object TestEvent {
  implicit def codec:CodecJson[TestEvent] = 
    casecodec1(TestEvent.apply, TestEvent.unapply)("text")
}

object TestResult {
  implicit def codec:CodecJson[TestResult] = 
    casecodec1(TestResult.apply, TestResult.unapply)("status")

}

