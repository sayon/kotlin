class B extends A {

}

interface Y extends X {

}

class Usages {
    void foo(X x) {

    }

    void foo(Y y) {

    }

    void foo(A a) {

    }

    void foo(B b) {

    }

    void foo(C c) {

    }
}