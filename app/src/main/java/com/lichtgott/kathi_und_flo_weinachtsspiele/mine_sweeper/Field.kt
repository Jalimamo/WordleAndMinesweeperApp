package com.lichtgott.kathi_und_flo_weinachtsspiele.mine_sweeper

import kotlin.math.abs

class Field(length: Int, width: Int, bombCount: Int, activity : MineSweeperActivity) {
    var grid: Array<Array<Point>> = Array(length) {
        Array(width) {
            Point()
        }
    };

    var gameOver: Boolean = false;
    var bombsPlaced = false;
    var bombCount: Int = 0;

    lateinit var activity: MineSweeperActivity;

    init {
        if (bombCount < 0 || width < 0 || length < 0) {
            activity.showErrorMessage("Error! all parameters must be greater than 0!")
        } else if (bombCount > width * length) {
            print("Error! not enough space for bombs")
        } else {
            grid = Array(length) { Array(width) { Point() } }
            this.bombCount = bombCount
            this.activity = activity
        }
    }

    fun addBombs(count: Int, safeX: Int, safeY: Int) {
        val cords = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until grid.size) {
            for (x in 0 until grid[0].size) {
                if (abs(safeX - x) > 1 || abs(safeY - y) > 1) {
                    cords += x to y;
                }
            }
        }

        cords.shuffle()

        repeat(count) { i ->
            val (x, y) = cords[i]
            grid[y][x].apply {
                setBomb()
                increaseBombCountAround(x, y)
            }
        }
    }

    fun increaseBombCountAround(x: Int, y: Int) {
        for (i in y-1 until y+2) {
            for (j in x-1 until x+2) {
                if (i >= 0 && i < grid.size && j >= 0 && j < grid[0].size &&
                    !grid[i][j].isBomb()) {
                    grid[i][j].increaseBombsAround()
                }
            }
        }
    }

    fun checkPoint(x: Int, y: Int) {
        if (!bombsPlaced) {
            addBombs(bombCount, x, y)
            bombsPlaced = true
        }
        val currentPoint = grid[y][x];
        if (gameOver) {
            println("There is no game running!")
        } else if (currentPoint.isMarked()) {
            activity.showErrorMessage("You need to unmark the point!")
        } else if (currentPoint.isBomb()) {
            gameOver = true;
            activity.showGameEndDialog(false)
        } else {
            currentPoint.check()
            activity.updateButton(x,y, BtnState.Open.setValue(grid[y][x].getBombsAround()))
            bfs(x, y)
        }

        if (checkWin() && !gameOver) {
            gameOver = true;
            activity.showGameEndDialog(true)
        }
    }

    fun markPoint(x: Int, y: Int) {
        if (!bombsPlaced) {
            activity.showErrorMessage("Try checking a point first")
        } else if (gameOver) {
            activity.showErrorMessage("There is no game running!")
        } else {
            if(grid[y][x].toggleMarked()) {
                if (grid[y][x].isMarked()) {
                    activity.updateButton(x,y, BtnState.Marked)
                } else {
                    activity.updateButton(x,y, BtnState.Closed)
                }

            }
        }
    }

    fun bfs(startX: Int, startY: Int) {
        val height = grid.size
        val width = grid[0].size

        val visited = Array(height) { BooleanArray(width) }
        val  result = mutableListOf<Pair<Int, Int>>()

        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(Pair(startX, startY))

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()

            if (x !in 0 until width || y !in 0 until height) continue
            if (visited[y][x]) continue

            visited[y][x] = true

            result += x to y

            if (grid[y][x].hasBombNeighbor()) continue

            queue.add(x + 1 to y)
            queue.add(x + 1 to y+1)
            queue.add(x to y+1)
            queue.add(x-1 to y+1)
            queue.add(x-1 to y)
            queue.add(x-1 to y-1)
            queue.add(x to y-1)
            queue.add(x+1 to y-1)
        }

        result.forEach { (x, y) ->
            grid[y][x].check()
            activity.updateButton(x,y, BtnState.Open.setValue(grid[y][x].getBombsAround()))
        }
    }

    fun updateAllBombs() {
        for (y in 0 until grid.size) {
            for (x in 0 until grid[0].size) {
                val currentPoint: Point = grid[y][x]
                if (currentPoint.isBomb())
                    activity.updateButton(x,y, BtnState.Bomb)
            }
        }
    }

    fun checkWin() = grid.all {
        row -> row.all {
            point -> point.isReadyForWin()
        }
    }

    override fun toString(): String {
        //return debugToString()
        return grid.joinToString("\n") {
            row -> row.joinToString(" ") { it.toString() }
        }
    }

    fun debugToString(): String {
        return grid.joinToString("\n") {
                row -> row.joinToString(" ") { it.debugString() }
        }
    }
}
