package com.lichtgott.kathi_und_flo_weinachtsspiele

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.setPadding
import com.lichtgott.kathi_und_flo_weinachtsspiele.databinding.ActivityWordleBinding




enum class GuessColor {
    WRONG,WRONG_PLACE,RIGHT,DEFAULT
}

class WordleActivity : AppCompatActivity() {


    private var debugMode = false;

    private val wordLength = 5;

    private val defaultGuessCount = 6;

    private lateinit var searchedWord:String;
    private lateinit var binding: ActivityWordleBinding
    private lateinit var letterGrid: MutableList<Array<TextView>>

    private lateinit var keyboard: Array<Array<Button>>

    private var buttonCache: Array<Button?> = arrayOfNulls(wordLength)

    private var guessCount = 0;
    private var cursor = 0;

    private var overtime = false;

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWordleBinding.inflate(layoutInflater)
        setContentView(binding.root)



        letterGrid = MutableList(defaultGuessCount) { row ->
            Array (wordLength) {col ->
                val resId = resources.getIdentifier("letterView${row+1}_${col+1}", "id" , packageName)
                findViewById<TextView>(resId)
            }
        }

        keyboard = Array(3) { row ->
            Array(if (row == 2) 9 else 11) { col ->
                val resId = resources.getIdentifier("button${row + 1}_${col + 1}", "id", packageName)
                findViewById<Button>(resId)
            }
        }



        start()



