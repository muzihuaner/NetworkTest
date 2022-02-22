package com.huangetech.networktest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.xml.sax.InputSource
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.SAXParserFactory
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendRequestBtn.setOnClickListener {
//            sendRequestWithHttpURLConnection()
            sendRequestWithOkhttp()
        }
    }


    private fun sendRequestWithHttpURLConnection() {
//        开启线程发起网络请求
        thread {
            var connection: HttpURLConnection? = null
            try {
                var geturl = edit_query.text.toString()
                var response = StringBuilder()
                val url = URL("https://zhiurl.cn")
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                val output = DataOutputStream(connection.outputStream)
                output.writeBytes("{\"url\":\"$geturl\"}")

                val input = connection.inputStream
//                对获取到的输入流进行读取
                val reader = BufferedReader(InputStreamReader(input))
                reader.use {
                    reader.forEachLine {
                        response.append(it)
                    }
                }
                showResponse(response.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun sendRequestWithOkhttp() {
        thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
//                    .url("https://quickso.cn/other/get_data.xml")
                    .url("https://quickso.cn/other/get_data.json")
                    .build()
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                if (responseData != null) {
//                    showResponse(responseData)
//                    parseXMLWithPull(responseData)
//                    parseXMLWithSAX(responseData)
//                    parseJSONWithJSONObject(responseData)
                    parseJSONWithGSON(responseData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




    private fun parseXMLWithSAX(xmlData: String) {
        try {
            val factory = SAXParserFactory.newInstance()
            val xmlReader = factory.newSAXParser().getXMLReader()
            val handler = ContentHandler()
//           将ContentHandler实例设置到XMLReader中
            xmlReader.contentHandler = handler
//           开始执行解析
            xmlReader.parse(InputSource(StringReader(xmlData)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showResponse(response: String) {
        runOnUiThread {
//            在这里进行UI操作，将结果显示在界面上
            ResponseText.text = response
        }
    }

    private fun parseXMLWithPull(xmlData: String) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val xmlPullParser = factory.newPullParser()
            xmlPullParser.setInput(StringReader(xmlData))
            var eventType = xmlPullParser.eventType
            var id = ""
            var name = ""
            var version = ""
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val nodeName = xmlPullParser.name
                when (eventType) {
//                    开始解析某个节点
                    XmlPullParser.START_TAG -> {
                        when (nodeName) {
                            "id" -> id = xmlPullParser.nextText()
                            "name" -> name = xmlPullParser.nextText()
                            "version" -> version = xmlPullParser.nextText()
                        }
                    }
//                    完成解析某个节点
                    XmlPullParser.END_TAG -> {
                        if ("app" == nodeName) {
                            Log.d("MainActivity", "id is $id")
                            Log.d("MainActivity", "name is $name")
                            Log.d("MainActivity", "version is $version")
                        }
                    }
                }
                eventType = xmlPullParser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun parseJSONWithJSONObject(jsonData: String) {
        try {
            val jsonArray=JSONArray(jsonData)
            for (i in 0 until jsonArray.length()){
                val jsonObject=jsonArray.getJSONObject(i)
                val id=jsonObject.getString("id")
                val name=jsonObject.getString("name")
                val version=jsonObject.getString("version")
                Log.d("MainActivity", "id is $id")
                Log.d("MainActivity", "name is $name")
                Log.d("MainActivity", "version is $version")
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun parseJSONWithGSON(jsonData: String) {
        val gson=Gson()
        val typeOf=object :TypeToken<List<App>>() {}.type
        val applist=gson.fromJson<List<App>>(jsonData,typeOf)
        for (app in applist){
            Log.d("MainActivity", "id is ${app.id}")
            Log.d("MainActivity", "name is ${app.name}")
            Log.d("MainActivity", "version is ${app.version}")
        }
    }

}