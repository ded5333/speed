package com.astend.android.myapplicationspeed;

import java.util.Formatter;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity implements IBaseGpsListener {
  TextView timer;
  Button reset;
  long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
  Handler handler;
  int Seconds, Minutes, MilliSeconds;

  CLocation cLocation = null;

  @SuppressLint("StaticFieldLeak")
  AsyncTask myTimer = new AsyncTask() {
    long startTimerTime = 0L;
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      startTimerTime = System.currentTimeMillis();
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
      updatePassedTime();
      super.onProgressUpdate(values);

    }

    @Override
    protected Object doInBackground(Object[] objects) {
      float speed = cLocation.getSpeed();
      while (speed < 3) {
        publishProgress();
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      return null;
    }

    @Override
    protected void onCancelled() {
      super.onCancelled();
      updatePassedTime();
    }

    private void updatePassedTime() {
      long passedTime = System.currentTimeMillis() - startTimerTime;
      int minutes = (int) (passedTime / 1000 / 60);
      int seconds = (int) (passedTime/1000 % 60);
      int milliSeconds = (int) (passedTime % 1000);

      timer.setText(minutes + ":" + seconds + ":" + milliSeconds);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    timer = findViewById(R.id.tvTimer);
    reset = findViewById(R.id.btnReset);
    handler = new Handler();
    reset.setEnabled(false);

    reset.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MillisecondTime = 0L;
        StartTime = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;

        timer.setText("00:00:00");
      }
    });


    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    if (ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this);
    this.updateSpeed(null);

    CheckBox chkUseMetricUntis = (CheckBox) this.findViewById(R.id.chkMetricUnits);
    chkUseMetricUntis.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        MainActivity.this.updateSpeed(null);
      }
    });
  }

  public void finish() {
    super.finish();
    System.exit(0);
  }

  private void updateSpeed(CLocation location) {
    // TODO Auto-generated method stub
    float nCurrentSpeed = 0;

    if (location != null) {
      location.setUseMetricunits(this.useMetricUnits());
      nCurrentSpeed = location.getSpeed();
    }


    Log.d("TAG", "SPEEd" + nCurrentSpeed);

    Formatter fmt = new Formatter(new StringBuilder());
    fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
    String strCurrentSpeed = fmt.toString();
    strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

    String strUnits = "km/hour";
    if (this.useMetricUnits()) {
      strUnits = "meters/second";
    }

    TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.txtCurrentSpeed);
    txtCurrentSpeed.setText(strCurrentSpeed + " " + strUnits);


//
//    if (nCurrentSpeed > 0){
//      StartTime = SystemClock.uptimeMillis();
//      handler.postDelayed(runnable, 0);
//
//      reset.setEnabled(false);
//      if(nCurrentSpeed >6){
//        TimeBuff += MillisecondTime;
//
//        handler.removeCallbacks(runnable);
//
//        reset.setEnabled(true);
//      }
//
//    }

    if (nCurrentSpeed > 0) {
      // StartTime = SystemClock.uptimeMillis();
      //if(!isStarted){
      //  handler.postDelayed(runnable, 0);
    //  }


      if (myTimer.getStatus() == AsyncTask.Status.PENDING)
        myTimer.execute();
      reset.setEnabled(false);

    }
    else if (nCurrentSpeed > 50) {
      myTimer.cancel(false);
      TimeBuff += MillisecondTime;

      //  handler.removeCallbacks(runnable);

      reset.setEnabled(true);
    }


  }

//  public Runnable runnable = new Runnable() {
//
//    public void run() {
//        isStarted = true;
//      MillisecondTime = SystemClock.uptimeMillis() - StartTime;
//
//      UpdateTime = TimeBuff + MillisecondTime;
//
//      Seconds = (int) (UpdateTime / 1000);
//
//      Minutes = Seconds / 60;
//
//      Seconds = Seconds % 60;
//
//      MilliSeconds = (int) (UpdateTime % 1000);
//
//      timer.setText("" + Minutes + ":"
//          + String.format("%02d", Seconds) + ":"
//          + String.format("%03d", MilliSeconds));
//
//      handler.postDelayed(this, 0);
//
//    }
//
//  };


  private boolean useMetricUnits() {
    // TODO Auto-generated method stub
    CheckBox chkUseMetricUnits = (CheckBox) this.findViewById(R.id.chkMetricUnits);
    return chkUseMetricUnits.isChecked();
  }

  @Override
  public void onLocationChanged(Location location) {
    // TODO Auto-generated method stub
    if (location != null) {
      cLocation = new CLocation(location, this.useMetricUnits());
      this.updateSpeed(cLocation);
    }
  }

  @Override
  public void onProviderDisabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderEnabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onGpsStatusChanged(int event) {
    // TODO Auto-generated method stub

  }


}