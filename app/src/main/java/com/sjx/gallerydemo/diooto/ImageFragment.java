package com.sjx.gallerydemo.diooto;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sjx.gallerydemo.R;

import me.panpf.sketch.Sketch;
import me.panpf.sketch.SketchImageView;
import me.panpf.sketch.cache.DiskCache;
import me.panpf.sketch.decode.ImageAttrs;
import me.panpf.sketch.drawable.SketchGifDrawable;
import me.panpf.sketch.request.CancelCause;
import me.panpf.sketch.request.DisplayListener;
import me.panpf.sketch.request.DownloadProgressListener;
import me.panpf.sketch.request.ErrorCause;
import me.panpf.sketch.request.ImageFrom;
import me.panpf.sketch.request.LoadListener;
import me.panpf.sketch.request.LoadRequest;
import me.panpf.sketch.request.LoadResult;
import me.panpf.sketch.util.SketchUtils;

public class ImageFragment extends Fragment {
    DragDiootoView dragDiootoView;
    ContentViewOriginModel contentViewOriginModel;
    TextView tv_delete;
    String url;
    SketchImageView sketchImageView;
    int position;
    FrameLayout loadingLayout;
    boolean shouldShowAnimation = false;
    boolean hasCache;

    public DragDiootoView getDragDiootoView() {
        return dragDiootoView;
    }

