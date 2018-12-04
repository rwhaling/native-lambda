import scalanative.native._

@link("curl")
@extern object LibCurl {

  type Curl = Ptr[Byte]
  type MultiCurl = Ptr[Byte]
  type CurlBuffer = CStruct3[CString, CSize, CSize]
  
  @name("curl_global_init")
  def global_init(flags:Long):Unit = extern

  @name("curl_global_cleanup")
  def global_cleanup():Unit = extern

  @name("curl_easy_init")
  def easy_init():Curl = extern
  
  @name("curl_easy_cleanup")
  def easy_cleanup(handle: Curl): Unit = extern

  @name("curl_easy_setopt")
  def easy_setopt(handle: Curl, option: CInt, parameter: Any): CInt = extern
  
  @name("curl_easy_getinfo")
  def easy_getinfo(handle: Curl, info: CInt, parameter: Any): CInt = extern

  @name("curl_easy_perform")
  def easy_perform(easy_handle: Curl): CInt = extern

  @name("curl_multi_init")
  def multi_init():MultiCurl = extern

  @name("curl_multi_add_handle")
  def multi_add_handle(multi:MultiCurl, easy:Curl):Int = extern

  @name("curl_multi_perform")
  def multi_perform(multi:MultiCurl, numhandles:Ptr[Int]):Int = extern

  @name("curl_multi_cleanup")
  def multi_cleanup(multi:MultiCurl):Int = extern
}

object Curl {
  import LibCurl._
  val WRITEDATA = 10001
  val URL = 10002
  val PORT = 10003
  val USERPASSWORD = 10005
  val READDATA = 10009
  val POSTFIELDS = 10015
  val HEADERDATA = 10029
  val WRITECALLBACK = 20011
  val READCALLBACK = 20012
  val HEADERCALLBACK = 20079
  val TIMEOUT = 13
  val GET = 80
  val POST = 47
  val PUT = 54
  val CONTENTLENGTHDOWNLOADT = 0x300000 + 15

  var body = ""

  var headers = Seq[(String,String)]()

  def writeData(ptr: Ptr[Byte], size: CSize, nmemb: CSize, data: Ptr[CurlBuffer]): CSize = {
    val new_data = stdlib.realloc(!data._1, !data._2 + (size * nmemb) + 1)
    !data._1 = new_data
    // println(easy_getinfo(data.cast[Ptr[Byte]], CONTENTLENGTHDOWNLOADT, len))
    // println(s"got data of size ${size} x ${nmemb}: ${fromCString(ptr)}")
    body = body + fromCString(ptr).trim()
    return size * nmemb
  }

  def writeHeader(ptr: Ptr[Byte], size: CSize, nmemb: CSize, data: Ptr[CurlBuffer]): CSize = {
    val byteSize = size * nmemb
    println(s"got header line of size ${byteSize}: ${fromCString(ptr)}")
    // fwrite(ptr, size, nmemb, stdout)
    val headerString = fromCString(ptr)
    if (headerString.contains(": ")) {
      val pair = headerString.trim.split(": ") match {
        case Array(k,v) => k -> v
      }
      headers = headers :+ pair
    }
    return byteSize
  }

  val writeCB = CFunctionPtr.fromFunction4(writeData)
  val headerCB = CFunctionPtr.fromFunction4(writeHeader)

  var serial = 0L

  def get(url:String):(Int,Map[String,String],String) = Zone { implicit z => 
    val reqId = serial
    serial += 1
    val handle = LibCurl.easy_init()
    val readbuffer = stdlib.malloc(1)
    val readbuffer_struct = stackalloc[CurlBuffer]
    !readbuffer_struct._1 = readbuffer
    !readbuffer_struct._2 = 1
    !readbuffer_struct._3 = reqId
    val headerbuffer = stdlib.malloc(1)
    val headerbuffer_struct = stackalloc[CurlBuffer]
    !headerbuffer_struct._1 = headerbuffer
    !headerbuffer_struct._2 = 1
    !headerbuffer_struct._3 = reqId

    LibCurl.easy_setopt(handle, URL,toCString(url))
    LibCurl.easy_setopt(handle, WRITECALLBACK, writeCB)
    LibCurl.easy_setopt(handle, WRITEDATA, readbuffer_struct)
    LibCurl.easy_setopt(handle, HEADERCALLBACK, headerCB)
    LibCurl.easy_setopt(handle, HEADERDATA, headerbuffer_struct)


    val r = LibCurl.easy_perform(handle)
    val headerMap = headers.toMap
    println(s"headers: $headerMap")

    (200, headerMap, body)
  }

  def post(url:String, data:String):(Int,Map[String,String],String) = Zone { implicit z => 
    val reqId = serial
    serial += 1
    val handle = LibCurl.easy_init()
    val readbuffer = stdlib.malloc(1)
    val readbuffer_struct = stackalloc[CurlBuffer]
    !readbuffer_struct._1 = readbuffer
    !readbuffer_struct._2 = 1
    !readbuffer_struct._3 = reqId
    val headerbuffer = stdlib.malloc(1)
    val headerbuffer_struct = stackalloc[CurlBuffer]
    !headerbuffer_struct._1 = headerbuffer
    !headerbuffer_struct._2 = 1
    !headerbuffer_struct._3 = reqId

    val dataBytes = toCString(data)

    LibCurl.easy_setopt(handle, URL,toCString(url))
    LibCurl.easy_setopt(handle, WRITECALLBACK, writeCB)
    LibCurl.easy_setopt(handle, WRITEDATA, readbuffer_struct)
    LibCurl.easy_setopt(handle, HEADERCALLBACK, headerCB)
    LibCurl.easy_setopt(handle, HEADERDATA, headerbuffer_struct)
    LibCurl.easy_setopt(handle, POSTFIELDS, dataBytes);
 
    val r = LibCurl.easy_perform(handle)
    val headerMap = headers.toMap
    println(s"headers: $headerMap")

    (200, headerMap, body)
  }
}