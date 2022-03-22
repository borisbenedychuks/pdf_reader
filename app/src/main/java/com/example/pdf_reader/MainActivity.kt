package com.example.pdf_reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pdf_reader.databinding.ActivityMainBinding
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleScope.launch(Dispatchers.IO) {
            val name = "sample.pdf"
            val response = service.sample(name).body() ?: throw Exception()
            val directory = "${filesDir.path}/pdfs"
            val directoryF = File(directory)
            if (directoryF.exists().not()) directoryF.mkdir()
            response.byteStream().saveToFile("${directory}/$name")
            response.close()
            val file = File("$directory/$name")
            val bitmap = Bitmap.createBitmap(
                binding.root.measuredWidth,
                binding.root.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            PdfRenderer(ParcelFileDescriptor.open(file, MODE_READ_ONLY)).openPage(0)
                .render(bitmap, null, null, RENDER_MODE_FOR_DISPLAY)
            withContext(Dispatchers.Main) {
                binding.imageView.setImageBitmap(bitmap)
            }
        }
    }
}

fun InputStream.saveToFile(filePath: String) = buffered().use { input ->
    File(filePath).outputStream().buffered().use { output ->
        input.copyTo(output)
        output.flush()
    }
}

val retrofit = Retrofit.Builder()
    .baseUrl("http://www.africau.edu/images/default/")
    .build()

val service = retrofit.create(ApiService::class.java)

interface ApiService {

    @GET("{id}")
    suspend fun sample(@Path("id") id: String): Response<ResponseBody>
}

fun log(mess: String) = Log.d("TEST", mess)