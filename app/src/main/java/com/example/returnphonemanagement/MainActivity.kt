package com.example.returnphonemanagement

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // 保存用のデータクラスを定義
    data class PhoneData(
        val model: String,
        val returnDate: String
    )

    private lateinit var manufacturerSpinner: Spinner
    private lateinit var modelSpinner: Spinner
    private lateinit var modelEditText: EditText
    private lateinit var purchaseDateEditText: EditText
    private lateinit var returnPeriodSpinner: Spinner
    private lateinit var calculateButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var registerButton: Button

    // メーカーと機種のデータ
    private val manufacturerList = listOf("Google", "Samsung", "Sony", "SHARP", "Xiaomi", "その他")
    private val modelMap = mapOf(
        "Google" to listOf(
            "Pixel 7 Pro",
            "Pixel 7",
            "Pixel 7a",
            "Pixel 8 Pro",
            "Pixel 8",
            "Pixel 8a",
            "Pixel 9 Pro XL",
            "Pixel 9 Pro",
            "Pixel 9",
            "Pixel 9a",
            "その他"
        ),
        "Samsung" to listOf(
            "Galaxy S24 Ultra",
            "Galaxy S24",
            "Galaxy S24 FE",
            "Galaxy S25 Ultra",
            "Galaxy S25",
            "Galaxy A25 5G",
            "Galaxy Z Fold5",
            "Galaxy Z Flip5",
            "Galaxy Z Fold6",
            "Galaxy Z Flip6",
            "その他"
        ),
        "Sony" to listOf(
            "Xperia 1 VII",
            "Xperia 10 VII",
            "Xperia 1 VI",
            "Xperia 10 VI",
            "その他"
        ),
        "SHARP" to listOf(
            "AQUOS sense9",
            "AQUOS sense8",
            "AQUOS wish3",
            "BASIO active3",
            "その他"
        ),
        "Xiaomi" to listOf(
            "Xiaomi 14T",
            "Redmi Note 13 Pro 5G",
            "Xiaomi 13T",
            "Redmi 12 5G",
            "その他"
        ),
    )

    private var selectedYear = 2024
    private var selectedMonth = 0 // 0-indexed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 登録済みデータがあればHomeActivityへ遷移
        val sharedPref = getSharedPreferences("phone_data", Context.MODE_PRIVATE)
        if (sharedPref.contains("model") && sharedPref.contains("return_date")) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        manufacturerSpinner = findViewById(R.id.manufacturerSpinner)
        modelSpinner = findViewById(R.id.modelSpinner)
        modelEditText = findViewById(R.id.modelEditText)
        purchaseDateEditText = findViewById(R.id.purchaseDateEditText)
        returnPeriodSpinner = findViewById(R.id.returnPeriodSpinner)
        calculateButton = findViewById(R.id.calculateButton)
        resultTextView = findViewById(R.id.resultTextView)
        registerButton = findViewById(R.id.registerButton)

        // メーカーSpinnerのセットアップ
        val manufacturerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, manufacturerList)
        manufacturerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        manufacturerSpinner.adapter = manufacturerAdapter

        // 最初のメーカーに対応した機種リストをセット
        updateModelSpinner(manufacturerList[0])

        // メーカー選択時に機種リストを切り替え
        manufacturerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedManufacturer = manufacturerList[position]
                if (selectedManufacturer == "その他") {
                    // モデルSpinnerを非表示、EditTextを表示
                    modelSpinner.visibility = View.GONE
                    modelEditText.visibility = View.VISIBLE
                } else {
                    // モデルSpinnerを表示、EditTextを非表示
                    modelSpinner.visibility = View.VISIBLE
                    modelEditText.visibility = View.GONE
                    updateModelSpinner(selectedManufacturer)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                updateModelSpinner(manufacturerList.first())
            }
        }

        // 契約年数Spinnerの設定
        val periods = arrayOf("2年", "1年")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        returnPeriodSpinner.adapter = adapter

        // 日付選択ダイアログ
        purchaseDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = DatePickerDialog(
                this,
                { _, year, month, _ ->
                    selectedYear = year
                    selectedMonth = month
                    purchaseDateEditText.setText(String.format("%04d-%02d", year, month + 1))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }

        calculateButton.setOnClickListener {
            val purchaseDate = purchaseDateEditText.text.toString()
            val period = returnPeriodSpinner.selectedItem.toString()

            if (purchaseDate.isNotEmpty()) {
                val returnDate = calculateReturnDate(purchaseDate, period)
                resultTextView.text = "返却月: $returnDate"
            } else {
                Toast.makeText(this, "購入年月を入力してください", Toast.LENGTH_SHORT).show()
            }
        }

        // 機種名と返却月のデータを保存
        registerButton.setOnClickListener {
            // 機種名の取得（SpinnerまたはEditTextから）
            val model = if (modelEditText.visibility == View.VISIBLE) {
                modelEditText.text.toString()
            } else {
                modelSpinner.selectedItem.toString()
            }

            // 返却月の取得
            val returnDate = resultTextView.text.toString().replace("返却月: ", "")

            // 機種名・返却月が空白でないことの確認と警告
            if (model.isBlank()) {
                Toast.makeText(this, "機種名を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (returnDate.isBlank()) {
                Toast.makeText(this, "返却月を計算してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // データ保存
            val sharedPref = getSharedPreferences("phone_data", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("model", model)
                putString("return_date", returnDate)
                apply()
            }
            Toast.makeText(this, "登録しました", Toast.LENGTH_SHORT).show()

            // ホーム画面に遷移
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // 機種Spinnerの内容を更新
    private fun updateModelSpinner(manufacturer: String) {
        val models = modelMap[manufacturer] ?: emptyList()
        val modelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, models)
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = modelAdapter
    }

    private fun calculateReturnDate(purchaseDate: String, period: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdf.parse(purchaseDate)
        val calendar = Calendar.getInstance()
        calendar.time = date ?: Date()
        calendar.add(Calendar.MONTH, if (period == "2年") 24 else 12)
        return sdf.format(calendar.time)
    }
}