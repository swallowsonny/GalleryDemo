package com.sjx.gallerydemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_display.*
import org.greenrobot.eventbus.EventBus


class DisplayActivity: AppCompatActivity() {
    private lateinit var adapter: UniversalRecyclerAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        supportPostponeEnterTransition()


        val position = intent.getIntExtra("position", 0)
        ViewCompat.setTransitionName(viewPager2, MainActivity.imgUrls.get(position));
        viewPager2.orientation= ViewPager2.ORIENTATION_HORIZONTAL
        adapter = object : UniversalRecyclerAdapter<String>(this, R.layout.item_viewpager,
            MainActivity.imgUrls
        ){
            override fun onBind(holder: UniversalRecyclerViewHolder, position: Int, t: String) {
                Glide.with(this@DisplayActivity)
                    .load(t)
                    .centerInside()
                    .into(holder.getView(R.id.iv_img))
            }
        }
        viewPager2.adapter = adapter
        viewPager2.setCurrentItem(position, false)

        viewPager2.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewPager2.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    //释放
                    supportStartPostponedEnterTransition()
                }
            })
        tv_delte.setOnClickListener {
            MainActivity.imgUrls.removeAt(viewPager2.currentItem)
            adapter.notifyDataSetChanged()
            EventBus.getDefault().post(UpdateEvent(true))
        }
    }

    override fun onBackPressed() {
        checkFinish(RESULT_OK)
    }

    private fun checkFinish(resultCode: Int) {
        val url = MainActivity.imgUrls.get(viewPager2.currentItem)
        setEnterShareCallback(url)
        val intent = Intent()
        intent.putExtra("url", url)
        setResult(resultCode, intent)
        supportFinishAfterTransition()
    }

    private fun setEnterShareCallback(url: String) {
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                sharedElements.clear()
                names.clear()
                names.add(url)
                sharedElements[url] = viewPager2
            }
        })
    }
}