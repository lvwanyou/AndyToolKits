package com.lwy.andytoolkits.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.lwy.andytoolkits.R
import com.lwy.andytoolkits.activity.sub.AudioTrackTestActivity
import com.lwy.andytoolkits.activity.sub.BitmapMainColorActivity
import com.lwy.andytoolkits.activity.sub.CodecTestActivity
import com.lwy.andytoolkits.activity.sub.CreativeCollectionActivity
import com.lwy.andytoolkits.activity.sub.ImageFusionTest2Activity
import com.lwy.andytoolkits.activity.sub.ImageFusionTest3Activity
import com.lwy.andytoolkits.activity.sub.ImageFusionTestActivity
import com.lwy.andytoolkits.activity.sub.ImgTransformTestActivity
import com.lwy.andytoolkits.activity.sub.LiveDataTestActivity
import com.lwy.andytoolkits.activity.sub.MediaCodecGLSurfaceView2Activity
import com.lwy.andytoolkits.activity.sub.MediaCodecGLSurfaceViewActivity
import com.lwy.andytoolkits.activity.sub.MediaCodecGLSurfaceViewWithPhotoActivity
import com.lwy.andytoolkits.activity.sub.MediaCodecSurfaceViewActivity
import com.lwy.andytoolkits.activity.sub.OpenGLES20Activity
import com.lwy.andytoolkits.activity.sub.RulerViewTestActivity
import com.lwy.andytoolkits.activity.sub.TextureViewES30Activity
import com.lwy.andytoolkits.activity.sub.VideoViewActivity
import com.lwy.andytoolkits.activity.sub.ZoomRotateImageTestActivity
import com.lwy.andytoolkits.bean.Menu
import com.lwy.andytoolkits.widget.adapter.MenuAdapter


class MainActivity : AppCompatActivity() {
    companion object {
        private const val READ_REQUEST_PERMISSION = 123
        private const val WRITE_REQUEST_PERMISSION = 234

        init {
            System.loadLibrary("opengl-test1-lib")
        }
    }

    /**
     * A native method that is implemented by the <tt> applicationtest </tt> native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_REQUEST_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_REQUEST_PERMISSION)
        }
        initViews()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Read Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == WRITE_REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Write Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initMenuList(): MutableList<Menu> { // 添加更多的菜单项...
        val menuList: MutableList<Menu> = ArrayList()
        menuList.add(Menu("Opengl 三角形示例", R.drawable.icon_arrow_triangle_right_fill_ltr, {
            startActivity(Intent(this, OpenGLES20Activity::class.java))
        }))
        menuList.add(Menu("刻度测试", R.drawable.icon_3_lines_horizontal_2, {
            startActivity(Intent(this, RulerViewTestActivity::class.java))
        }))

        menuList.add(
            Menu(
                "创意制作合集", R.drawable.icon_pen_star_fill, {}, hashMapOf(Pair("Lut图片") { _: View? ->
                startActivity(Intent(this, CreativeCollectionActivity::class.java))
            }, Pair("图片2D变换测试") { _: View? ->
                startActivity(Intent(this, ImgTransformTestActivity::class.java))
            }, Pair("获取图片主色测试") { _: View? ->
                startActivity(Intent(this, BitmapMainColorActivity::class.java))
            }, Pair("图片旋转缩放手势操作") { _: View? ->
                startActivity(Intent(this, ZoomRotateImageTestActivity::class.java))
            })
            )
        )

        menuList.add(Menu("mediaPlayer 测试", R.drawable.icon_video, {
            startActivity(Intent(this, VideoViewActivity::class.java))
        }))

        menuList.add(Menu("图片融合示例", R.drawable.icon_a_rectangle_on_rectangle, null, hashMapOf(Pair("图片融合示例1") { _: View? ->
            startActivity(Intent(this, ImageFusionTestActivity::class.java))
        }, Pair("图片融合示例2") { _: View? ->
            startActivity(Intent(this, ImageFusionTest2Activity::class.java))
        }, Pair("图片融合示例3") { _: View? ->
            startActivity(Intent(this, ImageFusionTest3Activity::class.java))
        })))

        menuList.add(Menu("liveData 示例", R.drawable.icon_live_entrance, {
            startActivity(Intent(this, LiveDataTestActivity::class.java))
        }))
        menuList.add(Menu("audioTrack 适配 Android15 示例", R.drawable.icon_2pt_music_note, {
            startActivity(Intent(this, AudioTrackTestActivity::class.java))
        }))

        menuList.add(
            Menu(
                "mediaCodec 解码示例", R.drawable.icon_gear_fill, null, hashMapOf(Pair("编解码测试") { _: View? ->
                startActivity(Intent(this, CodecTestActivity::class.java))
            }, Pair("mediaCodec surfaceView 解码示例") { _: View? ->
                startActivity(Intent(this, MediaCodecSurfaceViewActivity::class.java))
            }, Pair("mediaCodec GLSurfaceView 解码示例") { _: View? ->
                startActivity(Intent(this, MediaCodecGLSurfaceViewActivity::class.java))
            }, Pair("mediaCodec GLSurfaceView photo 解码示例") { _: View? ->
                startActivity(Intent(this, MediaCodecGLSurfaceViewWithPhotoActivity::class.java))
            }, Pair("mediaCodec GLSurfaceView2 解码示例") { _: View? ->
                startActivity(Intent(this, MediaCodecGLSurfaceView2Activity::class.java))
            }, Pair("TextureViewES30Activity 示例") { _: View? ->
                startActivity(Intent(this, TextureViewES30Activity::class.java))
            }))
        )

        return menuList
    }

    private fun initViews() {
        val txtShow: TextView = findViewById(R.id.txtShow)

        val menuList = initMenuList()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_menu_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MenuAdapter(menuList)

        useNativeStr(txtShow)
        fibonacciForTest(20)
    }

    private fun useNativeStr(tv: TextView) {
        tv.text = stringFromJNI()
    }

    fun fibonacciForTest(n: Int): Int {
        if (n <= 1) {
            return n
        }
        return fibonacciForTest(n - 1) + fibonacciForTest(n - 2)
    }
}