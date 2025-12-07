package com.example.calc_pr5

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var inputLength: EditText
    private lateinit var inputCount: EditText
    private lateinit var inputPmax: EditText
    private lateinit var inputTmax: EditText
    private lateinit var inputCostA: EditText
    private lateinit var inputCostP: EditText
    private lateinit var textResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        scrollView.isFillViewport = true
        scrollView.setBackgroundColor(Color.WHITE)

        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setPadding(40, 40, 40, 40)
        scrollView.addView(mainLayout)

        val title = TextView(this)
        title.text = "Калькулятор Надійності ЕПС"
        title.textSize = 22f
        title.typeface = Typeface.DEFAULT_BOLD
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.BLACK)
        title.setPadding(0, 0, 0, 40)
        mainLayout.addView(title)

        inputLength = createInput(mainLayout, "Довжина лінії (км)")
        inputCount = createInput(mainLayout, "Кількість приєднань (шт)")
        inputCount.inputType = InputType.TYPE_CLASS_NUMBER // Тільки цілі числа

        inputPmax = createInput(mainLayout, "Макс. навантаження (кВт)")
        inputTmax = createInput(mainLayout, "Час роботи Tmax (год)")
        inputCostA = createInput(mainLayout, "Вартість аварійна (грн)")
        inputCostP = createInput(mainLayout, "Вартість планова (грн)")

        val btnAutofill = Button(this)
        btnAutofill.text = "Автозаповнення (Варіант 5)"
        btnAutofill.setBackgroundColor(Color.parseColor("#03DAC5")) // Бірюзовий
        btnAutofill.setTextColor(Color.WHITE)
        val paramsFill = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 140
        )
        paramsFill.setMargins(0, 30, 0, 20)
        btnAutofill.layoutParams = paramsFill
        mainLayout.addView(btnAutofill)

        val btnCalc = Button(this)
        btnCalc.text = "Розрахувати показники"
        btnCalc.setBackgroundColor(Color.parseColor("#6200EE"))
        btnCalc.setTextColor(Color.WHITE)
        val paramsCalc = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 140
        )
        paramsCalc.setMargins(0, 0, 0, 40)
        btnCalc.layoutParams = paramsCalc
        mainLayout.addView(btnCalc)

        textResult = TextView(this)
        textResult.textSize = 16f
        textResult.setTextColor(Color.BLACK)
        textResult.setBackgroundColor(Color.parseColor("#F5F5F5")) // Сірий фон
        textResult.setPadding(30, 30, 30, 30)
        mainLayout.addView(textResult)

        setContentView(scrollView)

        btnAutofill.setOnClickListener {
            inputLength.setText("12.0")
            inputCount.setText("8")
            inputPmax.setText("5500")
            inputTmax.setText("5200")
            inputCostA.setText("24.5")
            inputCostP.setText("18.0")
        }

        btnCalc.setOnClickListener {
            calculate()
        }
    }

    private fun createInput(parent: LinearLayout, hintText: String): EditText {
        val label = TextView(this)
        label.text = hintText
        label.setTextColor(Color.DKGRAY)
        label.textSize = 14f
        parent.addView(label)

        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.setTextColor(Color.BLACK)
        parent.addView(editText)

        val spacer = TextView(this)
        spacer.height = 20
        parent.addView(spacer)

        return editText
    }

    private fun calculate() {
        try {
            val len = inputLength.text.toString().toDouble()
            val num = inputCount.text.toString().toInt()
            val p = inputPmax.text.toString().toDouble()
            val t = inputTmax.text.toString().toDouble()
            val costA = inputCostA.text.toString().toDouble()
            val costP = inputCostP.text.toString().toDouble()

            val w_pl = 0.007; val w_trans = 0.015; val w_breaker = 0.01
            val w_small = 0.02; val w_line_conn = 0.03; val w_sect = 0.02

            val tv_pl = 10.0; val tv_trans = 100.0; val tv_breaker = 30.0
            val tv_small = 15.0; val tv_conn = 2.0
            val tp_max = 43.0

            val w_oc = w_breaker + (w_pl * len) + w_trans + w_small + (w_line_conn * num)

            val t_sum = (w_breaker * tv_breaker) + (w_pl * len * tv_pl) +
                    (w_trans * tv_trans) + (w_small * tv_small) +
                    (w_line_conn * num * tv_conn)
            val tv_oc = t_sum / w_oc

            val ka_oc = (w_oc * tv_oc) / 8760.0
            val kp_oc = (1.2 * tp_max) / 8760.0

            val w_dk = 2 * w_oc * (ka_oc + kp_oc)
            val w_dc = w_dk + w_sect

            val lossA = ka_oc * p * t * costA
            val lossP = kp_oc * p * t * costP
            val total = lossA + lossP

            val res = String.format(Locale.US,
                "РЕЗУЛЬТАТИ:\n\n" +
                        "Одноколова система:\n" +
                        "Частота відмов: %.4f рік⁻¹\n" +
                        "Час відновлення: %.2f год\n" +
                        "Коеф. аварійний (ka): %.5f\n" +
                        "Коеф. плановий (kp): %.5f\n\n" +
                        "Двоколова система:\n" +
                        "Частота відмов (повна): %.4f рік⁻¹\n\n" +
                        "ЗБИТКИ:\n" +
                        "Аварійні: %.2f грн\n" +
                        "Планові: %.2f грн\n" +
                        "СУМА: %.2f грн",
                w_oc, tv_oc, ka_oc, kp_oc, w_dc, lossA, lossP, total
            )

            textResult.text = res

        } catch (e: Exception) {
            Toast.makeText(this, "Помилка! Перевірте дані.", Toast.LENGTH_SHORT).show()
        }
    }
}