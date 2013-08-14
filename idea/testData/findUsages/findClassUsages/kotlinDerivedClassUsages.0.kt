// PSI_ELEMENT: org.jetbrains.jet.lang.psi.JetClass
// OPTIONS: derivedClasses
trait X {

}

open class <caret>A: X {

}

trait Y: X {

}

open class B: A() {

}

open class C: Y {

}