package ua.pp.soulrise.ashtakavarga.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import ua.pp.soulrise.ashtakavarga.R

class HelpActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        // Consider using finish() for standard up navigation behavior
        // onBackPressedDispatcher.onBackPressed() // Modern way
        finish() // More common for Up button
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val expandableListView = findViewById<ExpandableListView>(R.id.helpExpandableListView)

        val helpData = mapOf(
            "Начало работы" to listOf(
                "1. После запуска программы заполните строки с персональными данными: напишите «Имя», «Дату рождения», «Время рождения», «Место рождения» (село, поселок, город, область, республика).",
                "2. Нажмите на копку «Добавить клиента»",
                "3. Строки для введения данных очистятся и появится имя нового клиента в списке клиентов."
            ),
            "Ввод данных Аштакаварги" to listOf(
                "1. Щелкните на нужном имени в списке, и тогда откроется первое окно ввода данных системы Аштакаварга",
                "2. Откройте любую программу на компьютере или онлайн, которая делает расчет таблицы системы Аштакаварга, и составьте свой гороскоп на момент рождения пользователя.",
                "3. Внесите данные таблицы Аштакаварги из программы на компьютере в программу на телефоне."
            ),
            "Карта и транзиты" to listOf(
                "1. Нажмите кнопку «Карта и транзиты».",
                "2. Откройте гороскоп на момент своего рождения в программе на компьютере",
                "3. В первом списке напротив каждой планет из ниспадающего списка выберите знак зодиак, в котором находится планета в вашем гороскопе.",
                "4. Составьте гороскоп на данный момент в программе на компьютере.",
                "5. Во втором списке (Транзиты) напротив каждой планеты из ниспадающего списка выберите знак зодиака, в котором находится планета в данный момент."
            ),
            "Интерпретация результатов" to listOf(
                "1. После ввода всех данных, программа покажет, в каком состоянии (позитив, средне, негатив) каждая планета находилась на момент рождения и в каком состоянии находится планета в данный момент. Состояние планеты на данный момент дает нам понимание, какое влияние оказывает на нас каждая планета в данный момент.",
                "2. Мы можем составить гороскоп на любой момент времени в будущем, затем внести данные в программу на телефоне, и узнать, какое влияние на нас будет оказывать каждая планета в какой-то конкретный момент времени в будущем. Таким образом мы можем составлять прогнозы и учитывать изменение состояния планет и их влияние (позитивное или негативное) на нас в будущем."
            ),
            "Благодарности" to listOf(
                "Постановка задачи: <a href=\"https://t.me/VishvaDevaGiri\">Вишва Дева Гири</a>",
                "Разработка: <a href=\"https://t.me/Premananda1\">Премананда<br></a>",
                "            <a href=\"https://www.anthropic.com/news/claude-3-5-sonnet\">Claude 3.5 Sonnet<br></a>",
                "            <a href=\"http://aistudio.google.com/app/prompts/new_chat?model=gemini-2.5-pro-exp-03-25\">Gemini-2.5-Pro-Exp</a>"
            )
        )

        val groups = helpData.keys.toList()
        // Use HtmlCompat for parsing
        val children: List<List<Spanned>> = helpData.values.map { items ->
            items.map { item ->
                HtmlCompat.fromHtml(item, HtmlCompat.FROM_HTML_MODE_LEGACY) // Or FROM_HTML_MODE_COMPACT
            }
        }

        val adapter = CustomExpandableListAdapter(this, groups, children)
        expandableListView.setAdapter(adapter)

        // REMOVE THIS LISTENER - it's not needed for links anymore
        /*
        expandableListView.setOnChildClickListener { parent, view, groupPosition, childPosition, id ->
            // This is no longer necessary for link clicking
            // val textView = view.findViewById<TextView>(android.R.id.text2)
            // textView.movementMethod = LinkMovementMethod.getInstance()
            true // Return true if you handled the click, false otherwise
        }
        */
    }
}