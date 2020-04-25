package com.example.asyncsample

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //画面部品ListViewを取得
        val lvCityList = findViewById<ListView>(R.id.lvCityList)
        //SimpleAdapterで使用するMutableListオブジェクトを用意
        val cityList: MutableList<MutableMap<String, String>> = mutableListOf()
        //都市データを格納するMutableMapオブジェクトの用意とcityListへのデータ登録
        var city = mutableMapOf("name" to "大津", "id" to "250010")
        cityList.add(city)
        city = mutableMapOf("name" to "彦根", "id" to "250020")
        cityList.add(city)
        city = mutableMapOf("name" to "京都", "id" to "260010")
        cityList.add(city)
        city = mutableMapOf("name" to "舞鶴", "id" to "260020")
        cityList.add(city)
        city = mutableMapOf("name" to "大阪", "id" to "270000")
        cityList.add(city)
        city = mutableMapOf("name" to "神戸", "id" to "280010")
        cityList.add(city)
        city = mutableMapOf("name" to "豊岡", "id" to "280020")
        cityList.add(city)
        //SimpleAdapterで使用するfrom-to用変数の用意
        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)
        //SimpleAdapterを生成。
        val adapter = SimpleAdapter(
            applicationContext,
            cityList,
            android.R.layout.simple_expandable_list_item_1,
            from,
            to
        )
        //ListViewにSimpleAdapterを設定
        lvCityList.adapter = adapter
        //リストタップのリスナクラス登録
        lvCityList.onItemClickListener = ListItemClickListener()
    }

    //リストがタップされたときの処理が記述されたメンバクラス
    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            //ListViewでタップされた行の都市名と都市IDを取得
            val item = parent.getItemAtPosition(position) as Map<String, String>
            val cityName = item["name"]
            val cityId = item["id"]
            //取得した都市名をtvCityNameに設定
            val tvCityName = findViewById<TextView>(R.id.tvCityName)
            tvCityName.text = cityName + "の天気： "
            //WeatherInfoReceiverインスタンスを生成
            val receiver = WeatherInfoReceiver()
            //WeatherInfoReceiverを実行
            receiver.execute(cityId)
        }
    }

    private inner class WeatherInfoReceiver() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {
            //可変長引数の1個目(インデックス0)を取得。これが都市ID
            val id = params[0]
            //都市IDを使って接続URL文字列を作成
            val urlStr = "http://weather.livedoor.com/forecast/webservice/json/v1?city=${id}"
            //URLオブジェクトを作成
            val url = URL(urlStr)
            //URLオブジェクトからHttpURLConnectionオブジェクトを取得
            val con = url.openConnection() as HttpURLConnection
            //http接続メソッドを設定
            con.requestMethod = "GET"
            //接続
            con.connect()
            //HttpURLConnectionオブジェクトからレスポンスデータを取得。天気情報が格納されている
            val stream = con.inputStream
            //レスポンスデータであるInputStreamオブジェクトを文字列(JSON文字列)に変換
            val result = is2String(stream)
            //HttpURLConnectionオブジェクトを解放
            con.disconnect()
            //InputStreamオブジェクトを解放
            stream.close()

            //JSON文字列を返す
            return result
        }

        override fun onPostExecute(result: String) {
            //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
            val rootJSON = JSONObject(result)
            //ルートJSON直下の「description」JSONオブジェクトを取得
            val descriptionJSON = rootJSON.getJSONObject("description")
            //「description」プロパティ直下の「text」文字列(天気概況文)を取得
            val desc = descriptionJSON.getString("text")
            //ルートJSON直下の「forecasts」JSON配列を取得
            val forecasts = rootJSON.getJSONArray("forecasts")
            //「forecasts」JSON配列のひとつ目(インデックス0)のJSONオブジェクトを取得
            val forecastNow = forecasts.getJSONObject(0)
            //「forecasts」ひとつ目のJSONオブジェクトから「telop」文字列(天気)を取得
            val telop = forecastNow.getString("telop")


            //天気情報用文字列をTextViewにセット
            val tvWeatherTelop = findViewById<TextView>(R.id.tvWeatherTelop)
            val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)
            tvWeatherTelop.text = telop
            tvWeatherDesc.text = desc
        }

        private fun is2String(stream: InputStream): String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while (line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }
}
