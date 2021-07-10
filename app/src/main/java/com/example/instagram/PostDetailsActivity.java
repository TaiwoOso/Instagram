package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.models.Post;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.Date;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";

    private TextView tvUsername;
    private TextView tvTimestamp;
    private ImageView ivImage;
    private TextView tvDescription;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        tvUsername = findViewById(R.id.tvUsername);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        ivImage = findViewById(R.id.ivImage);
        tvDescription = findViewById(R.id.tvDescription);

        // Unwrap the post passed via intent
        post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        Log.d(TAG, String.format("Showing details for '%s'", post.getUser().getUsername()));

        // Set the username and description
        tvUsername.setText(post.getUser().getUsername());
        tvDescription.setText(post.getDescription());

        // Set the image
        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(this)
                    .load(image.getUrl())
                    .into(ivImage);
        }

        // Set the timestamp
        Date createdAt = post.getCreatedAt();
        String timeAgo = Post.calculateTimeAgo(createdAt);
        tvTimestamp.setText(timeAgo);
    }
}