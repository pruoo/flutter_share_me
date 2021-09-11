package zhuoyuan.li.fluttershareme;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;


import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterShareMePlugin
 */
public class FlutterShareMePlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {

    private Activity activity;
    private MethodChannel methodChannel;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final FlutterShareMePlugin instance = new FlutterShareMePlugin();
        instance.onAttachedToEngine(registrar.messenger());
        instance.activity = registrar.activity();
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        activity = null;
    }

    private void onAttachedToEngine(BinaryMessenger messenger) {
        methodChannel = new MethodChannel(messenger, "flutter_share_me");
        methodChannel.setMethodCallHandler(this);
    }

    /**
     * method
     *
     * @param call   methodCall
     * @param result Result
     */
    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {
        String url, msg;
        switch (call.method) {
            case "shareTwitter":
                url = call.argument("url");
                msg = call.argument("msg");
                shareToTwitter(url, msg, result);
                break;
            case "shareWhatsApp":
                msg = call.argument("msg");
                url = call.argument("url");
                shareWhatsApp(url, msg, result, false);
                break;
            case "shareWhatsApp4Biz":
                msg = call.argument("msg");
                url = call.argument("url");
                shareWhatsApp(url, msg, result, true);
                break;
            case "system":
                msg = call.argument("msg");
                shareSystem(result, msg);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    /**
     * system share
     *
     * @param msg    String
     * @param result Result
     */
    private void shareSystem(Result result, String msg) {
        try {
            Intent textIntent = new Intent("android.intent.action.SEND");
            textIntent.setType("text/plain");
            textIntent.putExtra("android.intent.extra.TEXT", msg);
            activity.startActivity(Intent.createChooser(textIntent, "Share to"));
            result.success("success");
        } catch (Exception var7) {
            result.error("error", var7.toString(), "");
        }
    }

    /**
     * share to twitter
     *
     * @param url    String
     * @param msg    String
     * @param result Result
     */
    private void shareToTwitter(String url, String msg, Result result) {
        try {
            TweetComposer.Builder builder = new TweetComposer.Builder(activity)
                    .text(msg);
            if (url != null && url.length() > 0) {
                builder.url(new URL(url));
            }

            builder.show();
            result.success("success");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    /**
     * share to whatsapp
     *
     * @param msg                String
     * @param result             Result
     * @param shareToWhatsAppBiz boolean
     */
    private void shareWhatsApp(String url, String msg, Result result, boolean shareToWhatsAppBiz) {
        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("*/*");
            whatsappIntent.setPackage(shareToWhatsAppBiz ? "com.whatsapp.w4b" : "com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, msg);
            if (!TextUtils.isEmpty(url)) {
                File file = new File(url);
                Uri fileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                whatsappIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                //    whatsappIntent.setType("image/*");
            }
            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(whatsappIntent);
            result.success("success");
        } catch (Exception var9) {
            result.error("error", var9.toString(), "");
        }
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {

    }
}
