package activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventHandling();
    }

    private void eventHandling() {
        binding.activityLoginTvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));
    }
}