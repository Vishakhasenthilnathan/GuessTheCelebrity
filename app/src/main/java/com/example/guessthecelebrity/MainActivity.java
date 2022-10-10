package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;
    ImageView imageView;
    TextView resultTextView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    Bitmap celebImage;
    Boolean isAnswerShown= false;

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection httpURLConnection = null;
            StringBuilder stringBuilder = new StringBuilder();
            String content = "";

            try {
                //downloading url content
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();

                while (data != -1) {
                    char current = (char) data;
                    stringBuilder.append(current);
                    data = inputStreamReader.read();
                }
                content = stringBuilder.toString();
                return content;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask downloadTask = new DownloadTask();
        String result;
        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        resultTextView = findViewById(R.id.resultTextView);

        try {
            result = downloadTask.execute("https://www.imdb.com/list/ls052283250/").get();

            Pattern pattern = Pattern.compile("src=\"(.*?).jpg\"");
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                celebUrls.add(matcher.group(1) + ".jpg");
            }
            pattern = Pattern.compile("img alt=\"(.*?)\"");
            matcher = pattern.matcher(result);
            while (matcher.find()) {
                celebNames.add(matcher.group(1));
            }

            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newQuestion(){
        try {
            Random random = new Random();
            chosenCeleb = random.nextInt(celebNames.size());

            ImageDownloader imageDownloader = new ImageDownloader();

            celebImage = imageDownloader.execute(celebUrls.get(chosenCeleb)).get();


            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswer;
            for (int i = 0; i < 4; i++) {
                if (locationOfCorrectAnswer == i) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswer = random.nextInt(celebNames.size());
                    while (incorrectAnswer == chosenCeleb) {
                        incorrectAnswer = random.nextInt(celebNames.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswer);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
            imageView.setImageBitmap(celebImage);
            imageView.setVisibility(View.VISIBLE);
            button0.setVisibility(View.VISIBLE);
            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);
            button3.setVisibility(View.VISIBLE);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void VerifyAnswer(View view){
        int selectedAnswer = Integer.parseInt(view.getTag().toString());
        if(selectedAnswer == locationOfCorrectAnswer){
            Log.i("Answer","Correct");
            Toast.makeText(getApplicationContext(), "CORRECT!", Toast.LENGTH_SHORT).show();
            animateAnswerText();
            button0.setVisibility(View.INVISIBLE);
            button1.setVisibility(View.INVISIBLE);
            button2.setVisibility(View.INVISIBLE);
            button3.setVisibility(View.INVISIBLE);
            resultTextView.setText("CORRECT!");

        }else{
            Log.i("Answer","InCorrect");
            animateAnswerText();
            resultTextView.setText("WRONG! IT IS "+ celebNames.get(chosenCeleb));
            Toast.makeText(getApplicationContext(), "WRONG! IT IS "+ celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

        isAnswerShown = true;
        newQuestion();
    }

    private void animateAnswerText() {
        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
        alphaAnim.setStartOffset(3000);                        // start in 5 seconds
        alphaAnim.setDuration(200);
        alphaAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                button0.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                button1.setVisibility(View.INVISIBLE);
                button2.setVisibility(View.INVISIBLE);
                button3.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // make invisible when animation completes, you could also remove the view from the layout
                resultTextView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(celebImage);
                button0.setVisibility(View.VISIBLE);
                button1.setVisibility(View.VISIBLE);
                button2.setVisibility(View.VISIBLE);
                button3.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        resultTextView.setAnimation(alphaAnim);
    }
}