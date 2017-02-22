package ph.com.valtes.mobileapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import ph.com.valtes.mobileapplication.R;

public class RegistrationActivity extends AppCompatActivity {

    public static final int SIGNATURE_ACTIVITY = 1;
    public static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 1;

    static WebView webView;
    String bankNumber = "0123456789";
    String firstName = "Pepito";
    String lastName = "Manaloto";
    String middleName = "S";
    String contacNumber = "09161234567";
    String emailAddress = "sample@valtes.com.ph";

    String messageBody = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        webView = (WebView) this.findViewById(R.id.webDisplay);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                injectJS(view);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
        });

        Button getSignature = (Button) findViewById(R.id.signature);
        getSignature.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, CaptureSignature.class);
                startActivityForResult(intent,SIGNATURE_ACTIVITY);
            }
        });

        requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},
                MY_PERMISSIONS_REQUEST_RECEIVE_SMS);

        webView.loadUrl("https://192.168.100.41:8443/WebApplicationService/");
        webView.loadUrl("javascript: " +
                "var isAccessedFromMobile = '1';");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode) {
            case SIGNATURE_ACTIVITY:
                if (resultCode == RESULT_OK) {

                    Bundle bundle = data.getExtras();
                    String status  = bundle.getString("status");
                    if(status.equalsIgnoreCase("done")){
                        Toast toast = Toast.makeText(this, "Signature capture successful!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 105, 50);
                        toast.show();
                    }
                }
                break;
        }
    }

    private void injectJS(WebView view) {
        try {
            view.loadUrl("javascript: " +
                    "var bankNumber = document.getElementById('card_number').value = '"+bankNumber+"';" +
                    "var lastName = document.getElementById('last_name').value = '"+lastName+"'; " +
                    "var firstName = document.getElementById('first_name').value = '"+firstName+"'; " +
                    "var middleName = document.getElementById('middle_name').value = '"+middleName+"'; " +
                    "var contactNumber = document.getElementById('contact_number').value = '"+contacNumber+"'; " +
                    "var contactNumber = document.getElementById('email_address').value = '"+emailAddress+"';");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMessageBody(String pMessageBody){
        messageBody = pMessageBody;

        String[] data = pMessageBody.split(";");
        bankNumber = data[0];
        lastName = data[1];
        firstName = data[2];
        middleName = data[3];
        contacNumber = data[4];
        emailAddress = data[5];

        webView.loadUrl("javascript: " +
                "var bankNumber = document.getElementById('card_number').value = '"+bankNumber+"';" +
                "var lastName = document.getElementById('last_name').value = '"+lastName+"'; " +
                "var firstName = document.getElementById('first_name').value = '"+firstName+"'; " +
                "var middleName = document.getElementById('middle_name').value = '"+middleName+"'; " +
                "var contactNumber = document.getElementById('contact_number').value = '"+contacNumber+"'; " +
                "var contactNumber = document.getElementById('email_address').value = '"+emailAddress+"';");
    }

    public void injectSignatureString(String pEncodedSignature){
        webView.loadUrl("javascript: " +
                "var encodedSignatureString = document.getElementById('img').src = 'data:image/png;base64, "+pEncodedSignature+"';");
    }
}
