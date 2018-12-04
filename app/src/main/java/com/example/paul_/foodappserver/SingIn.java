package com.example.paul_.foodappserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.paul_.foodappserver.Common.Common;
import com.example.paul_.foodappserver.Model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.hoang8f.widget.FButton;

public class SingIn extends AppCompatActivity {

    EditText editPhone,editPassword;
    Button btnSingIn;

    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);
        editPassword = (MaterialEditText)findViewById(R.id.editPassword);
        editPhone = (MaterialEditText)findViewById(R.id.editPhone);
        btnSingIn = (FButton)findViewById(R.id.btnSignIn);

        //Initializez Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseDatabase.getInstance();
        users = db.getReference("User");

        //Pentru cand apas pe login button
        btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser(editPhone.getText().toString(),editPassword.getText().toString());
            }
        });


    }

    private void signInUser(final String phone, String password) {
        final ProgressDialog mDialog = new ProgressDialog(SingIn.this);
        mDialog.setMessage("Va rugam sa asteptati");
        mDialog.show();

        final String localPhone = phone;
        final String localPassword = password;
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(localPhone).exists())
                    {
                        mDialog.dismiss();
                        User user = dataSnapshot.child(localPhone).getValue(User.class);
                        user.setPhone(localPhone);
                        if(Boolean.parseBoolean(user.getIsstaff()))//verific daca e staff
                        {
                            if(user.getPassword().equals(localPassword))
                            {
                                // Login cu success
                                Intent login = new Intent(SingIn.this,Home1.class);
                                Common.currentUser = user;
                                startActivity(login);
                                finish();
                            }
                            else Toast.makeText(SingIn.this,"Parola este gresita",Toast.LENGTH_SHORT).show();
                        }
                        else Toast.makeText(SingIn.this,"Folositi un cont de administrator!",Toast.LENGTH_SHORT).show();
                    }
                    else
                        {
                                mDialog.dismiss();
                                Toast.makeText(SingIn.this,"Cont inexistent",Toast.LENGTH_SHORT).show();
                        }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
