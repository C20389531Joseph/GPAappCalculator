package com.example.msdassignment;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private Button addButton, calculateButton, screenshotButton;
    private TextView resultTextView;
    private int rowCount = 4; // Initial number of rows
    private Spinner creditsSpinner; // for credit dropbox
    private EditText moduleNameEditText; // for module name
    private ArrayAdapter<CharSequence> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        linearLayout = findViewById(R.id.linearLayout);
        addButton = findViewById(R.id.addButton);
        calculateButton = findViewById(R.id.calculateButton);
        resultTextView = findViewById(R.id.resultTextView);
        screenshotButton = findViewById(R.id.screenshotButton);
        creditsSpinner = findViewById(R.id.creditsSpinner);
        moduleNameEditText = findViewById(R.id.moduleNameEditText);


        // Set up the Spinner with credit options
        adapter = ArrayAdapter.createFromResource(
                this,
                R.array.credit_options,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        creditsSpinner.setAdapter(adapter);

        // Add initial rows
        for (int i = 0; i < rowCount; i++) {
            addRow();
        }

        // Button to dynamically add rows
        addButton.setOnClickListener(v -> addRow());

        // Calculate GPA button click listener
        calculateButton.setOnClickListener(v -> calculateGPA());

        // Screenshot button click listener
        screenshotButton.setOnClickListener(v -> takeScreenshot());
    }

    // Method to add a row dynamically
    private void addRow() {
        // Create a new horizontal LinearLayout to hold EditTexts for credits and grades
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Layout params for EditTexts and Spinner
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.25f
        );

        // Spinner for credits
        Spinner creditsSpinner = new Spinner(this);
        creditsSpinner.setLayoutParams(params);
        creditsSpinner.setAdapter(adapter); // Set the same adapter as the one used in the onCreate method
        rowLayout.addView(creditsSpinner);

        // EditText for grade percentage
        EditText gradeEditText = new EditText(this);
        gradeEditText.setLayoutParams(params);
        gradeEditText.setHint("Grade (%)");
        gradeEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        rowLayout.addView(gradeEditText);

        // EditText for module name
        EditText moduleNameEditText = new EditText(this);
        moduleNameEditText.setLayoutParams(params);
        moduleNameEditText.setHint("Module Name");
        rowLayout.addView(moduleNameEditText);

        // Add the new row to the main layout
        linearLayout.addView(rowLayout);
    }

    // Method to calculate GPA
    private void calculateGPA() {
        double totalCredits = 0;
        double totalGradePoints = 0;

        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            if (linearLayout.getChildAt(i) instanceof LinearLayout) {
                LinearLayout rowLayout = (LinearLayout) linearLayout.getChildAt(i);

                Spinner creditsSpinner = (Spinner) rowLayout.getChildAt(0);
                EditText gradeEditText = (EditText) rowLayout.getChildAt(1);
                EditText moduleNameEditText = (EditText) rowLayout.getChildAt(2);

                int credits = Integer.parseInt(creditsSpinner.getSelectedItem().toString());
                String gradeStr = gradeEditText.getText().toString();
                String moduleName = moduleNameEditText.getText().toString();

                if (!gradeStr.isEmpty() && !moduleName.isEmpty()) {
                    int grade = Integer.parseInt(gradeStr);
                    double gradePoint = convertToScale(grade);

                    if (credits == 10 || credits == 5) {
                        totalCredits += credits;
                        totalGradePoints += (credits * gradePoint);
                    } else {
                        Toast.makeText(this, "Credits should be 10 or 5", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        if (totalCredits > 0) {
            double gpa = totalGradePoints / totalCredits;
            resultTextView.setText(String.format("GPA: %.2f", gpa));
            screenshotButton.setVisibility(View.VISIBLE);
        } else {
            resultTextView.setText("Enter values to calculate GPA");
        }
    }

    private double convertToScale(int grade) {
        // Conversion logic for grading scale, for example (you may adjust as per your grading system)
        return grade / 20.0;
    }


    // Method to take a screenshot
    private void takeScreenshot() {
        // Define the file path and name where the screenshot will be saved
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "screenshot.png");

        // Execute the AsyncTask to take a screenshot in the background
        new ScreenshotTask(file).execute();
    }

    private class ScreenshotTask extends AsyncTask<Void, Void, Boolean> {
        private final File file;

        public ScreenshotTask(File file) {
            this.file = file;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Get the root view
                View rootView = getWindow().getDecorView().getRootView();

                // Enable drawing cache to get the bitmap of the view
                rootView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
                rootView.setDrawingCacheEnabled(false);

                // Write the bitmap to the specified file
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Show a toast message indicating successful screenshot capture
                Toast.makeText(MainActivity.this, "Screenshot saved", Toast.LENGTH_SHORT).show();
            } else {
                // Show a toast message if there was an error saving the screenshot
                Toast.makeText(MainActivity.this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
            }
        }

    }
}