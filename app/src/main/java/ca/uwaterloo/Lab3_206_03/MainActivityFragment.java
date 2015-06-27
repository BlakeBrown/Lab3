package ca.uwaterloo.Lab3_206_03;

import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

import ca.uwaterloo.Lab3_206_03.R;
import ca.uwaterloo.mapper.MapLoader;
import ca.uwaterloo.mapper.NavigationalMap;
import ca.uwaterloo.sensortoy.LineGraphView;
import ca.uwaterloo.mapper.MapView;

public class MainActivityFragment extends Fragment {

    // Variable declarations
    private MapView mv;
    private SensorManager sensorManager;
    private SensorEventListener accelerometerEventListener;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create a linear layout so that we can scroll in case the content goes off screen
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.label);

        //Initialize the map
        mv = new MapView(rootView.getContext(), 400, 400, 60, 60);
        registerForContextMenu(mv);
        layout.addView(mv);
        NavigationalMap map = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-peninsula.svg");
        mv.setMap(map);


        // Initialize a graph for displaying accelerometer data
        LineGraphView graph = new LineGraphView(rootView.getContext(),100, Arrays.asList("x", "y", "z"));
        layout.addView(graph);
        graph.setVisibility(View.VISIBLE);
        Button reset_button = (Button) rootView.findViewById(R.id.reset_button);

        // Initialize the sensor manager for our sensors
        sensorManager = (SensorManager) rootView.getContext().getSystemService(rootView.getContext().SENSOR_SERVICE);

        // Create a text view to hold our number of steps
        TextView stepsTextView = new TextView(rootView.getContext());
        // Create the accelerometer sensor event listener
        accelerometerEventListener = new AccelerometerSensorEventListener(graph, reset_button, stepsTextView);
        // TYPE_LINEAR_ACCELERATION is the same as TYPE_ACCELEROMETER but without gravity
        // SENSOR_DELAY_FASTEST is the fastest rate at which to read sensor data
        sensorManager.registerListener(accelerometerEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);

        // Add the text view to our layout
        layout.addView(stepsTextView);
        return rootView;
    }

     @Override
     public void onCreateContextMenu(ContextMenu menu , View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         mv.onCreateContextMenu(menu,v, menuInfo);
     }

    @Override
    public boolean  onContextItemSelected(MenuItem item) {
        return  super.onContextItemSelected(item) ||  mv.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-register sensor event listeners
        sensorManager.registerListener(accelerometerEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        // Unregister sensor event listeners
        sensorManager.unregisterListener(accelerometerEventListener);
        super.onPause();
    }


}
