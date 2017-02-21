package ph.com.valtes.mobileapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

import ph.com.valtes.mobileapplication.R;

public class RegistrationActivity extends AppCompatActivity {

    public static final int SIGNATURE_ACTIVITY = 1;
    public static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 1;

    static WebView webView;
    String bankNumber = "0123456789";
    String firstName = "Pipito";
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

        webView.loadUrl("http://192.168.100.41:8080/WebApplicationService/");
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

}
