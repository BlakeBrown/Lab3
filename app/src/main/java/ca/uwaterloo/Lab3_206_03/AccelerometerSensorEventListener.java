package ca.uwaterloo.Lab3_206_03;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import ca.uwaterloo.sensortoy.LineGraphView;

public class AccelerometerSensorEventListener implements SensorEventListener {

    // Initialize variables
    TextView stepsView;
    float currentMagnitude, prevMagnitude, currentDerivative, prevDerivative, magnitudeOfStep;
    Long prevTimestamp, currentTimestamp, lastStepTimestamp;
    int steps = 0;
    float[] values = new float[3];
    float[] prev20Magnitudes = new float[20];
    float largestMagnitudeInPast20Readings = 0;
    int filledCount = 0;
    boolean filled = false;
    LineGraphView accelerometerGraph;
    Button reset_button;

    // Constructor for the class, takes in a graph (for graphing steps), button (for resetting steps) and TextView (for displaying steps)
    public AccelerometerSensorEventListener(LineGraphView graph, Button button, TextView view1) {
        accelerometerGraph = graph;
        reset_button = button;
        stepsView = view1;
    }

    // Find the derivative given a point (y = magnitude, x = timestamp)
    public float calcDerivative(float magnitude, Long timestamp) {
        // On first iteration, set the prevTimestamp & prevMagnitude
        if(prevTimestamp == null && prevMagnitude == 0.0f) {
            prevTimestamp = timestamp;
            prevMagnitude = magnitude;
            return 0;
        } else {
            // Calculate the derivative by ( y2 - y1 / x2 - x1)
            float changeInY = magnitude-prevMagnitude;
            float changeInX = (float) (timestamp-prevTimestamp);
            float derivative = changeInY/changeInX;
            prevMagnitude = magnitude;
            prevTimestamp = timestamp;
            return derivative;
        }
    }

    public void findLargestMagnitudeInPast20Readings() {
        // If the array hasn't been filled yet
        if(!filled) {
            // Fill the next unfilled index in the array
            prev20Magnitudes[filledCount] = currentMagnitude;
            float largestValue = 0;
            // Find the largest value in the array
            for(int i = 0; i < filledCount; i++) {
                if(Math.abs(prev20Magnitudes[i]) > Math.abs(largestValue)) {
                    largestValue = prev20Magnitudes[i];
                }
            }
            largestMagnitudeInPast20Readings = largestValue;
            filledCount++;
            if(filledCount == prev20Magnitudes.length) {
                filled = true;
            }
        } else {
            // Move the array down by one index by moving everything backwards and setting the final index in the array to the current magnitude
            for(int i = 0; i < prev20Magnitudes.length; i++) {
                if(i == (prev20Magnitudes.length-1)) {
                    prev20Magnitudes[i] = currentMagnitude;
                } else {
                    prev20Magnitudes[i] = prev20Magnitudes[i+1];
                }
            }
            float largestValue = 0;
            // Find the largest value in the array
            for(int i = 0; i < prev20Magnitudes.length; i++) {
                if(Math.abs(prev20Magnitudes[i]) > Math.abs(largestValue)) {
                    largestValue = prev20Magnitudes[i];
                }
            }
            largestMagnitudeInPast20Readings = largestValue;
        }
    }

    public void checkForStep() {
        // There are 6 steps to our step checking algorithm:

        // 1. Check if there's been at least 100ms since the last step

        // 2. If there has, then use the sensor data (magnitude/timestamp) to calculate a derivative.
        // Check that the current derivative is negative and previous derivative was positive,
        // if this occurs we know there was probably just a peak in the graph.

        // 3. Check that the largest magnitude in the past 20 readings is < 5, this is to prevent steps being counted when the phone is being shaken
        // For walking the accelerometer magnitude should never be above 5

        // 4. Check that the current sensor magnitude is > 1, this is to prevent counting steps for miniscule values

        // 5. Lastly check time intervals, make sure there was somewhere between 600 and 1000 milliseconds since the last step was taken - a normal walking pace!

        // 6. If all of the above hold, increment the number of steps!
        if(lastStepTimestamp == null) {
            lastStepTimestamp = currentTimestamp;
        } else {
            // Find the difference between the current timestamp and timestamp of the last step
            float timeDiff = (currentTimestamp - lastStepTimestamp)/1000000;
            // Set the derivative if it hasn't been set yet
            if(prevDerivative == 0.0f) {
                prevDerivative = calcDerivative(currentMagnitude, currentTimestamp);
            } else {
                // Check for a step as long as it's been 100ms since the last step
                if(timeDiff > 100) {
                    currentDerivative = calcDerivative(currentMagnitude, currentTimestamp);
                    // Bulk of the step checking, implements steps 2-6 as mentioned above
                    if(largestMagnitudeInPast20Readings < 5 && currentMagnitude > 1.3 && currentDerivative < 0 && prevDerivative > 0 && (timeDiff > 600) && (timeDiff < 1000)) {
                        steps += 1;
                        lastStepTimestamp = currentTimestamp;
                        magnitudeOfStep = currentMagnitude;
                    }
                    prevDerivative = currentDerivative;
                }
            }
            if(timeDiff > 1000) {
                lastStepTimestamp = currentTimestamp;
            }
        }
    }

    public void onAccuracyChanged(Sensor s, int i) {

    }

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Reset the # of steps when the reset button is clicked
            reset_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vw) {
                    steps = 0;
                }
            });

            // Implement a low-pass filter to values received from the sensor, this will smooth out the data
            values[0] += (se.values[0] - values[0]) / 3;
            values[1] += (se.values[1] - values[1]) / 3;
            values[2] += (se.values[2] - values[2]) / 3;
            // Compute the magnitude of the acceleration vector sqrt(x^2 + y ^2 + z^2)
            currentMagnitude = (float) Math.sqrt(((values[0]*values[0]) + (values[1]*values[1]) + (values[2]*values[2])));
            currentTimestamp = se.timestamp;
            findLargestMagnitudeInPast20Readings();
            checkForStep();
            // Update the TextView
            stepsView.setText("Steps: " + steps);
            // Update the graph
            accelerometerGraph.addPoint(values);
        }
    }
}
