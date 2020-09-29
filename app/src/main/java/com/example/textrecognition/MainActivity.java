package com.example.textrecognition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    TextView textView;
    public static final int REQUEST_CODE = 101; // request code which i required to be passed in the startActivityforResultMethod() so that
    //we can compare that it was the camera one intent not the gallery one
    public static final int REQUEST_CODE2 = 102;
    // this one is for the gallery intent
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
    }
 //this is the click listener for the button
    public void captureImage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //creates an intent of a specific type which can be open by camera application
        if(intent.resolveActivity(getPackageManager()) != null) { //checking if the intent returned some value or if there is a app present for camera
            startActivityForResult(intent, REQUEST_CODE); // starting the activity and waiting for result
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // result
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE){ // matching the request code
            Bundle bundle = data.getExtras(); // taking the information of the image from the intent
            Bitmap bitmap = (Bitmap) bundle.get("data");  // converting the information into bitmap so that it could be shown on the screen
            imageView.setImageBitmap(bitmap); //setting it to the view screen
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap); //taking the firebaseVision image form a bitmap
            FirebaseVision firebaseVision = FirebaseVision.getInstance(); //creating an instance of the firebase vision - mandatory
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer(); // calling the text recognizer

            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage); // creating a task which will either succeed or fail it is async

            task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {   //if the process was succesfull
                    textView.setText(firebaseVisionText.getText()); // set the text

                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) { // if the process fails
                    Toast.makeText(getApplicationContext(),"failed to show",Toast.LENGTH_LONG).show();

                }
            });
        }
        if(requestCode == REQUEST_CODE2){ // if the intent was for the gallery
            imageView.setImageURI(data.getData()); // since the data is in the form if path we use imageuri to process the image from the data and set it to the image view
            FirebaseVisionImage firebaseVisionImage = null; //same as the camera one
            try { // try catch block is because if the image was not selected from the gallery
                firebaseVisionImage = FirebaseVisionImage.fromFilePath(getApplicationContext(),data.getData()); //Creates a FirebaseVisionImage from a local image file Uri.
            }
            catch (Exception e){

            }
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    textView.setText(firebaseVisionText.getText());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"FAILED TO GET IMAGE FROM GALLERY",Toast.LENGTH_LONG).show();
                }
            });

        }

    }

    public void selectImage(View view) { // for gallery on click button
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE2);
        }

    }
}