package com.hbl.cameraxapp

import android.os.AsyncTask
import org.junit.Test

import org.junit.Assert.*
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        thread(start = true) {
            println("task start")
            val arrays = arrayOf("one","two")
            val semaphore = Semaphore(0)
            thread(start = true) {
                println("inner thread start")
                Thread.sleep(1000)
                arrays[0] = "fix"
                semaphore.release()
                println("inner thread end")
            }.join()
            semaphore.acquire()
            println("task end $arrays")
        }.join()
    }
}
