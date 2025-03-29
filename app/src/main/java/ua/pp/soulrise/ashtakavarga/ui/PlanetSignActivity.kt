package ua.pp.soulrise.ashtakavarga.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import ua.pp.soulrise.ashtakavarga.data.AppDatabase
import ua.pp.soulrise.ashtakavarga.data.AstrologyDao
import ua.pp.soulrise.ashtakavarga.data.PlanetSignSelectionEntity
import ua.pp.soulrise.ashtakavarga.data.TransitEntity
import ua.pp.soulrise.ashtakavarga.common.Planet
import ua.pp.soulrise.ashtakavarga.common.ZodiacSign
import ua.pp.soulrise.ashtakavarga.R
import ua.pp.soulrise.ashtakavarga.common.QualityLevel
import ua.pp.soulrise.ashtakavarga.common.curQuality
import ua.pp.soulrise.ashtakavarga.common.modQuality


// --- ViewModel для PlanetSignActivity (Лучше вынести в отдельный файл PlanetSignViewModel.kt) ---
class PlanetSignViewModel(private val dao: AstrologyDao, private val userId: Long) : ViewModel() {

    // Получение данных по запросу
    suspend fun getPlanetaryPosition(planetId: Int, signId: Int): Int? {
        return dao.getPlanetaryPosition(planetId, signId, userId = userId)?.value
    }

    suspend fun getPlanetSignSelection(planetId: Int): Int? {
        return dao.getPlanetSignSelection(planetId, userId = userId)?.signId
    }

    suspend fun getTransitSelection(planetId: Int): Int? {
        return dao.getTransit(planetId, userId = userId)?.signId
    }

    // Сохранение выбора
    fun savePlanetSignSelection(planetId: Int, signId: Int) {
        viewModelScope.launch {
            dao.savePlanetSignSelection(PlanetSignSelectionEntity(planetId = planetId, signId = signId, userId = userId))
        }
    }

    fun saveTransitSelection(planetId: Int, signId: Int) {
        viewModelScope.launch {
            dao.saveTransit(TransitEntity(planetId = planetId, signId = signId, userId = userId))
        }
    }