        // Example of a call to a native method
        //binding.sampleText.text = stringFromJNI()
    }


    fun keyPressHandler(btn: Button){

        if (btn.tag == "ENTER"){
            enterHandler()
            return
        }

        if (btn.tag == "BACKSPACE"){
            backspaceHandler()
            return
        }


        if(cursor >= wordLength){
            Toast.makeText(this,"End of Line",Toast.LENGTH_SHORT).show()
            return
        }
        buttonCache[cursor] = btn;
        letterGrid[guessCount][cursor++].text = btn.text

    }




    fun backspaceHandler(){
        if(cursor == 0){
            Toast.makeText(this,"Start of Line",Toast.LENGTH_SHORT).show()
            return
        }
        letterGrid[guessCount][--cursor].text = ""
        buttonCache[cursor] = null
    }




    fun enterHandler(){
        if(cursor != wordLength) {
            if(debugMode){
                Toast.makeText(this,searchedWord, Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Please enter a 5 Letter Word first", Toast.LENGTH_SHORT).show()
            }
            return
        }

        var word = ""
        for(i in 0..wordLength - 1){
            word += letterGrid[guessCount][i].text
        }

        if(!assets.open("wordlist.txt").bufferedReader().readLines().contains(word)){
            Toast.makeText(this,"Not a valid Word",Toast.LENGTH_SHORT).show()
            return
        }



        val colorCache: Array<GuessColor> = Array(wordLength) { gColor ->
            GuessColor.WRONG
        }

        //Finding Right Letters at Right Places
        for(i in 0..wordLength - 1){    //Iterates trough searchedWord
            if(searchedWord[i] + "" == letterGrid[guessCount][i].text){
                colorCache[i] = GuessColor.RIGHT
            }
        }

        //Finding Right Letters at wrong Places
        var foundLetter = false;
        for(i in 0..wordLength - 1){    //Iterates trough searchedWord
            if(colorCache[i] == GuessColor.RIGHT){
                continue
            }
            for (j in 0..wordLength - 1){   //Iterates Through Inputted Word (letterGrid)
                if(colorCache[j] == GuessColor.WRONG && searchedWord[i] + "" == letterGrid[guessCount][j].text){
                    colorCache[j] = GuessColor.WRONG_PLACE
                    foundLetter = true
                    break;
                }
            }
            if(foundLetter){
                foundLetter = false
                continue
            }

        }

        //Coloring everything right

        for(i in 0..wordLength - 1){
            when(colorCache[i]){
                GuessColor.WRONG -> {
                    letterGrid[guessCount][i].setBackgroundResource(R.drawable.textview_border_wrong)
                    if (buttonCache[i]?.backgroundTintList?.equals(getColorStateList(R.color.defaultColorKeys))
                            ?: false
                    ) {
                        buttonCache[i]?.backgroundTintList = getColorStateList(R.color.wrongLetter)
                    }
                }
                GuessColor.WRONG_PLACE -> {
                    letterGrid[guessCount][i].setBackgroundResource(R.drawable.textview_border_wrongplace)
                    if(!(buttonCache[i]?.backgroundTintList?.equals(getColorStateList(R.color.rightLetter)) ?: true)){    //true gets inverted to false
                        buttonCache[i]?.backgroundTintList = getColorStateList(R.color.wrongPlace)
                    }
                }
                GuessColor.RIGHT -> {
                    letterGrid[guessCount][i].setBackgroundResource(R.drawable.textview_border_right)
                    buttonCache[i]?.backgroundTintList = getColorStateList(R.color.rightLetter)
                }
                else -> Toast.makeText(this,"Something went wrong with the ColorCache",Toast.LENGTH_LONG).show()
            }

        }

        if(word == searchedWord){
            AlertDialog.Builder(this)
                .setTitle("You won!")
                .setMessage("You got the right word ${searchedWord} in ${guessCount} Trys!")
                .setPositiveButton("New Word") { _, _ -> start()}
                .setNegativeButton("Beenden") {_,_ -> backToMenu() }
                .show()

            return

        }

        guessCount++
        cursor = 0
        buttonCache = Array(wordLength) { null }

        if(guessCount >= defaultGuessCount && !overtime) {
            AlertDialog.Builder(this)
                .setTitle("You ran out of Guesses")
                .setMessage("Do you want to continue?")
                .setPositiveButton("Yes") { _, _ ->
                    overtime = true
                    createNewLine()
                }
                .setNegativeButton("No") { _, _ ->

                    AlertDialog.Builder(this)
                        .setTitle("You lost!")
                        .setMessage("The word was ${searchedWord}!")
                        .setPositiveButton("New Word") { _, _ -> start()}
                        .setNegativeButton("Beenden") {_,_ -> backToMenu() }
                        .show()

                }
                .show()
        }

        if(overtime){
            createNewLine()
        }


    }


    fun choseWord():String{
        return assets.open("resultlist.txt").bufferedReader().readLines().random()
    }

    fun start(){
        val tableLayout = findViewById<TableLayout>(R.id.Guesses)


        for(row in tableLayout.children.toList()){//toList is used to copy the child-Sequence
            if(row.tag == "OvertimeRow") {
                tableLayout.removeView(row)
            }
        }

        letterGrid = letterGrid.subList(0,defaultGuessCount)

        for (row in letterGrid){
            for (cell in row){
                cell.text = ""
                cell.setBackgroundResource(R.drawable.textview_border_default)
            }
        }

        for (row in keyboard) {
            for (button in row) {
                button.setOnClickListener {
                    val btn = it as Button
                    keyPressHandler(btn)
                }
                button.backgroundTintList = getColorStateList(R.color.defaultColorKeys)
            }
        }

        searchedWord = choseWord()

        guessCount = 0

        cursor = 0

        overtime = false
    }


    fun createNewLine(){

        val tableLayout = findViewById<TableLayout>(R.id.Guesses)

        val newRow = TableRow(this)



        for (i in 0..wordLength - 1){
            val newLetter = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.LetterWidth),
                    resources.getDimensionPixelSize(R.dimen.LetterHeight)
                )
                setBackgroundResource(R.drawable.textview_border_default)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                text = ""
                setEms(10)
                setPadding(resources.getDimensionPixelSize(R.dimen.LetterPadding))
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.LetterTextSize)
                )
            }
            newRow.addView(newLetter)
        }

        newRow.tag = "OvertimeRow"

        letterGrid.add(Array(wordLength) { col ->
            newRow[col] as TextView
        })

        tableLayout.addView(newRow)
    }

    fun backToMenu() {
        val intent = Intent(this, MainMenu::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    companion object {
        // Used to load the 'wordle' library on application startup.
        init {
            System.loadLibrary("wordle")
        }
    }
}