fun none() {}

fun unitEmptyInfer() {}
fun unitEmpty() : Unit {}
fun unitEmptyReturn() : Unit {return}
fun unitIntReturn() : Unit {return <!TYPE_MISMATCH!>1<!>}
fun unitUnitReturn() : Unit {return Unit.VALUE}
fun test1() : Any = {<!RETURN_NOT_ALLOWED, RETURN_TYPE_MISMATCH!>return<!>}
fun test2() : Any = @a {<!RETURN_NOT_ALLOWED_EXPLICIT_RETURN_TYPE_REQUIRED!>return@a 1<!>}
fun test3() : Any { <!RETURN_TYPE_MISMATCH!>return<!> }
fun test4(): ()-> Unit = { <!RETURN_NOT_ALLOWED, RETURN_TYPE_MISMATCH!>return@test4<!> }
fun test5(): Any = @{ <!RETURN_NOT_ALLOWED_EXPLICIT_RETURN_TYPE_REQUIRED!>return@<!> }
fun test6(): Any = {<!RETURN_NOT_ALLOWED!>return 1<!>}

fun bbb() {
  return <!TYPE_MISMATCH!>1<!>
}

fun foo(<!UNUSED_PARAMETER!>expr<!>: StringBuilder): Int {
  val c = 'a'
  when(c) {
    0.toChar() -> throw Exception("zero")
    else -> throw Exception("nonzero" + c)
  }
}


fun unitShort() : Unit = Unit.VALUE
fun unitShortConv() : Unit = <!TYPE_MISMATCH!>1<!>
fun unitShortNull() : Unit = <!TYPE_MISMATCH!>null<!>

