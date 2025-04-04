package ua.pp.soulrise.ashtakavarga.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ua.pp.soulrise.ashtakavarga.R

class HelpActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // Настройка toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val expandableListView = findViewById<ExpandableListView>(R.id.helpExpandableListView)

        // Создаем структуру данных для справки
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
            "Блогодарности" to listOf(
                "Постаномка задачи: <a href=\"https://t.me/VishvaDevaGiri\">Вишва Дева Гири</a>",
                "Разработка: <a href=\"https://t.me/Premananda1\">Премананда</a>"
            )
        )

        // Подготавливаем данные для адаптера
        val groups = helpData.keys.toList()
        val children = helpData.values.map { items ->
            items.map { item ->
                Html.fromHtml(item, Html.FROM_HTML_MODE_COMPACT)
            }
        }

        // Создаем адаптер
        val adapter = CustomExpandableListAdapter(this, groups, children)

        expandableListView.setAdapter(adapter)

        // Обработка нажатий на дочерние элементы
        expandableListView.setOnChildClickListener { parent, view, groupPosition, childPosition, id ->
            val textView = view.findViewById<TextView>(android.R.id.text2)
            textView.movementMethod = LinkMovementMethod.getInstance()
            true
        }
    }
}