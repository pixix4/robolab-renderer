package de.robolab.common.testing

open class TestRunException(val run: ITestRun, message: String?, throwable: Throwable?) :
    Exception(message, throwable) {
    constructor(run: ITestRun, message: String?) : this(run, message, null)
    constructor(run: ITestRun, throwable: Throwable?) : this(run, null, throwable)
    constructor(run: ITestRun) : this(run, null, null)
}

open class TestRunFailedException(run: ITestRun, message: String?, throwable: Throwable?) :
    TestRunException(run, message, throwable) {
    constructor(run: ITestRun, message: String?) : this(run, message, null)
    constructor(run: ITestRun, throwable: Throwable?) : this(run, null, throwable)
    constructor(run: ITestRun) : this(run, null, null)
}

open class TestRunSkippedException(run: ITestRun, message: String?, throwable: Throwable?) :
    TestRunException(run, message, throwable) {
    constructor(run: ITestRun, message: String?) : this(run, message, null)
    constructor(run: ITestRun, throwable: Throwable?) : this(run, null, throwable)
    constructor(run: ITestRun) : this(run, null, null)
}