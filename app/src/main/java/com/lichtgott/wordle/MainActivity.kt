package com.lichtgott.wordle

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.lichtgott.wordle.databinding.ActivityMainBinding




enum class GuessColor {
    WRONG,WRONG_PLACE,RIGHT,DEFAULT
}

class MainActivity : AppCompatActivity() {


    private final var wordLength = 5;

    private lateinit var searchedWord:String;
    private lateinit var binding: ActivityMainBinding
    private lateinit var letterGrid: MutableList<Array<TextView>>

    private lateinit var keyboard: Array<Array<Button>>

    private var buttonCache: Array<Button?> = arrayOfNulls(wordLength)

    private var guessCount = 0;
    private var cursor = 0;

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        searchedWord = choseWord()

        letterGrid = MutableList(6) { row ->
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
            }
        }



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
            Toast.makeText(this,"Please enter a 5 Letter Word first", Toast.LENGTH_SHORT).show()
            return
        }

        if(false) { //tests for a real Word later
            Toast.makeText(this,"Not a valid Word",Toast.LENGTH_SHORT).show()
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

        guessCount++
        cursor = 0
        buttonCache = Array(wordLength) { null }


    }


    fun choseWord():String{
        return "LEGAT"
        //TODO: Implement
    }

    /**
     * A native method that is implemented by the 'wordle' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'wordle' library on application startup.
        init {
            System.loadLibrary("wordle")
        }
    }
}