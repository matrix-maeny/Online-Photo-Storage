package com.matrix_maeny.onlinephotostorage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;

import com.matrix_maeny.onlinephotostorage.databinding.ActivityImageBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ImageActivity extends AppCompatActivity {

    private ActivityImageBinding binding;
    private ScaleGestureDetector gestureDetector;
    private float scaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        gestureDetector = new ScaleGestureDetector(ImageActivity.this, new ScaleListener());

        String imageUrl = getIntent().getStringExtra("url");

        if (imageUrl != null){
            Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder).into(binding.selectedImageV);
        }

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scaleFactor *= gestureDetector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));

            binding.selectedImageV.setScaleX(scaleFactor);
            binding.selectedImageV.setScaleY(scaleFactor);
            return true;
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}