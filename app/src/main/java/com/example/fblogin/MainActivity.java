package com.example.fblogin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private NotificationCompat.Builder notBuilder;

    private static final int MY_NOTIFICATION_ID = 12345;

    private static final int MY_REQUEST_CODE = 100;

    private LoginButton loginButton;
    private ImageView imageView;
    private TextView txtId, txtName, txtEmail;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.notBuilder = new NotificationCompat.Builder(this);

        // Thông báo sẽ tự động bị hủy khi người dùng click vào Panel

        this.notBuilder.setAutoCancel(true);

        loginButton = findViewById(R.id.login_button);
        imageView = findViewById(R.id.profile_pic);
        txtId = findViewById(R.id.profile_id);
        txtName = findViewById(R.id.profile_name);
        txtEmail = findViewById(R.id.profile_email);

        callbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this,"Login Cancel",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this,"Login Error",Toast.LENGTH_LONG).show();
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null){
            txtId.setText("Chưa đăng nhập");
            txtName.setText("Chưa đăng nhập");
            txtEmail.setText("Chưa đăng nhập");
            Toast.makeText(MainActivity.this,"Sign in, Please ...", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(MainActivity.this,"Ok Ok :))))", Toast.LENGTH_LONG).show();
            loadUserProfile(accessToken);
        }
        new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null){
                    txtId.setText("Đã đăng xuất");
                    txtName.setText("Đã đăng xuất");
                    txtEmail.setText("Đã đăng xuất");
                    imageView.setImageResource(R.mipmap.ic_launcher_round);
                    Toast.makeText(MainActivity.this,"Signed out", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(MainActivity.this,"Signed in", Toast.LENGTH_LONG).show();
                    loadUserProfile(currentAccessToken);
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }


    private void loadUserProfile(AccessToken newAccessToken){
        GraphRequest request = GraphRequest.newMeRequest(
                newAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object,
                                            GraphResponse response) {
                        // Application code
                        String name = object.optString("name");
                        String id = object.optString("id");
                        String email;
                        if(object.optString("email").equals(""))
                            email = id+"@facebook.com";
                        else
                            email = object.optString("email");
                        String url = "https://graph.facebook.com/"+id+"/picture?type=large";
                        txtEmail.setText(email);
                        txtId.setText(id);
                        txtName.setText(name);
                        Picasso.with(MainActivity.this).load(url).into(imageView);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields","id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void notiButtonClicked(View view)  {

        // --------------------------
        // Chuẩn bị một thông báo
        // --------------------------

        this.notBuilder.setSmallIcon(R.mipmap.ic_launcher);
        this.notBuilder.setTicker("Thích thì ấn vào");

        // Sét đặt thời điểm sự kiện xẩy ra.
        // Các thông báo trên Panel được sắp xếp bởi thời gian này.
        this.notBuilder.setWhen(System.currentTimeMillis()+ 10* 1000);
        this.notBuilder.setContentTitle("Đây là thông báo");
        this.notBuilder.setContentText("Đây là nội dung thông báo");

        // Tạo một Intent
        Intent intent = new Intent(this, MainActivity.class);


        // PendingIntent.getActivity(..) sẽ start mới một Activity và trả về
        // đối tượng PendingIntent.
        // Nó cũng tương đương với gọi Context.startActivity(Intent).
        PendingIntent pendingIntent = PendingIntent.getActivity(this, MY_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);


        this.notBuilder.setContentIntent(pendingIntent);

        // Lấy ra dịch vụ thông báo (Một dịch vụ có sẵn của hệ thống).
        NotificationManager notificationService  =
                (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Xây dựng thông báo và gửi nó lên hệ thống.

        Notification notification =  notBuilder.build();
        notificationService.notify(MY_NOTIFICATION_ID, notification);

    }
}
