package com.sjx.gallerydemo

import android.app.SharedElementCallback
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.hw.ycshareelement.YcShareElement
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import android.view.ViewTreeObserver
import android.app.Activity


class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: UniversalRecyclerAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        YcShareElement.enableContentTransition(application);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
        setContentView(R.layout.activity_main)

        val glm = GridLayoutManager(this, 1)
        rv_imgs.layoutManager = glm
        mAdapter = object : UniversalRecyclerAdapter<String>(this, R.layout.item_grid, imgUrls){
            override fun onBind(holder: UniversalRecyclerViewHolder, position: Int, t: String) {
                val img = holder.getView<ImageView>(R.id.iv_img)
                Glide.with(this@MainActivity)
                    .load(t)
                    .dontAnimate()
//                    .centerCrop()
                    .into(img)
                ViewCompat.setTransitionName(img, t);
                holder.setOnClickListener(R.id.iv_img){
                    val intent = Intent(this@MainActivity, DisplayActivity::class.java)
                    intent.putExtra("position", position)
                    val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MainActivity,
                        img,
                        t
                    )
                    startActivity(
                        intent,
                        optionsCompat.toBundle()
                    )
                }
            }
        }
        rv_imgs.adapter = mAdapter
    }
    override fun onActivityReenter(resultCode: Int, data: Intent) {
        if (resultCode === Activity.RESULT_OK) {
            //针对classloader回收 我这里出现了这个问题，如果没有请忽略
            data.setExtrasClassLoader(classLoader)
            val url = data.getStringExtra("url")
            val index = imgUrls.indexOf(url)
            if (index != -1){
                rv_imgs.scrollToPosition(index)
            }
            mAdapter.notifyDataSetChanged()
            supportPostponeEnterTransition()
            rv_imgs.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        rv_imgs.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        supportStartPostponedEnterTransition()
                    }
                })
        }

        super.onActivityReenter(resultCode, data)
    }

    @Subscribe
    public fun onMessage(updateEvent: UpdateEvent){
        mAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }

    companion object{
        val imgUrls = arrayListOf(
//            "http://img1.juimg.com/140908/330608-140ZP1531651.jpg",
            "http://img1.juimg.com/180903/330620-1PZ312404883.jpg",
            "http://img1.juimg.com/180914/330813-1P914121H412.jpg",
            "http://img1.juimg.com/180821/330788-1PR10J44285.jpg",
            "http://img1.juimg.com/200421/179083-2004211Q13719.jpg",
            "http://img1.juimg.com/190317/323074-1Z31G9560792.jpg",
            "http://img1.juimg.com/190706/177835-1ZF605033180.jpg",
            "http://img1.juimg.com/190706/177835-1ZF605001171.jpg",
            "http://img1.juimg.com/190706/177835-1ZF604233131.jpg"
        )
    }
}
