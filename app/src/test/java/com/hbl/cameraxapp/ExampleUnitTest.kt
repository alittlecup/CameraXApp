package com.hbl.cameraxapp

import android.os.AsyncTask
import com.hbl.cameraxapp.test.Animal
import org.json.JSONObject
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
            val arrays = arrayOf("one", "two")
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

    @Test
    fun testJson() {
        var string = """
            {"data":{"beard":0,"contour":3,"eye":2,"eye_color":2,"eyebrow":2,"glasses":0,"hair":11,"hair_color":9,"hair_direction":0,"mouth":2,"nose":2,"skin_color":32},"meta":{"code":200}}
        """.trimIndent()
        val mockResponse =
            "{\"data\":{\"beard\":0,\"contour\":3,\"eye\":2,\"eye_color\":2,\"eyebrow\":2,\"glasses\":0,\"hair\":11,\"hair_color\":9,\"hair_direction\":0,\"mouth\":2,\"nose\":2,\"skin_color\":32},\"meta\":{\"code\":200}}"
        var jsonObject = JSONObject(string)
        var string1 = jsonObject.getJSONObject("data").getString("beard")
        println(string1)
    }

    @Test
    fun testAnimal() {
        var animal = Animal()
    }
}
