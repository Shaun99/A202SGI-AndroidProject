package com.example.user_pc.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_pc.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    //Declare and initialise variables
    private CircleImageView profileImageView;
    private EditText fullNameEditText, userPasswordEditText, addressEditText;
    private TextView profileChangeTextBtn,  closeTextBtn, saveTextButton;

    private StorageReference storageProfilePrictureRef;
    private String checker = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Create a storage reference
        storageProfilePrictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");
        //Locate the view having id from the layout resource file
        profileImageView = (CircleImageView) findViewById(R.id.settings_profile_image);
        fullNameEditText = (EditText) findViewById(R.id.settings_full_name);
        userPasswordEditText = (EditText) findViewById(R.id.settings_password);
        addressEditText = (EditText) findViewById(R.id.settings_address);
        closeTextBtn = (TextView) findViewById(R.id.close_settings_btn);
        saveTextButton = (TextView) findViewById(R.id.update_account_settings_btn);

        //Close button function
        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                close();
            }
        });

        //Update button function
        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                {   //save user text info
                    userInfoSaved();
                }
            }
        });

    }

    private void close() {
        Toast.makeText(this, "No changes were made", Toast.LENGTH_LONG).show();
        finish();
    }

    private void userInfoSaved()
    {   //Prompt error message if any of the required field is blank
        if (TextUtils.isEmpty(fullNameEditText.getText().toString()))
        {
            Toast.makeText(this, "Please fill in the name.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userPasswordEditText.getText().toString()))
        {
            Toast.makeText(this, "Please fill in the password.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(addressEditText.getText().toString()))
        {
            Toast.makeText(this, "Please fill in the address.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            updateOnlyUserInfo();
        }
    }

    private void updateOnlyUserInfo()
    {   //Create database reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        //Store each value inside each of the variable
        HashMap<String, Object> userMap = new HashMap<>();
        userMap. put("name", fullNameEditText.getText().toString());
        userMap. put("address", addressEditText.getText().toString());
        userMap. put("password", userPasswordEditText.getText().toString());
        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
        //Redirect user to main page and prompt successful message
        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
        Toast.makeText(SettingsActivity.this, "Profile Info update successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

}

