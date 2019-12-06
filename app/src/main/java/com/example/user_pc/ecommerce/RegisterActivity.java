package com.example.user_pc.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    //Declare and initialise variables
    private Button CreateAccountButton, BackButton;
    private EditText InputName, InputPhoneNumber, InputPassword, InputAddress;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Locate the view having id from the layout resource file
        CreateAccountButton = (Button) findViewById(R.id.register_btn);
        InputName = (EditText) findViewById(R.id.register_username_input);
        InputPassword = (EditText) findViewById(R.id.register_password_input);
        InputPhoneNumber = (EditText) findViewById(R.id.register_phone_number_input);
        InputAddress = (EditText) findViewById(R.id.register_address_input);
        loadingBar = new ProgressDialog(this);

        //To specify an action y calling the function when the button is pressed
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateAccount();
            }
        });
        //Redirect user back to Main page
        BackButton = (Button) findViewById(R.id.back_btn);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void CreateAccount()
    {
        //Get the inputs from user
        String name = InputName.getText().toString();
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();
        String address = InputAddress.getText().toString();
        //Prompt message if any of the field is blank
        if (TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Please fill in the name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone))
        {
            Toast.makeText(this, "Please fill in the phone number", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please fill in the password", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(address))
        {
            Toast.makeText(this, "Please fill in the address", Toast.LENGTH_SHORT).show();
        }
        else
        {   //Display the loading bar to user while checking for any existing phone no.
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            //Passing the values to another function
            ValidatephoneNumber(name, phone, password, address);
        }
    }

    private void ValidatephoneNumber(final String name, final String phone, final String password, final
                                     String address)
    {
        //Create a database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        //Add a listener for a single change in the data at this location
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //Check if phone number is not yet exist, user is able to create new account
                if (!(dataSnapshot.child("Users").child(phone).exists()))
                {

                    //Get the key and value in pairs to stored inside the firebase database
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    //Store each value inside each of the variable
                    userdataMap.put("phone", phone);
                    userdataMap.put("password", password);
                    userdataMap.put("name", name);
                    userdataMap.put("address", address);

                    //Create a parent node for all the users
                    RootRef.child("Users").child(phone).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {   //Display successful message and dismiss the loading bar
                                        Toast.makeText(RegisterActivity.this, "Congratulations, your account has been created.", Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                        //Redirect the user to login page once the account has created
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                    else
                                    {   //Display error message if sudden connection lost and dismiss the loading bar
                                        loadingBar.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Network Error: Please try again in awhile.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else
                {  //Prompt a message informing the user that the phone no. is already existed
                    Toast.makeText(RegisterActivity.this, "The number for " + phone + " has already exists.", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Please try again using another phone number.", Toast.LENGTH_SHORT).show();
                    //Redirect the user back to Main Activity
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
