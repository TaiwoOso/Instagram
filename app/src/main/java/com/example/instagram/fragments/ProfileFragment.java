package com.example.instagram.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.adapters.PostsAdapter;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 76;
    public String photoFileName = "photo.jpg";

    private TextView tvUsername;
    private ImageView ivProfileImage;
    private RecyclerView rvPosts;
    private PostsAdapter adapter;
    private List<Post> userPosts;
    private ImageButton ibAddProfileImage;
    private File photoFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvUsername = view.findViewById(R.id.tvUsername);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        rvPosts = view.findViewById(R.id.rvPosts);
        ibAddProfileImage = view.findViewById(R.id.ibAddProfileImage);

        // Set the username
        ParseUser currentUser = ParseUser.getCurrentUser();
        tvUsername.setText(currentUser.getUsername());

        // Set the profile image
        ParseFile image = currentUser.getParseFile(User.KEY_PROFILE_IMAGE);
        if (image != null) {
            Glide.with(getContext())
                    .load(image.getUrl())
                    .into(ivProfileImage);
        } else {
            Glide.with(getContext())
                    .load(R.drawable.profile_placeholder)
                    .into(ivProfileImage);
        }

        // Set up recycler view
        userPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), userPosts, R.layout.item_post_profile);
        rvPosts.setAdapter(adapter);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        rvPosts.setLayoutManager(layoutManager);

        // Fetch posts by current user
        queryPosts();


        // Add profile image
        ibAddProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the photo
                launchCamera();
                // Save the photo to Parse database
                saveProfileImage(photoFile);
            }
        });
    }



    private void saveProfileImage(File photoFile) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put(User.KEY_PROFILE_IMAGE, new ParseFile(photoFile));
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving profile image!", e);
                    Toast.makeText(getContext(), "Error while saving profile image!", Toast.LENGTH_SHORT).show();
                    // Load back previous image
                    Glide.with(getContext())
                            .load(currentUser.getParseFile(User.KEY_PROFILE_IMAGE))
                            .placeholder(R.drawable.profile_placeholder)
                            .into(ivProfileImage);
                } else {
                    Log.i(TAG, "Profile image was saved!");
                    Toast.makeText(getContext(), "Image saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void queryPosts() {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_USER);
        // only by currently signed in user
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // error checking
                if (e != null) {
                    Log.e(TAG, "Issue with retrieving posts", e);
                    return;
                }
                // log posts retrieved
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() +", username: " + post.getUser().getUsername());
                }
                // save received posts to list and notify adapter of new data
                adapter.addAll(posts);
            }
        });
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                Glide.with(getContext())
                        .load(takenImage)
                        .into(ivProfileImage);
//                ivProfileImage.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
}
