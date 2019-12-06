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
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_pc.ecommerce.Model.Users;
import com.example.user_pc.ecommerce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    //Declare and initialise variables
    private EditText InputPhoneNumber, InputPassword;
    private Button LoginButton ,BackButton;
    private ProgressDialog loadingBar;
    private TextView AdminLink, NotAdminLink;

    //Create Parent node to contain Admins and Users as child node
    private String parentDbName = "Users";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Locate the view having id from the layout resource file
        LoginButton = (Button) findViewById(R.id.login_btn);
        InputPassword = (EditText) findViewById(R.id.login_password_input);
        InputPhoneNumber = (EditText) findViewById(R.id.login_phone_number_input);
        AdminLink = (TextView) findViewById(R.id.admin_panel_link);
        NotAdminLink = (TextView) findViewById(R.id.not_admin_panel_link);
        loadingBar = new ProgressDialog(this);

        //To specify an action by calling the function when the button is pressed
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                LoginUser();
            }
        });

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {   //Change the login and link to admins
                LoginButton.setText("Login as Admin");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";
            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {   //Change the login and link to users
                LoginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";
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

    private void LoginUser(){
        //Get the inputs from user
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();
        //Prompt message if any of the field is blank
        if (TextUtils.isEmpty(phone))
        {
            Toast.makeText(this, "Please fill in the phone number", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please fill in the password", Toast.LENGTH_SHORT).show();
        }
        else
        {   //Display the loading bar to user while verifying user credentials
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            //Passing the values to another function
            AllowAccessToAccount(phone, password);
        }
    }

    private void AllowAccessToAccount(final String phone, final String password) {
        //Create a database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        //Add a listener for a single change in the data at this location
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {   //Check whether the unique key (phone) which contain all the info of users and admins is exist
                if (dataSnapshot.child(parentDbName).child(phone).exists())
                {   //Get the value and pass it to Users class
                    Users usersData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);
                    //Retrieve the data and check for validation
                    if (usersData.getPhone().equals(phone)) //Check phone input
                    {
                        if (usersData.getPassword().equals(password)) //Check password input
                        {
                            //Check the credentials whether it belong to admins to users
                            if (parentDbName.equals("Admins"))
                            {   //Prompt a successful message to admins and redirect them to admin page
                                Toast.makeText(LoginActivity.this, "Welcome Admin, Logged in Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                startActivity(intent);
                            }
                            else if (parentDbName.equals("Users"))
                            {   //Prompt a successful message to users and redirect them to home page
                                Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Prevalent.currentOnlineUser = usersData;
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        }
                        else
                        {   //Prompt error message if password does not match
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password is incorrect.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else
                {   //Prompt error message if the account does not exist yet
                    Toast.makeText(LoginActivity.this, "Account with this " + phone + " number do not exists. Please create an account first.", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
