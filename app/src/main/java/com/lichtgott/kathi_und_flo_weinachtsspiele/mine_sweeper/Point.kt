package com.lichtgott.kathi_und_flo_weinachtsspiele.mine_sweeper

class Point {
    internal enum class State {
        MARKED,
        OPENED,
        CLOSED
    }

    private var isBomb: Boolean = false
    private var state: State = State.CLOSED
    var bombesAround: Int = 0
        private set


    fun setBomb() {
        isBomb = true
    }

    fun increaseBombsAround() {
        ++bombesAround
    }

    fun getBombsAround() : Int {
        return bombesAround
    }

    fun isBomb(): Boolean {
        return isBomb
    }

    fun isMarked(): Boolean {
        return state == State.MARKED
    }

    fun hasBombNeighbor(): Boolean {
        return bombesAround != 0
    }

    fun check() {
        state = State.OPENED
    }

    fun toggleMarked(): Boolean {
        if (state == State.MARKED) {
            state = State.CLOSED
            return true
        } else if (state == State.CLOSED) {
            state = State.MARKED
            return true
        } else {
            return false
        }
    }

    fun debugString(): String {
        if (isBomb) {
            return "!$bombesAround"
        } else {
            return ".$bombesAround"
        }
    }

    fun isReadyForWin(): Boolean {
        return (state == State.OPENED).xor(isBomb)
    }

    override fun toString(): String {
        return when (state) {
            State.CLOSED -> {
                "X"
            }

            State.MARKED -> {
                "?"
            }

            State.OPENED -> {
                if (isBomb) {
                    "!!"
                } else {
                    bombesAround.toString()
                }
            }
        }
    }
}
