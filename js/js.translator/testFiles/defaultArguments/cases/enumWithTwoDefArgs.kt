package foo
enum class Foo(val a: Int = 1, val b: String = "a") {
  A: Foo()
  B: Foo(2, "b")
  C: Foo(b = "b")
  D: Foo(a = 2)
}

fun box(): String {
   if (Foo.A.a != 1 || Foo.A.b != "a") return "fail"
   if (Foo.B.a != 2 || Foo.B.b != "b") return "fail"
   if (Foo.C.a != 1 || Foo.C.b != "b") return "fail"
   if (Foo.D.a != 2 || Foo.D.b != "a") return "fail"
   return "OK"
}
