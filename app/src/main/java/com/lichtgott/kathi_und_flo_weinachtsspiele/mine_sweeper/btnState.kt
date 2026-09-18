package com.lichtgott.kathi_und_flo_weinachtsspiele.mine_sweeper

enum class BtnState(var value: Int? = null) {
    Open(0),
    Closed,
    Marked,
    Bomb;

    fun setValue(value: Int): BtnState {
        this.value = value
        return this
    }
}