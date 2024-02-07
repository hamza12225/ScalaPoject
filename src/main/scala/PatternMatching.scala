object PatternMatching {

  def main(args: Array[String]): Unit = {

    def describe(x: Any ) : String = x match {
      case 0 => "it's a zero"
      case 1 => "it's a one"
      case "hello" => "It's a greeting"
      case _ : Double => "It's a double"
      case _ => "It's something else"
    }
   /* println(describe(4))
    println(describe(0))
    println(describe("hello"))
    println(describe(4.7))
    */

    case class Person(name:String , age : Int)

    val person = Person("hamza",21)
    /*println(person.age)
    println(person.hashCode())
     */

    val someValue: Option[String] = Some("Hello")
    val noneValue: Option[String] = None

    println(someValue.getOrElse("Default Value"))
    println(noneValue.getOrElse("Default Value"))

  }
}
