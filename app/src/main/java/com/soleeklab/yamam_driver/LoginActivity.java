package com.soleeklab.yamam_driver;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.inpt_email)TextInputLayout usernameInpt;
    @BindView(R.id.inpt_password)TextInputLayout idInpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }
    public void loginBtn(View view){
        if (validate (usernameInpt.getEditText().getText().toString(),idInpt.getEditText().getText().toString())){
            Intent intent = new Intent(this,HomeActivity.class);
            intent.putExtra("name",usernameInpt.getEditText().getText().toString());
            startActivity(intent);
            finish();
        }
    }

    private boolean validate(String s, String s1) {
        usernameInpt.setErrorEnabled(false);
        idInpt.setErrorEnabled(false);
        if (s==null||s.length()<3){
            usernameInpt.setError("please enter valid username");
            return false;
        }else if (s1==null||s1.length()<3){
            idInpt.setError("please enter valid id");
            return false;
        }else return true;
    }
}
