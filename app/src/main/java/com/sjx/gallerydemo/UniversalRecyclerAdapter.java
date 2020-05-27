package com.sjx.gallerydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by swallowsonny on 2016/12/6.
 * 通用单一数据的adapter
 */

public abstract class UniversalRecyclerAdapter<T> extends RecyclerView.Adapter<UniversalRecyclerAdapter.UniversalRecyclerViewHolder> {
    private int itemLayoutId;
    private List<T> datas;
    private LayoutInflater mInflater;
    private Context context;

    public static final int FLAG_VISIBLE = 1;
    public static final int FLAG_INVISIBLE = 2;
    public static final int FLAG_GONE = 3;


    public UniversalRecyclerAdapter(Context context, int itemLayoutId, List<T> datas) {
        this.itemLayoutId = itemLayoutId;
        this.datas = datas;
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public UniversalRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = mInflater.inflate(itemLayoutId, parent, false);
        return UniversalRecyclerViewHolder.getHolder(context, parent, itemLayoutId);
    }

    @Override
    public void onBindViewHolder(UniversalRecyclerViewHolder holder, int position) {
        onBind(holder, position, datas.get(position));
    }

    protected abstract void onBind(UniversalRecyclerViewHolder holder, int position, T t);

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }


    /**
     * 通用的viewholder
     */
    public static class UniversalRecyclerViewHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> mViews;//存储itemView
        private View mConvertView;//itemView复用

        public UniversalRecyclerViewHolder(View itemView) {
            super(itemView);
            mConvertView = itemView;
            mViews = new SparseArray<>();
        }

        /**
         * **************************该方法重要***************************
         *
         * @param context
         * @param parent
         * @param itemLayoutId
         * @return
         */
        public static UniversalRecyclerViewHolder getHolder(Context context, ViewGroup parent, int itemLayoutId) {
            View itemView = LayoutInflater.from(context).inflate(itemLayoutId, parent, false);
            return new UniversalRecyclerViewHolder(itemView);
        }


        /**
         * 通过viewId（控件的ID）来获取控件
         *
         * @param viewId
         * @param <E>
         * @return
         */
        public <E extends View> E getView(int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = mConvertView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return (E) view;
        }

        ////////////////////////////////设置值//////////////////////////////////
        public UniversalRecyclerViewHolder setText(int viewId, CharSequence text) {
            TextView view = getView(viewId);
            view.setText(text);
            return this;
        }

        public UniversalRecyclerViewHolder setTextColor(int viewId, int color){
            TextView view = getView(viewId);
            view.setTextColor(color);
            return this;
        }

        public UniversalRecyclerViewHolder setTextSize(int viewId, int textSize) {
            TextView view = getView(viewId);
            view.setTextSize(textSize);
            return this;
        }


        public UniversalRecyclerViewHolder setImageResource(int viewId, int resId) {
            ImageView view = getView(viewId);
            view.setImageResource(resId);
            return this;
        }

        public UniversalRecyclerViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
            ImageView view = getView(viewId);
            view.setImageBitmap(bitmap);
            return this;
        }

        public UniversalRecyclerViewHolder setVisible(int viewId, boolean visible) {
            View view = getView(viewId);
            view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            return this;
        }


        /**
         * @param viewId
         * @param flag
         * @return
         */
        public UniversalRecyclerViewHolder setVisible(int viewId, int flag) {
            View view = getView(viewId);
            if (flag == FLAG_VISIBLE) {
                view.setVisibility(View.VISIBLE);
            } else if (flag == FLAG_INVISIBLE) {
                view.setVisibility(View.INVISIBLE);
            } else if (flag == FLAG_GONE) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        public UniversalRecyclerViewHolder setChecked(int viewId, boolean isChecked) {
            CheckBox view = getView(viewId);
            view.setChecked(isChecked);
            return this;
        }

        public UniversalRecyclerViewHolder setBackgroundResource(int viewId, int resColorId) {
            View view = getView(viewId);
            view.setBackgroundResource(resColorId);
            return this;
        }

        public UniversalRecyclerViewHolder setBackgroundColor(int viewId, int color) {
            View view = getView(viewId);
            view.setBackgroundColor(color);
            return this;
        }


        ///////////////////////////////////设置点击事件/////////////////////////////////////////
        public UniversalRecyclerViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
            View view = getView(viewId);
            view.setOnClickListener(listener);
            return this;
        }

    }
}
