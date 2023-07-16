package activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.example.chat.R;
import com.example.chat.databinding.ActivityLoginBinding;
import com.example.chat.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventHandling();
    }

    private void eventHandling() {
        binding.activityRegisterBtLogin.setOnClickListener(v -> onBackPressed());
    }
}