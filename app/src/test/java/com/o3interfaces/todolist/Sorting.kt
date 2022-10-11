package com.o3interfaces.todolist

import org.junit.Test

class SortingNewISHA {

    @Test
    fun `test_1`() {
        println(solution(arrayOf(0, 1, 2, 0, 1, 2, 1, 2, 1, 0)))
    }

    @Test
    fun `test_2`() {
        println(solution(arrayOf(0, 0, 1, 2, 1, 2, 1, 0, 1, 0, 1, 1)))
    }

    @Test
    fun `test_3`() {
        println(solution(arrayOf(2, 1, 2, 1, 0, 1, 2, 1, 0, 0)))
    }

    private fun solution(numsArray: Array<Int>): String {


        val redList = arrayListOf<Int>()
        val whiteList = arrayListOf<Int>()
        val blueList = arrayListOf<Int>()


        numsArray.forEach {
            when (it) {
                0 -> redList.add(it)
                1 -> whiteList.add(it)
                2 -> blueList.add(it)
                else -> {}
            }
        }


        return arrayListOf<Int>().apply {
            addAll(redList)
            addAll(whiteList)
            addAll(blueList)
        }.joinToString(",")
    }
}