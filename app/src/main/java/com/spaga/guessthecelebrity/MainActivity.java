package com.spaga.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebImages;
    ArrayList<String> celebNames;
    int chosenCeleb, locationOfCorrectAns;
    int numOfOptions = 4;
    String[] answers = new String[numOfOptions];
    ImageView imageView;
    Button button0, button1, button2, button3;

    public class ImageDownloader extends  AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public void chooseAnswer(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAns)))
            Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Wrong! It was" + celebNames.get(chosenCeleb),
                    Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebImages = new ArrayList<>();
        celebNames = new ArrayList<>();
        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        DownloadTask task = new DownloadTask();
        String result;

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);
            while (m.find()) {
                celebImages.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);
            while (m.find()) {
                celebNames.add(m.group(1));
            }

            Random random = new Random();
            chosenCeleb = random.nextInt(celebImages.size());

            ImageDownloader imageTask = new ImageDownloader();
            Bitmap bitmap;

            bitmap = imageTask.execute(celebImages.get(chosenCeleb)).get();
            imageView.setImageBitmap(bitmap);

            locationOfCorrectAns = random.nextInt(numOfOptions);
            int incorrectAnsLocation;

            for (int i = 0; i < numOfOptions; i++) {
                if (i == locationOfCorrectAns)
                    answers[i] = celebNames.get(chosenCeleb);
                else {
                    incorrectAnsLocation = random.nextInt(celebImages.size());

                    while (incorrectAnsLocation == chosenCeleb) {
                        incorrectAnsLocation = random.nextInt(celebImages.size());
                    }
                    answers[i] = celebNames.get(incorrectAnsLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
