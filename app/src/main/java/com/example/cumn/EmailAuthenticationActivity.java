package com.example.cumn;

import static android.content.ContentValues.TAG;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class EmailAuthenticationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button signInButton;
    private Button signUpButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView forgotPasswordTextView;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_authentication);
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        signInButton = findViewById(R.id.sign_in_button);
        signUpButton = findViewById(R.id.sign_up_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password_text_view);

        signInButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                if (!isValidEmail(email)) {
                    emailEditText.setError("Please enter a valid email address.");
                    emailEditText.requestFocus();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                startActivity(new Intent(EmailAuthenticationActivity.this, MainActivity.class));
                                finish();
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    emailEditText.setError("This account does not exist.");
                                    emailEditText.requestFocus();
                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    passwordEditText.setError("Incorrect password.");
                                    passwordEditText.requestFocus();
                                } else {
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(EmailAuthenticationActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                if (email.isEmpty()) {
                    if (!isValidEmail(email)) {
                        emailEditText.setError("Please enter a valid email address.");
                        emailEditText.requestFocus();
                        return;
                    }
                    emailEditText.setError("Please enter your email.");
                    emailEditText.requestFocus();
                } else {
                    if (!isValidEmail(email)) {
                        emailEditText.setError("Please enter a valid email address.");
                        emailEditText.requestFocus();
                        return;
                    }
                    passwordEditText.setError("Please enter your password.");
                    passwordEditText.requestFocus();
                }
            }
        });


        signUpButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {

                if (!isValidEmail(email)) {
                    emailEditText.setError("Please enter a valid email address.");
                    emailEditText.requestFocus();
                    return;
                }
                if (password.length() < 6) {
                    passwordEditText.setError("Password must be at least 6 characters long.");
                    passwordEditText.requestFocus();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                startActivity(new Intent(EmailAuthenticationActivity.this, MainActivity.class));
                                finish();
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    emailEditText.setError("This email is already in use.");
                                    emailEditText.requestFocus();
                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(EmailAuthenticationActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                if (email.isEmpty()) {
                    if (!isValidEmail(email)) {
                        emailEditText.setError("Please enter a valid email address.");
                        emailEditText.requestFocus();
                        return;
                    }
                    emailEditText.setError("Please enter your email.");
                    emailEditText.requestFocus();
                } else {
                    passwordEditText.setError("Please enter your password.");
                    passwordEditText.requestFocus();
                    if (!isValidEmail(email)) {
                        emailEditText.setError("Please enter a valid email address.");
                        emailEditText.requestFocus();
                        return;
                    }
                }
            }
        });

        forgotPasswordTextView.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();

            if (!email.isEmpty()) {
                if (!isValidEmail(email)) {
                    emailEditText.setError("Please enter a valid email address.");
                    emailEditText.requestFocus();
                    return;
                }
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "resetPasswordEmail:success");
                                Toast.makeText(EmailAuthenticationActivity.this, "Password reset email sent.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "resetPasswordEmail:failure", task.getException());
                                Toast.makeText(EmailAuthenticationActivity.this, "Failed to send password reset email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                emailEditText.setError("Please enter your email.");
            }
        });

    }
}
