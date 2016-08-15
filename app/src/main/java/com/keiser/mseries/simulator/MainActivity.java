package com.keiser.mseries.simulator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner buildMajorSpinner = (Spinner) findViewById(R.id.buildMajorSpinner);

        ArrayAdapter<CharSequence> buildMajorArray = ArrayAdapter.createFromResource(this,
                R.array.build_majors_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        buildMajorArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        buildMajorSpinner.setAdapter(buildMajorArray);

        ArrayList<String> bikeIDArray = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            bikeIDArray.add(Integer.toString(i));
        }

        Spinner bikeIDSpinner = (Spinner) findViewById(R.id.bikeIDSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, bikeIDArray);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        bikeIDSpinner.setAdapter(adapter);

    }

    public void onSimulateButtonClicked(View v) {
        Spinner buildMajorSpinner = (Spinner) findViewById(R.id.buildMajorSpinner);
        String buildItem = (String) buildMajorSpinner.getSelectedItem();

        Spinner bikeIDSpinner = (Spinner) findViewById(R.id.bikeIDSpinner);
        String bikeItem = (String) bikeIDSpinner.getSelectedItem();
        Intent intent = new Intent(this, SimulationActivity.class);
        intent.putExtra("BuildMajor", buildItem);
        intent.putExtra("BikeID", bikeItem);
        startActivity(intent);
    }
}
