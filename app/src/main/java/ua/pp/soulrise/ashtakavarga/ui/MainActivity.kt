package ua.pp.soulrise.ashtakavarga.ui

import ua.pp.soulrise.ashtakavarga.databinding.ActivityMainBinding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels // Для viewModels делегата
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope // Для viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ua.pp.soulrise.ashtakavarga.common.Planet
import ua.pp.soulrise.ashtakavarga.common.ZodiacSign
import ua.pp.soulrise.ashtakavarga.data.AppDatabase
import ua.pp.soulrise.ashtakavarga.data.AstrologyDao
import ua.pp.soulrise.ashtakavarga.data.PlanetaryPositionEntity
import ua.pp.soulrise.ashtakavarga.R

// --- ViewModel для управления данными ---
class MainViewModel(private val dao: AstrologyDao) : ViewModel() {

    // Получаем Flow данных из DAO
    private val userIdFlow = MutableStateFlow<Long>(1)

    // Flow данных, который переключается при изменении userId
    val allPositionsFlow: Flow<List<PlanetaryPositionEntity>> = userIdFlow.flatMapLatest { userId ->
        dao.getAllPlanetaryPositions(userId.toLong())
    }

    fun setUserId(newUserId: Long) {
        userIdFlow.value = newUserId // Обновляем userId, что вызовет переключение Flow
    }

    // Функция для сохранения данных

    fun saveData(dataToSave: List<Triple<Int, Int, Int?>>) {
        viewModelScope.launch {
            dataToSave.forEach { (planetId, signId, value) ->
                dao.upsertPlanetaryPosition(planetId, signId, userId = userIdFlow.value, value = value) // Используем актуальный userId из Flow
            }
        }
    }