    // Фабрика
    class PlanetSignViewModelFactory(private val dao: AstrologyDao, private val userId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlanetSignViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlanetSignViewModel(dao, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


class PlanetSignActivity : AppCompatActivity() {

    // ViewModel
    private var userId: Long = 1 // Default value
    private val viewModel: PlanetSignViewModel by viewModels {
        PlanetSignViewModel.PlanetSignViewModelFactory(AppDatabase.getDatabase(applicationContext).astrologyDao(), userId)
    }

    // Списки ID и имен остаются
    private val planetNames = listOf("Солнце","Луна","Марс","Меркурий","Юпитер","Венера","Сатурн")
    private val planetIds = listOf(Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN)
    private val zodiacSigns = listOf("Овен♈", "Телец♉", "Близнецы♊", "Рак♋", "Лев♌", "Дева♍", "Весы♎", "Скорпион♏", "Стрелец♐", "Козерог♑", "Водолей♒", "Рыбы♓")
    private val zodiacSignIds = listOf(ZodiacSign.ARIES, ZodiacSign.TAURUS, ZodiacSign.GEMINI, ZodiacSign.CANCER, ZodiacSign.LEO, ZodiacSign.VIRGO, ZodiacSign.LIBRA, ZodiacSign.SCORPIO, ZodiacSign.SAGITTARIUS, ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS, ZodiacSign.PISCES)

    // Списки View остаются
    private lateinit var spinners: List<Spinner>
    private lateinit var valueTextViews: List<TextView>
    private lateinit var qualityTextViews: List<TextView>
    private lateinit var homeTextViews: List<TextView>
    private lateinit var transitSpinners: List<Spinner>
    private lateinit var transitValueTextViews: List<TextView>
    private lateinit var transitQualityTextViews: List<TextView>
    private lateinit var transitComparisonTextViews: List<TextView>
    private lateinit var barChartPlanet: BarChart
    private lateinit var barChartTransit: BarChart

    // Флаги для предотвращения лишних вызовов onItemSelected при начальной загрузке
    private var planetSpinnersInitialized = false
    private var transitSpinnersInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planet_sign) // Убедитесь, что используете правильный layout

        // Get userId from intent extras
        userId = intent.getLongExtra("user_id", 1)

        initializeViews() // Инициализация всех View

        setupSpinners() // Настройка спиннеров (включая адаптеры и слушатели)

        loadInitialSelections() // Загрузка сохраненных позиций и установка их в спиннеры

        // Кнопки навигации
        findViewById<Button>(R.id.button_planet_sign_activity).setOnClickListener {
            // Уже здесь, можно добавить обновление данных, если нужно
            // loadInitialSelections() // Перезагрузить данные?
        }
        findViewById<Button>(R.id.button_main_activity).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // Чтобы не создавать новую MainActivity
            startActivity(intent)
            // finish() // Не закрываем, если хотим иметь возможность вернуться назад
        }

        findViewById<Button>(R.id.button_user_activity).setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // Чтобы не создавать новую
            startActivity(intent)
            // finish() // Не закрываем, если хотим иметь возможность вернуться назад
        }
    }

    private fun initializeViews() {
        spinners = listOf(
            findViewById(R.id.spinnerSun), findViewById(R.id.spinnerMoon), findViewById(R.id.spinnerMars),
            findViewById(R.id.spinnerMercury), findViewById(R.id.spinnerJupiter), findViewById(R.id.spinnerVenus),
            findViewById(R.id.spinnerSaturn)
        )
        valueTextViews = listOf(
            findViewById(R.id.tvSunValue), findViewById(R.id.tvMoonValue), findViewById(R.id.tvMarsValue),
            findViewById(R.id.tvMercuryValue), findViewById(R.id.tvJupiterValue), findViewById(R.id.tvVenusValue),
            findViewById(R.id.tvSaturnValue)
        )
        qualityTextViews = listOf(
            findViewById(R.id.tvSunQuality), findViewById(R.id.tvMoonQuality), findViewById(R.id.tvMarsQuality),
            findViewById(R.id.tvMercuryQuality), findViewById(R.id.tvJupiterQuality), findViewById(R.id.tvVenusQuality),
            findViewById(R.id.tvSaturnQuality)
        )
        homeTextViews = listOf(
            findViewById(R.id.tvSunHome), findViewById(R.id.tvMoonHome), findViewById(R.id.tvMarsHome),
            findViewById(R.id.tvMercuryHome), findViewById(R.id.tvJupiterHome), findViewById(R.id.tvVenusHome),
            findViewById(R.id.tvSaturnHome)
        )
        transitSpinners = listOf(
            findViewById(R.id.spinnerTransitSun), findViewById(R.id.spinnerTransitMoon), findViewById(R.id.spinnerTransitMars),
            findViewById(R.id.spinnerTransitMercury), findViewById(R.id.spinnerTransitJupiter), findViewById(R.id.spinnerTransitVenus),
            findViewById(R.id.spinnerTransitSaturn)
        )
        transitValueTextViews = listOf(
            findViewById(R.id.tvTransitSunValue), findViewById(R.id.tvTransitMoonValue), findViewById(R.id.tvTransitMarsValue),
            findViewById(R.id.tvTransitMercuryValue), findViewById(R.id.tvTransitJupiterValue), findViewById(R.id.tvTransitVenusValue),
            findViewById(R.id.tvTransitSaturnValue)
        )
        transitQualityTextViews = listOf(
            findViewById(R.id.tvTransitSunQuality), findViewById(R.id.tvTransitMoonQuality), findViewById(R.id.tvTransitMarsQuality),
            findViewById(R.id.tvTransitMercuryQuality), findViewById(R.id.tvTransitJupiterQuality), findViewById(R.id.tvTransitVenusQuality),
            findViewById(R.id.tvTransitSaturnQuality)
        )
        transitComparisonTextViews = listOf(
            findViewById(R.id.tvTransitSunComparison), findViewById(R.id.tvTransitMoonComparison), findViewById(R.id.tvTransitMarsComparison),
            findViewById(R.id.tvTransitMercuryComparison), findViewById(R.id.tvTransitJupiterComparison), findViewById(R.id.tvTransitVenusComparison),
            findViewById(R.id.tvTransitSaturnComparison)
        )
        barChartPlanet = findViewById(R.id.barChartPlanet)
        barChartTransit = findViewById(R.id.barChartTransit)
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, zodiacSigns)

        // Настройка натальных спиннеров
        spinners.forEachIndexed { index, spinner ->
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Выполняем только после начальной загрузки
                    if (planetSpinnersInitialized) {
                        val selectedZodiacSignId = zodiacSignIds[position]
                        val planetId = planetIds[index]
                        updatePlanetData(index, planetId, selectedZodiacSignId)
                        viewModel.savePlanetSignSelection(planetId, selectedZodiacSignId) // Сохраняем выбор
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Настройка транзитных спиннеров
        transitSpinners.forEachIndexed { index, spinner ->
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Выполняем только после начальной загрузки
                    if (transitSpinnersInitialized) {
                        val selectedZodiacSignId = zodiacSignIds[position]
                        val planetId = planetIds[index]
                        updateTransitData(index, planetId, selectedZodiacSignId)
                        viewModel.saveTransitSelection(planetId, selectedZodiacSignId) // Сохраняем выбор
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    // Загрузка начальных выбранных значений для спиннеров
    private fun loadInitialSelections() {
        // Сбрасываем флаги перед загрузкой
        planetSpinnersInitialized = false
        transitSpinnersInitialized = false

        lifecycleScope.launch {
            // Загрузка натальных позиций
            var planetSelectionsLoaded = 0
            planetIds.forEachIndexed { index, planetId ->
                val savedSignId = viewModel.getPlanetSignSelection(planetId)
                val signIndex = savedSignId?.let { zodiacSignIds.indexOf(it) } ?: -1 // Находим индекс или -1
                if (signIndex != -1) {
                    spinners[index].setSelection(signIndex, false) // Устанавливаем без вызова onItemSelected
                } else {
                    // Если нет сохраненного значения, можно установить значение по умолчанию (например, первый знак)
                    spinners[index].setSelection(0, false)
                }

                // Обновляем данные для этой планеты после установки спиннера
                val currentSignId = zodiacSignIds[spinners[index].selectedItemPosition]
                updatePlanetData(index, planetId, currentSignId)

                planetSelectionsLoaded++
                if (planetSelectionsLoaded == planetIds.size) {
                    planetSpinnersInitialized = true // Все натальные спиннеры инициализированы
                    updateBarChartPlanet() // Обновляем график после загрузки всех данных
                }
            }

            // Загрузка транзитных позиций
            var transitSelectionsLoaded = 0
            planetIds.forEachIndexed { index, planetId ->
                val savedSignId = viewModel.getTransitSelection(planetId)
                val signIndex = savedSignId?.let { zodiacSignIds.indexOf(it) } ?: -1
                if (signIndex != -1) {
                    transitSpinners[index].setSelection(signIndex, false) // Устанавливаем без вызова onItemSelected
                } else {
                    transitSpinners[index].setSelection(0, false)
                }

                // Обновляем данные для этого транзита после установки спиннера
                val currentSignId = zodiacSignIds[transitSpinners[index].selectedItemPosition]
                updateTransitData(index, planetId, currentSignId)

                transitSelectionsLoaded++
                if (transitSelectionsLoaded == planetIds.size) {
                    transitSpinnersInitialized = true // Все транзитные спиннеры инициализированы
                    updateBarChartTransit() // Обновляем график после загрузки всех данных
                }
            }
        }
    }

    // Обновление данных для натальной планеты (значение, качество, дом, сравнение)
    private fun updatePlanetData(index: Int, planetId: Int, selectedSignId: Int) {
        lifecycleScope.launch {
            val positionValue = viewModel.getPlanetaryPosition(planetId, selectedSignId)
            val homeValue = viewModel.getPlanetaryPosition(Planet.HOUSE, selectedSignId)

            if (index < 0 || index >= homeTextViews.size) {
                return@launch
            }

            // Обновляем UI для натальной планеты
            updateValueAndQuality(positionValue, valueTextViews[index], qualityTextViews[index], ::determineQualityLevel)
            
            val homeTextView = homeTextViews[index]
            if (homeTextView != null) {
                val displayValue = if (homeValue != null) {
                    homeValue.toString()
                } else {
                    "-"
                }
                homeTextView.text = displayValue
            }


            // Обновляем сравнение с транзитом
            updateComparisonTextView(index)
            // Обновляем натальный график (только если инициализация завершена)
            if (planetSpinnersInitialized) updateBarChartPlanet()
        }
    }

    // Обновление данных для транзитной планеты (значение, качество, сравнение)
    private fun updateTransitData(index: Int, planetId: Int, selectedSignId: Int) {
        lifecycleScope.launch {
            val positionValue = viewModel.getPlanetaryPosition(planetId, selectedSignId)

            // Обновляем UI для транзитной планеты
            updateValueAndQuality(positionValue, transitValueTextViews[index], transitQualityTextViews[index], ::currentQuality)

            // Обновляем сравнение с наталом
            updateComparisonTextView(index)
            // Обновляем транзитный график (только если инициализация завершена)
            if (transitSpinnersInitialized) updateBarChartTransit()
        }
    }

    // Вспомогательная функция для обновления TextView значения и качества
    // Использует дженерик и функцию высшего порядка для определения качества
    private fun <T : Enum<T>> updateValueAndQuality(
        value: Int?,
        valueTextView: TextView,
        qualityTextView: TextView,
        qualityDeterminer: (Int) -> QualityInfoProvider<T> // Функция, возвращающая провайдер информации о качестве
    ) {
        if (value != null) {
            valueTextView.text = value.toString()
            val quality = qualityDeterminer(value) // Получаем информацию о качестве
            qualityTextView.text = quality.description
            qualityTextView.setTextColor(quality.color)
        } else {
            valueTextView.text = "-"
            qualityTextView.text = "-"
            qualityTextView.setTextColor(Color.BLACK) // Стандартный цвет для отсутствия данных
        }
    }

    // Обновление текста сравнения для конкретной планеты
    private fun updateComparisonTextView(planetIndex: Int) {
        val comparisonTextView = transitComparisonTextViews[planetIndex]
        // Получаем текущие значения из TextView (они уже обновлены другими функциями)
        val natalValue = valueTextViews[planetIndex].text.toString().toIntOrNull()
        val transitValue = transitValueTextViews[planetIndex].text.toString().toIntOrNull()

        val comparison = compareTransitWithNatal(transitValue, natalValue) // Вызываем функцию сравнения

        comparisonTextView.text = comparison.description
        comparisonTextView.setTextColor(comparison.color)
    }

    // Логика сравнения (вынесена из старого DatabaseHelper)
    private fun compareTransitWithNatal(transitValue: Int?, natalValue: Int?): modQuality {
        // Если одно из значений null, считаем "без изменений"
        if (transitValue == null || natalValue == null) {
            return modQuality.UNCHANGED
        }
        // Сравниваем числовые значения
        return when {
            transitValue > natalValue -> modQuality.ENHANCEMENT
            transitValue < natalValue -> modQuality.DETERIORATION
            else -> modQuality.UNCHANGED
        }
    }


    // --- Функции определения качества (адаптированы для интерфейса) ---

    // Интерфейс для унификации Enum'ов качества, чтобы использовать их в updateValueAndQuality
    // (Лучше вынести в отдельный файл QualityInfoProvider.kt в пакете util)
    interface QualityInfoProvider<T : Enum<T>> {
        val description: String
        val color: Int
    }

    // Адаптеры для Enum'ов
    // Возвращает объект, реализующий QualityInfoProvider
    private fun determineQualityLevel(value: Int): QualityInfoProvider<QualityLevel> {
        return when (value) {
            0 -> QualityLevel.WORST
            1 -> QualityLevel.VERY_BAD
            2 -> QualityLevel.WORSE
            3 -> QualityLevel.BAD
            4 -> QualityLevel.AVERAGE
            5 -> QualityLevel.BETTER
            6 -> QualityLevel.EVEN_BETTER
            7 -> QualityLevel.VERY_GOOD
            8, 9 -> QualityLevel.EXCELLENT // Объединено 8 и 9
            else -> if (value < 0) QualityLevel.WORST else QualityLevel.EXCELLENT // Обработка других значений
        }
    }

    private fun currentQuality(value: Int): QualityInfoProvider<curQuality> {
        return when {
            value <= 3 -> curQuality.NEGATIVE
            value == 4 -> curQuality.MEDIUM
            else -> curQuality.POSITIVE // Все что > 4
        }
    }

    // --- Обновление графиков ---

    private fun updateBarChartPlanet() {
        // Собираем данные для графика асинхронно
        lifecycleScope.launch {
            val entries = mutableListOf<BarEntry>()
            val colors = mutableListOf<Int>()

            planetIds.forEachIndexed { index, _ -> // Используем _ т.к. planetId не нужен здесь
                // Получаем значение напрямую из TextView, т.к. оно уже обновлено
                val value = valueTextViews[index].text.toString().toIntOrNull() ?: 0 // 0 если null или не число
                entries.add(BarEntry(index.toFloat(), value.toFloat()))
                val qualityLevel = determineQualityLevel(value) // Определяем качество по значению
                colors.add(qualityLevel.color) // Добавляем цвет
            }

            // Обновляем график в основном потоке
            setupBarChart(barChartPlanet, entries, colors, planetNames, "Натальные значения")
        }
    }

    private fun updateBarChartTransit() {
        lifecycleScope.launch {
            val entries = mutableListOf<BarEntry>()
            val colors = mutableListOf<Int>()

            planetIds.forEachIndexed { index, _ ->
                // Получаем значение из TextView транзита
                val value = transitValueTextViews[index].text.toString().toIntOrNull() ?: 0
                entries.add(BarEntry(index.toFloat(), value.toFloat()))
                // Используем determineQualityLevel для цвета, как в оригинале, для консистентности
                val qualityLevel = determineQualityLevel(value)
                colors.add(qualityLevel.color)
            }
            // Обновляем график транзитов
            setupBarChart(barChartTransit, entries, colors, planetNames, "Транзитные значения")
        }
    }

    // Общая функция настройки графика BarChart
    private fun setupBarChart(
        chart: BarChart,
        entries: List<BarEntry>,
        colors: List<Int>,
        labels: List<String>,
        label: String // Метка для набора данных (не отображается, т.к. легенда выключена)
    ) {
        val dataSet = BarDataSet(entries, label).apply {
            setColors(colors) // Устанавливаем цвета для столбцов
            valueTextColor = Color.BLACK // Цвет текста значений над столбцами
            valueTextSize = 12f // Размер текста значений
            setDrawValues(true) // Отображать значения над столбцами
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f // Ширина столбцов (относительно доступного пространства)

        chart.apply {
            data = barData
            description.isEnabled = false // Отключаем описание графика
            legend.isEnabled = false // Отключаем легенду

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels) // Метки оси X (названия планет)
                position = XAxis.XAxisPosition.BOTTOM // Позиция оси X
                granularity = 1f // Шаг между метками
                isGranularityEnabled = true // Включаем гранулярность
                textColor = Color.BLACK // Цвет текста меток
                setDrawGridLines(false) // Не рисовать вертикальные линии сетки
                setDrawAxisLine(true) // Рисовать линию оси X
            }
            axisLeft.apply {
                axisMinimum = 0f // Минимальное значение оси Y
                axisMaximum = 9f // Максимальное значение оси Y (или можно сделать 9.5f для отступа)
                granularity = 1f // Шаг меток оси Y
                textColor = Color.BLACK // Цвет текста меток
                setDrawGridLines(true) // Рисовать горизонтальные линии сетки
                setDrawAxisLine(true) // Рисовать линию оси Y
            }
            axisRight.isEnabled = false // Отключаем правую ось Y

            setFitBars(true) // Пытаемся уместить все столбцы
            setDrawGridBackground(false) // Не рисовать фон сетки
            setDrawBorders(false) // Не рисовать границы графика
            setScaleEnabled(false) // Отключаем масштабирование
            setPinchZoom(false) // Отключаем масштабирование щипком
            isDoubleTapToZoomEnabled = false // Отключаем зум двойным тапом

            invalidate() // Перерисовываем график
        }
    }
}
