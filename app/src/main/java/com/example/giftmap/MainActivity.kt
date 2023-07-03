package com.example.giftmap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var email: String
    lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        var joinBTN: Button = findViewById(R.id.joinBTN)
        var loginBTN: Button = findViewById(R.id.loginBTN)
        var idET: EditText = findViewById(R.id.idET)
        var pwET: EditText = findViewById(R.id.pwET)

        loginBTN.setOnClickListener{
            email = idET.text.toString()
            password = pwET.text.toString()
            auth.signInWithEmailAndPassword(email,password) // 로그인
                .addOnCompleteListener {
                        result->
                    if(result.isSuccessful){
                        var intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
                }
            }
        }
        joinBTN.setOnClickListener{
            email = idET.text.toString()
            password = pwET.text.toString()
            auth.createUserWithEmailAndPassword(email,password) // 회원 가입
                .addOnCompleteListener {
                        result ->
                    if(result.isSuccessful){
                        Toast.makeText(this,"회원가입 완료",Toast.LENGTH_SHORT).show()
                        if(auth.currentUser!=null){
                            var intent = Intent(this, MapActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    else if(result.exception?.message.isNullOrEmpty()){
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth!!.currentUser
        updateUI(currentUser)
    }

    // 로그인 감지
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent: Intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}