    // Фабрика для создания ViewModel с зависимостью DAO
    class MainViewModelFactory(private val dao: AstrologyDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class ZodiacPlanetData(
    val signId: Int,
    val signName: String,
    val editTextIds: Array<Int>, // Массив ID EditText для этого знака
    var sumTextViewId: Int       // ID TextView для суммы этого знака
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var userId: Long = 1 // Default value
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory(
            AppDatabase.getDatabase(applicationContext).astrologyDao()
        )
    }

    private lateinit var zodiacSumTextViews: Array<TextView>
    private lateinit var planetSumTextViews: Array<TextView>
    private lateinit var editTextList: List<EditText>

    // Структура данных остается прежней для удобства работы с UI
    private val zodiacDataList = listOf(
        ZodiacPlanetData(
            ZodiacSign.ARIES,
            "Овен ♈",
            arrayOf(
                R.id.etAriesSun,
                R.id.etAriesMoon,
                R.id.etAriesMars,
                R.id.etAriesMercury,
                R.id.etAriesJupiter,
                R.id.etAriesVenus,
                R.id.etAriesSaturn,
                R.id.etAriesHouse
            ),
            R.id.tvAriesSum
        ),
        ZodiacPlanetData(
            ZodiacSign.TAURUS,
            "Телец ♉",
            arrayOf(
                R.id.etTaurusSun,
                R.id.etTaurusMoon,
                R.id.etTaurusMars,
                R.id.etTaurusMercury,
                R.id.etTaurusJupiter,
                R.id.etTaurusVenus,
                R.id.etTaurusSaturn,
                R.id.etTaurusHouse
            ),
            R.id.tvTaurusSum
        ),
        ZodiacPlanetData(
            ZodiacSign.GEMINI,
            "Близнецы ♊",
            arrayOf(
                R.id.etGeminiSun,
                R.id.etGeminiMoon,
                R.id.etGeminiMars,
                R.id.etGeminiMercury,
                R.id.etGeminiJupiter,
                R.id.etGeminiVenus,
                R.id.etGeminiSaturn,
                R.id.etGeminiHouse
            ),
            R.id.tvGeminiSum
        ),
        ZodiacPlanetData(
            ZodiacSign.CANCER,
            "Рак ♋",
            arrayOf(
                R.id.etCancerSun,
                R.id.etCancerMoon,
                R.id.etCancerMars,
                R.id.etCancerMercury,
                R.id.etCancerJupiter,
                R.id.etCancerVenus,
                R.id.etCancerSaturn,
                R.id.etCancerHouse
            ),
            R.id.tvCancerSum
        ),
        ZodiacPlanetData(
            ZodiacSign.LEO,
            "Лев ♌",
            arrayOf(
                R.id.etLeoSun,
                R.id.etLeoMoon,
                R.id.etLeoMars,
                R.id.etLeoMercury,
                R.id.etLeoJupiter,
                R.id.etLeoVenus,
                R.id.etLeoSaturn,
                R.id.etLeoHouse
            ),
            R.id.tvLeoSum
        ),
        ZodiacPlanetData(
            ZodiacSign.VIRGO,
            "Дева ♍",
            arrayOf(
                R.id.etVirgoSun,
                R.id.etVirgoMoon,
                R.id.etVirgoMars,
                R.id.etVirgoMercury,
                R.id.etVirgoJupiter,
                R.id.etVirgoVenus,
                R.id.etVirgoSaturn,
                R.id.etVirgoHouse
            ),
            R.id.tvVirgoSum
        ),
        ZodiacPlanetData(
            ZodiacSign.LIBRA,
            "Весы ♎",
            arrayOf(
                R.id.etLibraSun,
                R.id.etLibraMoon,
                R.id.etLibraMars,
                R.id.etLibraMercury,
                R.id.etLibraJupiter,
                R.id.etLibraVenus,
                R.id.etLibraSaturn,
                R.id.etLibraHouse
            ),
            R.id.tvLibraSum
        ),
        ZodiacPlanetData(
            ZodiacSign.SCORPIO,
            "Скорпион ♏",
            arrayOf(
                R.id.etScorpioSun,
                R.id.etScorpioMoon,
                R.id.etScorpioMars,
                R.id.etScorpioMercury,
                R.id.etScorpioJupiter,
                R.id.etScorpioVenus,
                R.id.etScorpioSaturn,
                R.id.etScorpioHouse
            ),
            R.id.tvScorpioSum
        ),
        ZodiacPlanetData(
            ZodiacSign.SAGITTARIUS,
            "Стрелец ♐",
            arrayOf(
                R.id.etSagittariusSun,
                R.id.etSagittariusMoon,
                R.id.etSagittariusMars,
                R.id.etSagittariusMercury,
                R.id.etSagittariusJupiter,
                R.id.etSagittariusVenus,
                R.id.etSagittariusSaturn,
                R.id.etSagittariusHouse
            ),
            R.id.tvSagittariusSum
        ),
        ZodiacPlanetData(
            ZodiacSign.CAPRICORN,
            "Козерог ♑",
            arrayOf(
                R.id.etCapricornSun,
                R.id.etCapricornMoon,
                R.id.etCapricornMars,
                R.id.etCapricornMercury,
                R.id.etCapricornJupiter,
                R.id.etCapricornVenus,
                R.id.etCapricornSaturn,
                R.id.etCapricornHouse
            ),
            R.id.tvCapricornSum
        ),
        ZodiacPlanetData(
            ZodiacSign.AQUARIUS,
            "Водолей ♒",
            arrayOf(
                R.id.etAquariusSun,
                R.id.etAquariusMoon,
                R.id.etAquariusMars,
                R.id.etAquariusMercury,
                R.id.etAquariusJupiter,
                R.id.etAquariusVenus,
                R.id.etAquariusSaturn,
                R.id.etAquariusHouse
            ),
            R.id.tvAquariusSum
        ),
        ZodiacPlanetData(
            ZodiacSign.PISCES,
            "Рыбы ♓",
            arrayOf(
                R.id.etPiscesSun,
                R.id.etPiscesMoon,
                R.id.etPiscesMars,
                R.id.etPiscesMercury,
                R.id.etPiscesJupiter,
                R.id.etPiscesVenus,
                R.id.etPiscesSaturn,
                R.id.etPiscesHouse
            ),
            R.id.tvPiscesSum
        )
    )
    private val planetIds = arrayOf(
        Planet.SUN,
        Planet.MOON,
        Planet.MARS,
        Planet.MERCURY,
        Planet.JUPITER,
        Planet.VENUS,
        Planet.SATURN,
        Planet.HOUSE
    )
    private val planetSumTextViewIds = arrayOf(
        R.id.tvSunSum,
        R.id.tvMoonSum,
        R.id.tvMarsSum,
        R.id.tvMercurySum,
        R.id.tvJupiterSum,
        R.id.tvVenusSum,
        R.id.tvSaturnSum
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get userId from intent extras
        userId = intent.getLongExtra("user_id", 1)
        viewModel.setUserId(userId) // Update ViewModel's userId

            // dbHelper больше не нужен
            // dbHelper = DatabaseHelper(this)

            initSumTextViews()
            initEditTextListAndListeners() // Инициализируем EditText и слушатели до загрузки данных
            observeDatabaseChanges() // Наблюдаем за изменениями в БД

            val buttonToPlanetSign = binding.bottomButtonBar.buttonPlanetSignActivity
            buttonToPlanetSign.setOnClickListener {
                saveDataToDb() // Сохраняем данные перед переходом
                val intent = Intent(this, PlanetSignActivity::class.java)
                intent.putExtra("user_id", userId) // Передаем user_id в PlanetSignActivity
                startActivity(intent)
            }

            val buttonToMain = binding.bottomButtonBar.buttonMainActivity
            buttonToMain.setOnClickListener {
                // Уже на MainActivity, ничего не делаем или можно обновить страницу
            }

            val buttonToUser = binding.bottomButtonBar.buttonUserActivity
            buttonToUser.setOnClickListener {
                saveDataToDb() // Сохраняем данные перед переходом
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
            }
        }

        // Наблюдение за Flow из ViewModel
        private fun observeDatabaseChanges() {
            lifecycleScope.launch {
                viewModel.allPositionsFlow.collect { positions ->
                    // Обновляем UI при получении новых данных
                    loadDataIntoUI(positions)
                }
            }
        }

        // Загрузка данных в UI (вызывается из observeDatabaseChanges)
        private fun loadDataIntoUI(allPositions: List<PlanetaryPositionEntity>) {
            val zodiacSums = IntArray(zodiacSumTextViews.size)
            val planetSums = IntArray(planetSumTextViews.size)

            // Создаем Map для быстрого доступа к позициям
            val positionMap = allPositions.associateBy { Pair(it.planetId, it.signId) }

            zodiacDataList.forEachIndexed { signIndex, zodiacData ->
                var currentZodiacSum = 0
                zodiacData.editTextIds.forEachIndexed { planetIndex, editTextId ->
                    val editText = findViewById<EditText>(editTextId)
                    val planetId = planetIds[planetIndex]
                    // Ищем позицию в Map
                    val position = positionMap[Pair(planetId, zodiacData.signId)]

                    var value = 0
                    if (position?.value != null) {
                        // Устанавливаем текст, только если он отличается, чтобы избежать рекурсии TextWatcher
                        if (editText.text.toString() != position.value.toString()) {
                            editText.setText(position.value.toString())
                        }
                        value = position.value!! // Используем !! т.к. проверили на null
                    } else {
                        if (editText.text.isNotEmpty()) {
                            editText.text.clear()
                        }
                    }

                    if (planetId != Planet.HOUSE) {
                        planetSums[planetIndex] += value
                        currentZodiacSum += value
                    }
                }
                zodiacSums[signIndex] = currentZodiacSum
            }

            // Обновляем суммы
            zodiacSumTextViews.forEachIndexed { index, textView ->
                textView.text = zodiacSums[index].toString()
            }
            planetSumTextViews.forEachIndexed { index, textView ->
                textView.text = planetSums[index].toString()
            }
        }


        private fun initEditTextListAndListeners() {
            editTextList = zodiacDataList.flatMap { zodiacData ->
                zodiacData.editTextIds.map { findViewById<EditText>(it) }
            }

            editTextList.forEachIndexed { index, editText ->
                // УДАЛИТЕ старый TextWatcher, который вызывает recalculateSums()
                // editText.addTextChangedListener(object : TextWatcher { ... })

                // Добавьте новый TextWatcher, который сохраняет данные при изменении (с задержкой)
                val debounceHandler = Handler(Looper.getMainLooper())
                var debounceRunnable: Runnable? = null

                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // Отменяем предыдущий запланированный вызов сохранения
                        debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
                    }

                    override fun afterTextChanged(editable: Editable?) {
                        // Планируем сохранение через 500 мс после последнего изменения
                        debounceRunnable = Runnable { saveDataToDb() }
                        debounceHandler.postDelayed(debounceRunnable!!, 500) // 500ms debounce

                        // Логика перехода фокуса остается
                        val planetIndex = index % 8 // 0=Sun, 1=Moon, ..., 7=House
                        val isHouseField = (planetIndex == 7) // Planet.HOUSE

                        if (!isHouseField) { // Для всех кроме House
                            if (editable?.length == 1) {
                                focusNextEditText(index)
                            }
                        } else { // Для House
                            if (editable?.length == 2) { // Переход после 2 символов для House
                                focusNextEditText(index)
                            }
                        }
                    }
                })

