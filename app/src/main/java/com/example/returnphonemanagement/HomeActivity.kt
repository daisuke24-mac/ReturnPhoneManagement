package com.example.returnphonemanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 保存データの読み込み
        val sharedPref = getSharedPreferences("phone_data", Context.MODE_PRIVATE)
        val model = sharedPref.getString("model", "未登録") ?: "未登録"
        val returnDate = sharedPref.getString("return_date", "") ?: ""

        // UI要素の取得
        val modelTextView = findViewById<TextView>(R.id.modelTextView)
        val returnDateTextView = findViewById<TextView>(R.id.returnDateTextView)
        val remainingTimeTextView = findViewById<TextView>(R.id.remainingTimeTextView)
        val newButton = findViewById<Button>(R.id.newButton)

        // データ表示
        modelTextView.text = "機種名: $model"
        returnDateTextView.text = "返却月: $returnDate"

        // 残り時間の計算
        if (returnDate.isNotEmpty()) {
            val remaining = calculateRemainingTime(returnDate)
            remainingTimeTextView.text = "残り: ${remaining.first}年${remaining.second}ヶ月"
        }

        // 新規作成ボタン
        newButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun calculateRemainingTime(returnDate: String): Pair<Int, Int> {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val returnCalendar = Calendar.getInstance().apply {
            time = sdf.parse(returnDate) ?: Date()
        }

        val currentCalendar = Calendar.getInstance()

        val diffYear = returnCalendar.get(Calendar.YEAR) - currentCalendar.get(Calendar.YEAR)
        var diffMonth = returnCalendar.get(Calendar.MONTH) - currentCalendar.get(Calendar.MONTH)

        // 月の調整
        if (diffMonth < 0) {
            diffMonth += 12
        }

        return diffYear to diffMonth
    }
}