    public static ImageFragment newInstance(String url, int position, boolean shouldShowAnimation, ContentViewOriginModel contentViewOriginModel) {
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putInt("position", position);
        args.putBoolean("shouldShowAnimation", shouldShowAnimation);
        args.putParcelable("model", contentViewOriginModel);
        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        if (getArguments() != null) {
            url = getArguments().getString("url");
            position = getArguments().getInt("position");
            shouldShowAnimation = getArguments().getBoolean("shouldShowAnimation");
            contentViewOriginModel = getArguments().getParcelable("model");
        }
        loadingLayout = view.findViewById(R.id.loadingLayout);
        dragDiootoView = view.findViewById(R.id.dragDiootoView);
        tv_delete = view.findViewById(R.id.tv_delete);
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteListener != null) {
                    onDeleteListener.onDelete(url, position);
                }
            }
        });
        loadingLayout.setVisibility(View.GONE);
        sketchImageView = new SketchImageView(getContext());
        sketchImageView.getOptions().setDecodeGifImage(true);
        sketchImageView.setZoomEnabled(true);
        dragDiootoView.addContentChildView(sketchImageView);
        sketchImageView.getZoomer().getBlockDisplayer().setPause(!isVisibleToUser());
        return view;
    }

    private Diooto.OnDeleteListener onDeleteListener;

    public void setOnDelteListener(Diooto.OnDeleteListener onDelteListener) {
        this.onDeleteListener = onDelteListener;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getContext() == null || getActivity() == null) {
            return;
        }
        if (Diooto.onLoadPhotoBeforeShowBigImageListener != null) {
            if (dragDiootoView.getContentView() instanceof SketchImageView) {
                Diooto.onLoadPhotoBeforeShowBigImageListener.loadView((SketchImageView) dragDiootoView.getContentView(), position);
            } else if (dragDiootoView.getContentParentView().getChildAt(1) instanceof SketchImageView) {
                Diooto.onLoadPhotoBeforeShowBigImageListener.loadView((SketchImageView) dragDiootoView.getContentParentView().getChildAt(1), 0);
                dragDiootoView.getContentParentView().getChildAt(1).setVisibility(View.VISIBLE);
            }
        }
        dragDiootoView.setOnShowFinishListener(new DragDiootoView.OnShowFinishListener() {
            @Override
            public void showFinish(DragDiootoView view, boolean showImmediately) {
                loadImage();
            }
        });
        DiskCache diskCache = Sketch.with(getContext()).getConfiguration().getDiskCache();
        dragDiootoView.putData(contentViewOriginModel.getLeft(), contentViewOriginModel.getTop(), contentViewOriginModel.getWidth(), contentViewOriginModel.getHeight());
        //如果显示的点击的position  则进行动画处理
        dragDiootoView.show(!shouldShowAnimation);
        dragDiootoView.setOnFinishListener(new DragDiootoView.OnFinishListener() {
            @Override
            public void callFinish() {
                if (getContext() instanceof ImageActivity) {
                    ((ImageActivity) getContext()).finishView();
                }
                if (Diooto.onFinishListener != null) {
                    Diooto.onFinishListener.finish(dragDiootoView);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dragDiootoView.notifySizeConfig();
    }


    /**
     * 由于图片框架原因  这里需要使用两种不同的加载方式
     * 如果有缓存  直接可显示
     * 如果没缓存 则需要等待加载完毕  才能够将图片显示在view上
     */
    private void loadImage() {
        if (getContext() == null || sketchImageView == null) {
            return;
        }
        if (hasCache) {
            loadWithCache();
        } else {
            loadWithoutCache();
        }
    }

    private void loadWithCache() {
        sketchImageView.setDisplayListener(new DisplayListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onCompleted(@NonNull Drawable drawable, @NonNull ImageFrom imageFrom, @NonNull ImageAttrs imageAttrs) {
                int w = drawable.getIntrinsicWidth();
                int h = drawable.getIntrinsicHeight();
                //如果有缓存  直接将大小变为最终大小而不是去调用notifySize来更新 并且是直接显示不进行动画
                dragDiootoView.putData(
                        contentViewOriginModel.getLeft(), contentViewOriginModel.getTop(),
                        contentViewOriginModel.getWidth(), contentViewOriginModel.getHeight(),
                        w, h);
                dragDiootoView.show(true);
            }

            @Override
            public void onError(@NonNull ErrorCause cause) {
            }

            @Override
            public void onCanceled(@NonNull CancelCause cause) {

            }
        });
        sketchImageView.setDownloadProgressListener(new DownloadProgressListener() {
            @Override
            public void onUpdateDownloadProgress(int totalLength, int completedLength) {
                loadingLayout.setVisibility(View.VISIBLE);
                int ratio = (int) (completedLength / (float) totalLength * 100);
            }
        });
        sketchImageView.displayImage(url);
    }

    LoadRequest loadRequest;

    private void loadWithoutCache() {
        loadRequest = Sketch.with(getContext()).load(url, new LoadListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onCompleted(@NonNull LoadResult result) {
                if (result.getGifDrawable() != null) {
                    result.getGifDrawable().followPageVisible(true, true);
                }
                int w = result.getBitmap().getWidth();
                int h = result.getBitmap().getHeight();
                dragDiootoView.notifySize(w, h);
                sketchImageView.displayImage(url);
                hasCache = true;
            }

            @Override
            public void onError(@NonNull ErrorCause cause) {
            }

            @Override
            public void onCanceled(@NonNull CancelCause cause) {
            }
        }).downloadProgressListener(new DownloadProgressListener() {
            @Override
            public void onUpdateDownloadProgress(int totalLength, int completedLength) {
            }
        }).commit();
    }

    @Override
    public void onDestroyView() {
        if (loadRequest != null) {
            loadRequest.cancel(CancelCause.ON_DETACHED_FROM_WINDOW);
            loadRequest = null;
        }
        super.onDestroyView();
    }

    public void backToMin() {
        dragDiootoView.backToMin();
    }

    /**
     * SketchImageView 生命周期处理
     */

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onUserVisibleChanged(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            onUserVisibleChanged(true);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed()) {
            onUserVisibleChanged(isVisibleToUser);
        }
    }

    public boolean isVisibleToUser() {
        return isResumed() && getUserVisibleHint();
    }

    protected void onUserVisibleChanged(boolean isVisibleToUser) {
        // 不可见的时候暂停分块显示器，节省内存，可见的时候恢复
        if (sketchImageView != null && sketchImageView.isZoomEnabled()) {
            sketchImageView.getZoomer().getBlockDisplayer().setPause(!isVisibleToUser);
            Drawable lastDrawable = SketchUtils.getLastDrawable(sketchImageView.getDrawable());
            if (lastDrawable != null && (lastDrawable instanceof SketchGifDrawable)) {
                ((SketchGifDrawable) lastDrawable).followPageVisible(isVisibleToUser, false);
            }
        }
    }
}
