package com.champion.bankrobot4.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.champion.bankrobot4.R;
import com.daimajia.numberprogressbar.NumberProgressBar;

/**
 * Created by 轾 on 2016/5/16.
 */
public class WebDialog extends Dialog {

    public WebDialog(Context context) {
        this(context, 0);
    }

    public WebDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {
        private Context context;
        private String site;
        private NumberProgressBar mProgressbar;
        private WebView webView;
        private ImageView webdialog_close;
        private WebChromeClient.CustomViewCallback myCallBack = null;
        private FrameLayout frameLayout = null;
        private View myView = null;
        private OnClickListener deleteListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setWebsite(String site) {
            this.site = site;
            return this;
        }

        public Builder setDeleteListener(OnClickListener listener) {
            this.deleteListener = listener;
            return this;
        }

        public WebDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final WebDialog dialog = new WebDialog(context, R.style.WebDialog);
            View layout = inflater.inflate(R.layout.webdialog, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            webView = (WebView) layout.findViewById(R.id.wd_web);
            mProgressbar = (NumberProgressBar) layout.findViewById(R.id.number_progress_bar);
            frameLayout = (FrameLayout) layout.findViewById(R.id.web_frameLayout);
            webdialog_close = (ImageView) layout.findViewById(R.id.webdialog_close);
            if (deleteListener != null) {
                webdialog_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }
            WebChromeClient chromeClient = new ChromeClient();
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setDatabaseEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setSaveFormData(false);
            settings.setLoadWithOverviewMode(true);
            settings.setAppCacheEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            settings.setSupportZoom(false);  //支持缩放
            settings.setBuiltInZoomControls(false);  //显示缩放工具
            settings.setUseWideViewPort(true);
            webView.setWebChromeClient(chromeClient);
            webView.setWebViewClient(new webViewClient());
            webView.loadUrl(site);
            return dialog;
        }

        class webViewClient extends WebViewClient {
            //重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                //如果不需要其他对点击链接事件的处理返回true，否则返回false
                return true;
            }
        }

        private class ChromeClient extends WebChromeClient implements MediaPlayer.OnCompletionListener {

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (myView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                //frameLayout.removeView(videoWebView);
                webView.setVisibility(View.GONE);
                frameLayout.addView(view);
                myView = view;
                myCallBack = callback;
                webView.setVisibility(View.VISIBLE);
                super.onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                if (myView == null) {
                    return;
                }
                frameLayout.removeView(myView);
                myView = null;
                //frameLayout.addView(videoWebView);
                webView.setVisibility(View.GONE);
                myCallBack.onCustomViewHidden();
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // TODO Auto-generated method stub
                Log.d("ZR", consoleMessage.message() + " at " + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressbar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressbar.setVisibility(View.GONE);
                } else {
                    mProgressbar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null) {
                    if (mp.isPlaying()) mp.stop();
                    mp.reset();
                    mp.release();
                    mp = null;
                }
            }
        }
    }

}