                // Остальные слушатели фокуса и клика остаются
                editText.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        val et = view as EditText
                        Handler(Looper.getMainLooper()).postDelayed({
                            et.selectAll()
                        }, 10) // Небольшая задержка для надежности
                    }
                }
                editText.setOnClickListener {
                    val et = it as EditText
                    et.requestFocus()
                    et.selectAll()
                }
            }
        }

        // Вспомогательная функция для перехода фокуса
        private fun focusNextEditText(currentIndex: Int) {
            val nextIndex = currentIndex + 8 // Переход к той же планете в следующем знаке
            if (nextIndex < editTextList.size) {
                editTextList[nextIndex].requestFocus()
            } else {
                // Если это последний столбец, можно перейти к первому EditText следующей строки (если нужно)
                // val nextRowIndex = currentIndex - (currentIndex % 8) + 8 // Начало следующей строки
                // if (nextRowIndex < editTextList.size) {
                //     editTextList[nextRowIndex].requestFocus()
                // }
                // Или просто убрать фокус
                currentFocus?.clearFocus()
            }
        }


        private fun initSumTextViews() {
            zodiacSumTextViews =
                zodiacDataList.map { findViewById<TextView>(it.sumTextViewId) }.toTypedArray()
            planetSumTextViews =
                planetSumTextViewIds.map { findViewById<TextView>(it) }.toTypedArray()
        }

        // Удален recalculateSums(), так как суммы обновляются в loadDataIntoUI

        // Удален loadDataFromDb(), заменен на observeDatabaseChanges и loadDataIntoUI

        // Сохранение данных из UI в БД через ViewModel
        private fun saveDataToDb() {
            val dataToSave = mutableListOf<Triple<Int, Int, Int?>>()
            var allDataValid = true

            zodiacDataList.forEach { zodiacData ->
                zodiacData.editTextIds.forEachIndexed { planetIndex, editTextId ->
                    val editText = findViewById<EditText>(editTextId)
                    val valueStr = editText.text.toString()
                    val planetId = planetIds[planetIndex]
                    val signId = zodiacData.signId

                    val value: Int? = if (valueStr.isNotEmpty()) {
                        try {
                            valueStr.toInt()
                        } catch (e: NumberFormatException) {
                            // Можно показать ошибку, но не будем прерывать сохранение других полей
                            // editText.error = getString(R.string.input_number_error)
                            allDataValid = false // Помечаем, что есть невалидные данные
                            null // Сохраняем null, если ввод некорректен
                        }
                    } else {
                        null // Пустое поле сохраняем как null
                    }
                    dataToSave.add(Triple(planetId, signId, value))
                }
            }

            // Вызываем метод сохранения в ViewModel
            viewModel.saveData(dataToSave)

            // Пересчет сумм теперь происходит автоматически через Flow/Collect в loadDataIntoUI
        }

    }

