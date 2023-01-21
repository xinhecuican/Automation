package tech.xinhecuican.automation.fragment;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.model.Storage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    private ActivityResultLauncher<String> importContact;
    private ActivityResultLauncher<String> exportContact;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        importContact = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            try {
                ParcelFileDescriptor parcelFileDescriptor = requireActivity().getContentResolver().openFileDescriptor(result, "r");
                Storage.instance().load(parcelFileDescriptor);
                Storage.instance().save();
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        exportContact = registerForActivityResult(new ActivityResultContracts.CreateDocument(), result -> {
            try {
                ParcelFileDescriptor pfd = requireActivity().getContentResolver().
                        openFileDescriptor(result, "w");
                Storage.instance().save(pfd);
                pfd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        Button importSettingButton = (Button)view.findViewById(R.id.import_setting_button);
        importSettingButton.setOnClickListener(v->{
            importContact.launch("*/*");
        });
        Button exportSettingButton = (Button)view.findViewById(R.id.export_setting_button);
        exportSettingButton.setOnClickListener(v -> exportContact.launch("save"));
        Button resetSettingButton = (Button)view.findViewById(R.id.reset_settting_button);
        resetSettingButton.setOnClickListener(v->Storage.instance().reset());
        return view;
    }

}