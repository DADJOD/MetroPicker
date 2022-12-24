package com.example.metropicker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_MAIN
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var mSelectedStation: TextView? = null
    private var mBL: TextView? = null
    private lateinit var mPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // тоаст уведомляющий об интента - ACTION_MAIN
        Toast.makeText(this, ACTION_MAIN, Toast.LENGTH_SHORT).show()

        // создание переменной tv (text view) и наполнение им станциями
        mSelectedStation = findViewById(R.id.selectedStation)

        // получение данных
        mPrefs = getSharedPreferences(PREFS, MODE_PRIVATE)

        mSelectedStation!!.text = mPrefs.getString(
            ListViewActivity.EXTRA_SELECTED_STATION,
            NOTHING_SELECTED /** ничего странного в этом нет, потому что при
                                отсутствии данных выдаст NOTHING_SELECTED в tv **/
        )

        // серьезно?
        mBL = findViewById(R.id.textBattery)
        mBL!!.text = getBatteryPercentage().toString()
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun onClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(ACTION_PICK_STATION)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CODE_SELECT_STATION)
        } else {
            Toast.makeText(
                this, R.string.no_activity_msg,
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == CODE_SELECT_STATION) {
            if (resultCode == RESULT_OK) {
                mSelectedStation!!.text = data
                    .getStringExtra(ListViewActivity.EXTRA_SELECTED_STATION)

            } else {
                mSelectedStation!!.setText(
                    R.string.no_station_seleceted_msg
                )
            }
        }
    }

// 1-ый - мы использовали способ сохранения данных при каждом клике пользователя и в onCreate()
// 2-ой - сохранять данные при получении (resultCode) из 2ой активности в onActivityResult
// 3-ий - сохранять данные при запуске onStop() т.е. в момент закрытии приложения

    @SuppressLint("ObsoleteSdkInt")
    private fun getBatteryPercentage(): Int {
        return when {
            Build.VERSION.SDK_INT >= 21 -> {
                val bm = this.getSystemService(BATTERY_SERVICE) as BatteryManager
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            }
            else -> {
                val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus: Intent? = this.registerReceiver(null, iFilter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

                val batteryPct = level!! / scale!!.toDouble()
                (batteryPct * 100).toInt()
            }
        }
    }

    companion object {
        const val CODE_SELECT_STATION = 1
        const val ACTION_PICK_STATION = "PICK_METRO_STATION"
        const val PREFS = "PREFS"
        const val NOTHING_SELECTED = "nothing selected"
    }
}
