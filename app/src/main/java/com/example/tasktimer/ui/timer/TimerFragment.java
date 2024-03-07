package com.example.tasktimer.ui.timer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tasktimer.R;
import com.example.tasktimer.databinding.FragmentTimerBinding;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTimerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        CheckBox checkBox = root.findViewById(R.id.checkBox);
        checkBox.setFocusable(false);
        checkBox.setClickable(false);

        root.findViewById(R.id.deleteButton).setVisibility(View.GONE);

        return root;
    }

    @Override 
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}