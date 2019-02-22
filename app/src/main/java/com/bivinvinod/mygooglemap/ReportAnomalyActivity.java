package com.bivinvinod.mygooglemap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class ReportAnomalyActivity extends AppCompatActivity {


    TextView txtlong;
    TextView txtltat;
    EditText txt1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_anomaly);
//*******************************************************************************************
    // *****INITIALIZATION OF VIEWS BEGINS

        txtlong=findViewById(R.id.txtlong);
        txtltat=findViewById(R.id.txtlat);
        txt1=findViewById(R.id.txt1);

        // *****INITIALIZATION OF VIEWS ENDS
 // *******************************************************************************************

// *******************************************************************************************

        //GET VALUES BEGINS

        Bundle extras = getIntent().getExtras();

        //double longt=extras.getDouble("LONGT");
        double lati= 0;
        double longiti=0;
        if (extras != null) {
            lati = extras.getDouble("LAT");
            longiti =extras.getDouble("LONGT");
        }

        //GET VALUES ENDS
// *******************************************************************************************

     //DATA BINDING

        txtlong.setText(longiti+"");
        txtltat.setText(lati+"");

        //txt1.setText();




    }
}