fun intEmpty() : Int {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun intShortInfer() = 1
fun intShort() : Int = 1
//fun intBlockInfer()  {1}
fun intBlock() : Int {return 1}
fun intBlock1() : Int {<!UNUSED_EXPRESSION!>1<!><!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun intString(): Int = <!TYPE_MISMATCH!>"s"<!>
fun intFunctionLiteral(): Int = <!TYPE_MISMATCH!>{ 10 }<!>

fun blockReturnUnitMismatch() : Int {<!RETURN_TYPE_MISMATCH!>return<!>}
fun blockReturnValueTypeMismatch() : Int {return <!ERROR_COMPILE_TIME_VALUE!>3.4<!>}
fun blockReturnValueTypeMatch() : Int {return 1}
fun blockReturnValueTypeMismatchUnit() : Int {return <!TYPE_MISMATCH!>Unit.VALUE<!>}

fun blockAndAndMismatch() : Int {
  true && false
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun blockAndAndMismatch1() : Int {
  return <!TYPE_MISMATCH!>true && false<!>
}
fun blockAndAndMismatch2() : Int {
  <!UNREACHABLE_CODE!>(return <!ERROR_COMPILE_TIME_VALUE!>true<!>) && (return <!ERROR_COMPILE_TIME_VALUE!>false<!>)<!>
}

fun blockAndAndMismatch3() : Int {
  true || false
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun blockAndAndMismatch4() : Int {
  return <!TYPE_MISMATCH!>true || false<!>
}
fun blockAndAndMismatch5() : Int {
  <!UNREACHABLE_CODE!>(return <!ERROR_COMPILE_TIME_VALUE!>true<!>) || (return <!ERROR_COMPILE_TIME_VALUE!>false<!>)<!>
}
fun blockReturnValueTypeMatch1() : Int {
  return if (1 > 2) <!ERROR_COMPILE_TIME_VALUE!>1.0<!> else <!ERROR_COMPILE_TIME_VALUE!>2.0<!>
}
fun blockReturnValueTypeMatch2() : Int {
  return <!TYPE_MISMATCH!>if (1 > 2) 1<!>
}
fun blockReturnValueTypeMatch3() : Int {
  return <!TYPE_MISMATCH!>if (1 > 2) else 1<!>
}
fun blockReturnValueTypeMatch4() : Int {
  if (1 > 2)
    return <!ERROR_COMPILE_TIME_VALUE!>1.0<!>
  else return <!ERROR_COMPILE_TIME_VALUE!>2.0<!>
}
fun blockReturnValueTypeMatch5() : Int {
  if (1 > 2)
    return <!ERROR_COMPILE_TIME_VALUE!>1.0<!>
  return <!ERROR_COMPILE_TIME_VALUE!>2.0<!>
}
fun blockReturnValueTypeMatch6() : Int {
  if (1 > 2)
    else return <!ERROR_COMPILE_TIME_VALUE!>1.0<!>
  return <!ERROR_COMPILE_TIME_VALUE!>2.0<!>
}
fun blockReturnValueTypeMatch7() : Int {
  if (1 > 2)
    1.0
  else 2.0
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun blockReturnValueTypeMatch8() : Int {
  if (1 > 2)
    1.0
  else 2.0
  return 1
}
fun blockReturnValueTypeMatch9() : Int {
  if (1 > 2)
    1.0
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun blockReturnValueTypeMatch10() : Int {
  return <!TYPE_MISMATCH!>if (1 > 2)
    1<!>
}
fun blockReturnValueTypeMatch11() : Int {
  if (1 > 2)
  else 1.0
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun blockReturnValueTypeMatch12() : Int {
  if (1 > 2)
    return 1
  else return <!ERROR_COMPILE_TIME_VALUE!>1.0<!>
}
fun blockNoReturnIfValDeclaration(): Int {
  val <!UNUSED_VARIABLE!>x<!> = 1
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun blockNoReturnIfEmptyIf(): Int {
  if (1 < 2) {} else {}
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun blockNoReturnIfUnitInOneBranch(): Int {
  if (1 < 2) {
    return 1
  } else {
    if (3 < 4) {
    } else {
      return 2
    }
  }
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun nonBlockReturnIfEmptyIf(): Int = if (1 < 2) <!TYPE_MISMATCH!>{}<!> else <!TYPE_MISMATCH!>{}<!>
fun nonBlockNoReturnIfUnitInOneBranch(): Int = if (1 < 2) <!TYPE_MISMATCH!>{}<!> else 2

val a = <!RETURN_NOT_ALLOWED!>return 1<!>

class A() {
}
fun illegalConstantBody(): Int = <!TYPE_MISMATCH!>"s"<!>
fun illegalConstantBlock(): String {
    return <!ERROR_COMPILE_TIME_VALUE!>1<!>
}
fun illegalIfBody(): Int =
    if (1 < 2) <!ERROR_COMPILE_TIME_VALUE!>'a'<!> else { <!ERROR_COMPILE_TIME_VALUE!>1.0<!> }
fun illegalIfBlock(): Boolean {
    if (1 < 2)
        return false
    else { return <!ERROR_COMPILE_TIME_VALUE!>1<!> }
}
fun illegalReturnIf(): Char {
    return if (1 < 2) 'a' else { <!ERROR_COMPILE_TIME_VALUE!>1<!> }
}

fun returnNothing(): Nothing {
    throw <!ERROR_COMPILE_TIME_VALUE!>1<!>
}
fun f(): Int {
    if (1 < 2) { return 1 } else returnNothing()
}

fun f1(): Int = if (1 < 2) 1 else returnNothing()

<!PUBLIC_MEMBER_SHOULD_SPECIFY_TYPE!>public fun f2()<!> = 1
class B() {
   <!PUBLIC_MEMBER_SHOULD_SPECIFY_TYPE!>protected fun f()<!> = "ss"
}

fun testFunctionLiterals() {
    val <!UNUSED_VARIABLE!>endsWithVarDeclaration<!> : () -> Boolean = {
        <!EXPECTED_TYPE_MISMATCH!>val <!UNUSED_VARIABLE!>x<!> = 2<!>
    }

    val <!UNUSED_VARIABLE!>endsWithAssignment<!> = { () : Int ->
        var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>x<!> = 1
        <!EXPECTED_TYPE_MISMATCH!>x = <!UNUSED_VALUE!>333<!><!>
    }

    val <!UNUSED_VARIABLE!>endsWithReAssignment<!> = { () : Int ->
        var x = 1
        <!ASSIGNMENT_TYPE_MISMATCH!>x += 333<!>
    }

    val <!UNUSED_VARIABLE!>endsWithFunDeclaration<!> : () -> String = {
        var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>x<!> = 1
        x = <!UNUSED_VALUE!>333<!>
        <!EXPECTED_TYPE_MISMATCH!>fun meow() : Unit {}<!>
    }

    val <!UNUSED_VARIABLE!>endsWithObjectDeclaration<!> : () -> Int = {
        var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>x<!> = 1
        x = <!UNUSED_VALUE!>333<!>
        <!EXPECTED_TYPE_MISMATCH!>object A {}<!>
    }

    val <!UNUSED_VARIABLE!>expectedUnitReturnType1<!> = { () : Unit ->
        val <!UNUSED_VARIABLE!>x<!> = 1
    }

    val <!UNUSED_VARIABLE!>expectedUnitReturnType2<!> = { () : Unit ->
        fun meow() : Unit {}
        object A {}
    }

}