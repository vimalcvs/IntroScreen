package com.vimal.cvs.introscreen;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;


public class SplashActivity extends Activity {

    private static int SPLASH_TIME_OUT = 1100;

    private ImageView mImageViewLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeViews();
       // animateLogo();


        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!isStoragePermissionGranted()) {
            Toast.makeText(this, "Please allow Permission to continue..", Toast.LENGTH_SHORT).show();
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else if (isStoragePermissionGranted()) {

            startActivity();
        }

    }


    private void initializeViews() {
        mImageViewLogo = findViewById(R.id.splash);
    }

    public void startActivity() {
        handlerSplash();
    }

    public void handlerSplash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(SplashActivity.this, LaunchActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }


    private void animateLogo() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_without_duration);
        fadeInAnimation.setDuration(SPLASH_TIME_OUT);
        mImageViewLogo.startAnimation(fadeInAnimation);
    }



    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("PPP", "Permission is granted");
                //startActivity();
                return true;
            } else {

                Log.v("PPP", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("PPP", "Permission is granted");
            //startActivity();
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {

                        //openFragment();
                        startActivity();
                    }

                } else {
                    /*ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            1);*/
                    finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void forceUpdate() {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String currentVersion = packageInfo.versionName;
        ForceUpdateAsync forceUpdateAsync = new ForceUpdateAsync(SplashActivity.this);
        //new ForceUpdateAsync(currentVersion, DrawerActivity.this).execute();
        try {
            String latestVersion = forceUpdateAsync.execute().get();
            if (latestVersion != null) {
                Log.v("Splash", "|| " + latestVersion + " || " + currentVersion);
                if (currentVersion.equalsIgnoreCase(latestVersion)) {
                    //Toast.makeText(DrawerActivity.this, "Update Available", Toast.LENGTH_SHORT).show();
                    startDialog();
                    //isUpdate = true;
                } else {
                    //Toast.makeText(DrawerActivity.this, "Update Not Available", Toast.LENGTH_SHORT).show();
                    //isUpdate = false;
                    handlerSplash();
                }
            } else {
                //isUpdate = false;
                handlerSplash();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void startDialog() {
        final AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
                SplashActivity.this);
        myAlertDialog.setTitle("Update Available");
        myAlertDialog.setMessage("A new version of WhatsApp Statuses Saver is available. Please update to version");

        myAlertDialog.setPositiveButton("Update",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        handlerSplash();
                        rateUsOnPlayStore();
                    }
                });

        myAlertDialog.setNegativeButton("Not Now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        handlerSplash();

                    }
                });
        myAlertDialog.show();
    }


    public void rateUsOnPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }
}
