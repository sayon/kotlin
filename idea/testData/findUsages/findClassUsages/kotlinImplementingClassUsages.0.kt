// PSI_ELEMENT: org.jetbrains.jet.lang.psi.JetClass
// OPTIONS: implementingClasses
trait <caret>X {

}

open class A: X {

}

trait Y: X {

}

open class B: A() {

}

open class C: Y {

}