package com.lichtgott.kathi_und_flo_weinachtsspiele.mine_sweeper

import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lichtgott.kathi_und_flo_weinachtsspiele.R

class MineSweeperActivity : AppCompatActivity() {

    var height = 9;
    var width = 5;
    var bombCount = 5;

    var mineMode = true;

    var field = Field(height, width, 5, this);

    var btnGrid: Array<Array<Button?>> = Array(height) { Array(width) { null } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mine_sweeper)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }


        val mineBtn = findViewById<Button>(R.id.btnModeMine)
        val markBtn = findViewById<Button>(R.id.btnModeMark)

        mineBtn.setOnClickListener {
            mineMode = true
            mineBtn.backgroundTintList = getColorStateList(R.color.active)
            markBtn.backgroundTintList = getColorStateList(R.color.inactive)
        }

        markBtn.setOnClickListener {
            mineMode = false
            mineBtn.backgroundTintList = getColorStateList(R.color.inactive)
            markBtn.backgroundTintList = getColorStateList(R.color.active)
        }

        mineBtn.backgroundTintList = getColorStateList(R.color.active)
        markBtn.backgroundTintList = getColorStateList(R.color.inactive)

        fillGrid()
    }



    fun showNewGameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.mine_sweeper_dialog_new_game, null)

        AlertDialog.Builder(this)
            .setTitle("Dimensionen")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val widthInput = dialogView.findViewById<EditText>(R.id.inputCols).text
                val heightInput = dialogView.findViewById<EditText>(R.id.inputRows).text
                val bombCountInput = dialogView.findViewById<EditText>(R.id.inputBombCount).text

                try {
                    width = Integer.parseInt(widthInput.toString())
                    height = Integer.parseInt(heightInput.toString())
                    bombCount = Integer.parseInt(bombCountInput.toString())

                    if (width <= 0 || height <= 0 || bombCount <= 0) {
                        throw RuntimeException();
                    }

                    fillGrid()

                } catch (_ : RuntimeException) {
                    Toast.makeText(this, "all Inputs must be Numbers greater than 0", Toast.LENGTH_SHORT).show()
                    showNewGameDialog()
                }

            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    fun showGameEndDialog(won: Boolean) {
        field.updateAllBombs()
        val title = if (won) "Gewonnen!" else "Verloren!"
        val message = if (won)
            "Du hast das Spiel gewonnen 🎉"
        else
            "Du hast verloren 💥"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)

            // 🔁 Neues Spiel, gleiche Parameter
            .setPositiveButton("Neues Spiel") { _, _ ->
                fillGrid()
            }

            // ⚙️ Neues Spiel, neue Parameter
            .setNeutralButton("Neue Parameter") { _, _ ->
                showNewGameDialog()
            }

            // 🏠 Hauptmenü
            .setNegativeButton("Hauptmenü") { _, _ ->
                finish()   // Activity beenden → zurück zum Menü
            }

            .setCancelable(false)
            .show()
    }


    fun fillGrid() {
        val grid = findViewById<GridLayout>(R.id.gameGrid)
        grid.removeAllViews()
        grid.columnCount = width
        grid.rowCount = height

        btnGrid = Array(height) { Array(width) { null } }

        for (i in 0 until height) {
            for (j in 0 until width) {
                val btn = Button(this)
                grid.addView(btn)
                btnGrid[i][j] = btn

                btn.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(2, 2, 2, 2)
                }

                btn.textSize = 18f
                btn.setTypeface(null, Typeface.BOLD)


                btn.setOnClickListener {
                    onMinePress(j, i)
                }
            }
        }

        grid.post {
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val btn = btnGrid[i][j] ?: continue
                    val size = btn.width
                    btn.layoutParams = btn.layoutParams.apply {
                        height = size
                    }
                    btn.requestLayout()
                }
            }
        }

        field = Field(height, width, bombCount, this)

    }

    fun onMinePress(x: Int, y: Int) {
        if (mineMode) {
            field.checkPoint(x,y)
        } else {
            field.markPoint(x,y)
        }
    }

    fun showWonMessage() {
        showErrorMessage("Won")
    }

    fun showErrorMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun updateButton(x: Int, y:Int, btnState: BtnState) {
        val currentBtn = btnGrid[y][x]
        currentBtn ?: return
        when (btnState) {
            BtnState.Closed -> {
               currentBtn.backgroundTintList = getColorStateList(R.color.closed)
                currentBtn.text = ""
            }

            BtnState.Marked -> {
                currentBtn.backgroundTintList = getColorStateList((R.color.marked))
                currentBtn.text = ""
            }

            BtnState.Bomb -> {
                currentBtn.backgroundTintList = getColorStateList(R.color.bomb)
                currentBtn.text = ""
            }

            BtnState.Open -> {
                currentBtn.backgroundTintList = getColorStateList(R.color.opened)
                currentBtn.setTextColor( getColor(when (btnState.value) {
                    1 -> R.color.openedNumber1
                    2 -> R.color.openedNumber2
                    3 -> R.color.openedNumber3
                    4 -> R.color.openedNumber4
                    5 -> R.color.openedNumber5
                    6 -> R.color.openedNumber6
                    7 -> R.color.openedNumber7
                    8 -> R.color.openedNumber8
                    else -> android.R.color.transparent
                    } ))
                currentBtn.text = "${btnState.value}"
            }
        }
    }


}