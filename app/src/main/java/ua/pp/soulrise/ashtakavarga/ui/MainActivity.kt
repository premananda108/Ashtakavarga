package ua.pp.soulrise.ashtakavarga.ui

import ua.pp.soulrise.ashtakavarga.databinding.ActivityMainBinding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels // Для viewModels делегата
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope // Для viewModelScope
import kotlinx.coroutines.Job
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
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
    private val userIdLiveData = MutableLiveData<Long>(1)

    // LiveData данных, который переключается при изменении userId
    val allPositionsLiveData: LiveData<List<PlanetaryPositionEntity>> = userIdLiveData.switchMap { userId ->
        dao.getAllPlanetaryPositionsLiveData(userId)
    }

    fun setUserId(newUserId: Long) {
        android.util.Log.d("MainViewModel", "Обновление userId в ViewModel на: $newUserId")
        userIdLiveData.value = newUserId // Обновляем userId, что вызовет переключение LiveData
    }

    // Функция для сохранения данных

    fun saveData(dataToSave: List<Triple<Int, Int, Int?>>): Job {
        return viewModelScope.launch {
            dataToSave.forEach { (planetId, signId, value) ->
                dao.upsertPlanetaryPosition(planetId, signId, userId = userIdLiveData.value ?: 1L, value = value) // Используем актуальный userId из LiveData
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private var userId: Long = 1 // Default value
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory(
            AppDatabase.getDatabase(applicationContext).astrologyDao()
        )
    }

    private lateinit var zodiacSumTextViews: Array<TextView>
    private lateinit var planetSumTextViews: Array<TextView>
    private lateinit var editTextList: List<EditText>
    private var isSaving = false // Флаг для контроля состояния сохранения

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
        
        // Настройка тулбара
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        //supportActionBar?.title = "Аштакаварга"

        // Get userId from intent extras
        handleIntent(intent)


        // Инициализируем компоненты UI
        initSumTextViews()
        initEditTextListAndListeners()
        
        // Настраиваем наблюдение за данными и устанавливаем userId
        observeDatabaseChanges()
        viewModel.setUserId(userId.toLong())

            val buttonToPlanetSign = binding.bottomButtonBar.buttonPlanetSignActivity
            buttonToPlanetSign.setOnClickListener {
                lifecycleScope.launch {
                    val saveJob = saveDataToDb() // Сохраняем данные перед переходом
                    saveJob.join() // Ждем завершения сохранения
                    val intent = Intent(this@MainActivity, PlanetSignActivity::class.java)
                    intent.putExtra("user_id", userId) // Передаем user_id в PlanetSignActivity
                    startActivity(intent)
                }
            }

            val buttonToMain = binding.bottomButtonBar.buttonMainActivity
            buttonToMain.setOnClickListener {
                // Уже на MainActivity, ничего не делаем или можно обновить страницу
            }

            val buttonToUser = binding.bottomButtonBar.buttonUserActivity
            buttonToUser.setOnClickListener {
                lifecycleScope.launch {
                    val saveJob = saveDataToDb() // Сохраняем данные перед переходом
                    saveJob.join() // Ждем завершения сохранения
                    val intent = Intent(this@MainActivity, UserActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }
        }

        // Наблюдение за LiveData из ViewModel
        private fun observeDatabaseChanges() {
            viewModel.allPositionsLiveData.observe(this) { positions ->
                android.util.Log.d("MainActivity", "Получены новые данные из LiveData для userId: $userId")
                
                // Очищаем все поля перед загрузкой новых данных
                zodiacDataList.forEach { zodiacData ->
                    zodiacData.editTextIds.forEach { editTextId ->
                        findViewById<EditText>(editTextId).text.clear()
                    }
                }
                
                // Обновляем UI при получении новых данных
                loadDataIntoUI(positions)
            }
        }

        // Загрузка данных в UI (вызывается из observeDatabaseChanges)
        private fun loadDataIntoUI(allPositions: List<PlanetaryPositionEntity>) {
            android.util.Log.d("MainActivity", "Загрузка данных в UI для userId: $userId, количество позиций: ${allPositions.size}")
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
                            android.util.Log.d("MainActivity", "Обновление поля для userId: $userId, planetId: ${planetIds[planetIndex]}, signId: ${zodiacData.signId}, новое значение: ${position.value}")
                            editText.setText(position.value.toString())
                        }
                        value = position.value!! // Используем !! т.к. проверили на null
                    } else {
                        // Очищаем поле, если значение в базе null
                        if (editText.text.isNotEmpty()) {
                            android.util.Log.d("MainActivity", "Очистка поля для userId: $userId, planetId: ${planetIds[planetIndex]}, signId: ${zodiacData.signId}")
                            editText.text.clear()
                        }
                        value = 0 // При отсутствии значения используем 0 для суммирования
                    }

                    if (planetId != Planet.HOUSE) {
                        planetSums[planetIndex] += value
                        currentZodiacSum += value
                    }
                }
                zodiacSums[signIndex] = currentZodiacSum
            }

            
        }


        private fun initEditTextListAndListeners() {
            editTextList = zodiacDataList.flatMap { zodiacData ->
                zodiacData.editTextIds.map { findViewById<EditText>(it) }
            }

            editTextList.forEachIndexed { index, editText ->
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
                        val input = editable?.toString() ?: ""
                        val filteredInput = input.filter { it.isDigit() } // Фильтруем нецифровые символы
                        if (input != filteredInput) { // Если ввод был изменен фильтрацией
                            editText.setText(filteredInput)
                            editText.setSelection(filteredInput.length) // Возвращаем курсор в конец
                            // Не используем return здесь, чтобы продолжить обработку валидного ввода
                        }
                   
                        // Логика перехода фокуса - отключена при сохранении
                        if (!isSaving) {
                            val planetIndex = index % 8
                            val isHouseField = (planetIndex == 7)
                        
                            if (!isHouseField && filteredInput.length == 1) { // Для всех кроме House
                                focusNextEditText(index)
                            } else if (isHouseField && filteredInput.length == 2) { // Для House
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
                        recalculateSums() // Обновляем суммы при переходе фокуса
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
            }
        }


        private fun initSumTextViews() {
            zodiacSumTextViews =
                zodiacDataList.map { findViewById<TextView>(it.sumTextViewId) }.toTypedArray()
            planetSumTextViews =
                planetSumTextViewIds.map { findViewById<TextView>(it) }.toTypedArray()
        }

        private fun recalculateSums() {
            val zodiacSums = IntArray(zodiacSumTextViews.size)
            val planetSums = IntArray(planetSumTextViews.size)

            zodiacDataList.forEachIndexed { signIndex, zodiacData ->
                var currentZodiacSum = 0
                zodiacData.editTextIds.forEachIndexed { planetIndex, editTextId ->
                    val editText = findViewById<EditText>(editTextId)
                    val planetId = planetIds[planetIndex]
                    val value = editText.text.toString().toIntOrNull() ?: 0

                    if (planetId != Planet.HOUSE) {
                        planetSums[planetIndex] += value
                        currentZodiacSum += value
                    }
                }
                zodiacSums[signIndex] = currentZodiacSum
            }

            // Обновляем текстовые поля с суммами
            zodiacSums.forEachIndexed { index, sum ->
                zodiacSumTextViews[index].text = sum.toString()
            }

            planetSums.forEachIndexed { index, sum ->
                planetSumTextViews[index].text = sum.toString()
            }
        }

        // Сохранение данных из UI в БД через ViewModel
        private fun saveDataToDb(): Job {
            isSaving = true
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

            // Вызываем метод сохранения в ViewModel и возвращаем Job
            return viewModel.saveData(dataToSave).also {
                it.invokeOnCompletion { isSaving = false }
            }
        }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val newUserId = intent.getLongExtra("user_id", userId)
        if (newUserId != userId) {
            userId = newUserId
            android.util.Log.d("MainActivity", "Получен новый userId из Intent: $userId")
            viewModel.setUserId(userId)
        }
    }


}

