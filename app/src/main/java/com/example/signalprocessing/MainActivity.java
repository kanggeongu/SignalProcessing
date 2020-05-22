package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase= FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private GoogleSignInClient mGoogleSingInClient;
    private static final int RC_SIGN_IN=9001;
    private SignInButton glogin;
    private Button signup,signin,find,verify;
    private EditText editemail,editpw;
    private boolean isnewAccount=true;
    private SharedPreferences auto;
    private String loginID,loginPW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glogin=(SignInButton)findViewById(R.id.share_btn_glogin);
        signup=(Button)findViewById(R.id.share_btn_signup);
        signin=(Button)findViewById(R.id.share_btn_signin);
        editemail=(EditText)findViewById(R.id.share_edit_email);
        editpw=(EditText)findViewById(R.id.share_edit_pw);
        find=(Button)findViewById(R.id.share_btn_find);
        verify=(Button)findViewById(R.id.share_btn_verify);

        auto=getSharedPreferences("autologin", Activity.MODE_PRIVATE);
        loginID = auto.getString("inputId", null);
        loginPW = auto.getString("inputPW", null);

        if(mAuth.getCurrentUser()!=null){
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }

        else if (loginID != null && loginPW != null) {
            login(loginID, loginPW);
        }

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSingInClient= GoogleSignIn.getClient(MainActivity.this,gso);

        signup.setOnClickListener(this);
        glogin.setOnClickListener(this);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=editemail.getText().toString().trim();
                String pw=editpw.getText().toString().trim();
                if (email.equals("")) {
                    showToast("이메일을 입력하세요");
                } else if (pw.equals("")) {
                    showToast("패스워드를 입력하세요");
                } else {
                    login(email, pw);
                }
            }
        });

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,FindpwActivity.class);
                startActivity(intent);
                finish();
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,VerifyEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.share_btn_signup:
                signupUI();
                break;
            case R.id.share_btn_glogin:
                gsignIn();
                break;
        }
    }

    private void login(final String email,final String pw){
        mAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor autoLogin = auto.edit();
                            autoLogin.putString("inputId", email);
                            autoLogin.putString("inputPW", pw);
                            autoLogin.commit();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUIwithEmailcheck(user);
                        } else {
                            showToast("로그인 오류입니다");
                            updateUIwithEmailcheck(null);
                        }
                    }
                });
    }

    private void gsignIn(){
        Intent signInIntent=mGoogleSingInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account=task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email=user.getEmail();
                            checkAccount(email);
                        }
                        else{
                            showToast("로그인 실패");
                            updateUI(null);
                        }
                    }
                });
    }

    private void checkAccount(final String email){
        mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    User userData = snapshot.getValue(User.class);
                    if (userData.getUserEmail().equals(email)) {
                        isnewAccount = false;
                    }
                }
                FirebaseUser user = mAuth.getCurrentUser();
                if(isnewAccount){
                    showToast("신규가입을 환영합니다");
                    updateUIwithName(user);
                }
                else{
                    updateUI(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("인터넷 연결을 확인하세요");
            }
        });
    }

    public void showToast(String contents){
        Toast.makeText(MainActivity.this,contents,Toast.LENGTH_SHORT).show();
    }

    public void signupUI(){
        Intent intent = new Intent(MainActivity.this,SignupActivity.class);
        startActivity(intent);
        finish();
    }

    public void updateUI(FirebaseUser user){
        if(user!=null) {
            Intent intent = new Intent(MainActivity.this,FreeBoardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void updateUIwithName(FirebaseUser user){
        User userdata=new User(user.getDisplayName(),user.getEmail());
        Intent intent =new Intent(MainActivity.this,SetNameActivity.class);
        intent.putExtra("userInformation",userdata);
        startActivity(intent);
        finish();
    }

    public void updateUIwithEmailcheck(FirebaseUser user){
        if(user!=null){
            boolean emailVerified=user.isEmailVerified();
            if(emailVerified){
                Intent intent=new Intent(MainActivity.this,FreeBoardActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                mAuth.signOut();
                showToast("이메일 인증 후 사용할 수 있습니다");
            }
        }
    }

}
