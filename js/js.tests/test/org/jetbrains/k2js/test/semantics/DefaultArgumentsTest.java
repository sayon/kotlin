package org.jetbrains.k2js.test.semantics;

import org.jetbrains.k2js.test.SingleFileTranslationTest;

public class DefaultArgumentsTest extends SingleFileTranslationTest {

    public DefaultArgumentsTest() {
        super("defaultArguments/");
    }
    
    public void testDefArgs1() throws Exception {
        checkFooBoxIsOk();
    }

    public void testDefArgs2() throws Exception {
        checkFooBoxIsOk();
    }

    public void testEnum() throws Exception {
        checkFooBoxIsOk();
    }

    public void testEnumWithOneDefArg() throws Exception {
        checkFooBoxIsOk();
    }

    public void testEnumWithTwoDefArgs() throws Exception {
        checkFooBoxIsOk();
    }

    public void testEnumWithTwoDoubleDefArgs() throws Exception {
        checkFooBoxIsOk();
    }

    public void testOverrideValWithDefaultValue() throws Exception {
        checkFooBoxIsOk();
    }

    public void testDefArgsWithSuperCall() throws Exception {
        checkFooBoxIsOk();
    }


}
