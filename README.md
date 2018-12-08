# native-lambda: AWS Lambda Custom Runtime for Scala Native

A running, proof-of-concept port of the [Custom Runtime Tutorial](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-walkthrough.html) to Scala Native, with a few bonus goodies.  The code is about 40 lines of Scala; I implemented the API interactions with Paweł Cejrowski's excellent [Curl backend for STTP](https://github.com/softwaremill/sttp/tree/master/core/native/src/main/scala/com/softwaremill/sttp).  

The tricky part is the build: custom Lambda runtimes execute on a somewhat cranky [Amazon Linux machine image](https://docs.aws.amazon.com/lambda/latest/dg/current-supported-versions.html), with outdated versions of several critical libraries, including critical ones like LLVM, curl, and openssl.
To construct a working Scala Native image, I use Docker to build custom versions of the required libraries, publish those as a [Lambda Layer](https://docs.aws.amazon.com/lambda/latest/dg/configuration-layers.html), and then do a bit of linking trickery to ensure that the function binary will find the packaged libaries once it's installed on the Lambda server.  

There are two Dockerfiles: [one for the base image](native-lambda-base/Dockerfile), which I have published at `rwhaling/native-lambda-base`, but can be rebuilt locally if you prefer, and [one for compiling and linking your code](Dockerfile), which is much simpler. 

Extras: I've included Argonaut in the libary for JSON serialization, and  openssl for future applications (see below).

Caveat: I'm not responsible for any AWS costs you may invoke by using this software - this is all intended to fit well within the free tier but please review AWS' pricing policies before attempting to deploy this software.

## API Example

```scala
object Main {
  def main(args:Array[String]) {
    LambdaRuntime.serve[TestEvent,TestResult] { (event, headers) =>
      val requestId = headers("Lambda-Runtime-Aws-Request-Id")
      TestResult(s"GOT REQUEST ${requestId} with event data: ${event}")
    }
  }
}
```

## How to use it

What you need:

- Docker
- An AWS account
- The ARN for an AMI role created as described [here](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-walkthrough.html#runtimes-walkthrough-prereqs)
- AWS credentials (access key/secret key) with full permission to create/list/delete/invoke AWS Lambda functions and layers 
- A name for your function (can be anything you want, I use )

First: export the environment variables with your credentials:

```
~/../native-lambda $ export AWS_ACCESS_KEY_ID=aaaaaaa
~/../native-lambda $ export AWS_SECRET_ACCESS_KEY=bbbbbbb
~/../native-lambda $ export AWS_DEFAULT_REGION=us-west-1 
~/../native-lambda $ export LAMBDA_ROLE_ARN=arn:aws:iam::1234567890:role/lambda-test
```

Second: build and tag the Docker image - it will install all dependencies, then compile and link the scala code as well

```
~/native-lambda $ ./build.sh
```

Third: upload the code and the runtime to AWS lambda: 

```
~/native-lambda $ ./deploy.sh
```

Fourth: invoke your lambda:

```
~/native-lambda $ ./invoke.sh '{"text":"some event text"}'
```

Fourth: Check out the logs and metrics on the Lambda dashboard to see how it runs and what the performance is like.  I haven't done full load-testing yet,but from what I've seen cold starts are under 200ms, and a warmed executor serves most requests below the 100ms minimum billing increment. 

This is an enormous improvement over the JVM, which takes over 4 seconds to cold start with the default 128MB memory size.  It's great to see Scala Native's small footprint and snappy startup making an impact here!

## TODO's, caveats, and future work

1. Handle errors better - there are [two more endpoints](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html#runtimes-api-invokeerror) to implement, for signaling runtime errors and function-handler errors.

2. Test integration with API Gateway, to expose functions for HTTP calls.  I've started working on this, but API Gateway is `¯\_(ツ)_/¯`

3. Test integration with streaming invocation targets: DynamoDB streams, Kinesis, SQS, SNS

4. Implement basic AWS API request signing with openssl as described [here](http://czak.pl/2015/09/15/s3-rest-api-with-curl.html)

5. Generate or implement the most useful AWS SDK clients for a serverless app - DynamoDB, SQS, SNS, Kinesis, S3, Cloudwatch - right now we can consume events from all kinds of sources without an SDK, but producing them takes more work.  Third-party [Rust](https://github.com/rusoto/rusoto) and [OCaml](https://github.com/inhabitedtype/ocaml-aws) SDK's have both followed the strategy of generating the API clients from botocore manifests, which seems promising.

6. Start thinking about what a higher-level framework would look like. Given Scala's strength at async and streaming processes, leaning in to state-machine/reducer patterns instead of classic MVC probably makes sense.

7.  Think about using the Lambda SDK to bootstrap/deploy from the output binary itself - potentially a really interesting way to distribute serverless software.

## License

See LICENSE - this code is freely available but unsupported.   MIT License.