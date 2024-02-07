object Main {
  def main(args: Array[String]): Unit = {
    // creating immutable list
    val myList:List[Int] = List(1,2,4,5,6)
    //println(myList)
    // operation on list
    val doubledList : List[Int] = myList.map(_ * 2)
    val fileredList : List[Int] = myList.filter(_ % 2 == 0)
    val sum : Int = myList.foldRight(1)(_ * _)

    // set

    val mySet : Set[String] = Set("appele","banana","orange")
    val modifed:Set[String] = mySet + "grapes" // add an element to set
    //println(modifed)

    // Map comme des tableau assciative

    val Maped: Map[String,Int] = Map("one" -> 1, "two" -> 2 ,"three" -> 3)
    val UpdatedMap : Map[String,Int] = Maped + ("four" -> 4)
    //println(UpdatedMap.values)

    // Higher-order function that takes a function as an argument
    def ApplyOperation(x:Int,y:Int , operation:(Int,Int) => Int):Int = {
      operation(x,y)
    }
    val add: (Int, Int) => Int = (a, b) => a + b
    val mutliply: (Int,Int) => Int = (a,b) => a * b

    // Using the higher-order function with different operations

    val ResaultAdd:Int = ApplyOperation(4,6,add)
    val Product:Int = ApplyOperation(4,6,mutliply)

    println(ResaultAdd)
    println(Product)

  }
}