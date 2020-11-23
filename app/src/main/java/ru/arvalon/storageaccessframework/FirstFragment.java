package ru.arvalon.storageaccessframework;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static ru.arvalon.storageaccessframework.MainActivity.TAG;

public class FirstFragment extends Fragment {

    private static final int CREATE_DIR_REQUEST_CODE = 42;
    private static final int READ_REQUEST_CODE = 43;

    private static final String DIR_NAME = "ag_workspace";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG,"FirstFragment onCreateView");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG,"FirstFragment onViewCreated");

        view.findViewById(R.id.button_first).setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));

        // Кнопка Create root folder (SAF)
        view.findViewById(R.id.create_root_folder_saf_btn).setOnClickListener(v -> createRootDirSaf());

        // Кнопка "Select file"
        view.findViewById(R.id.create_public_folder_saf_btn).setOnClickListener(v -> createPublicDirSaf());

        // Кнопка "Create public folder (Classic)"
        view.findViewById(R.id.create_public_folder_classic_btn).setOnClickListener(v -> createPublicDirClassic());

        // Кнопка "Create external folder (Classic)"
        view.findViewById(R.id.create_external_folder_classic_btn).setOnClickListener(v -> createExternalDirClassic());

        // Кнопка "Get extSdCard path"
        view.findViewById(R.id.get_extsdcard_path_btb).setOnClickListener(v -> getExtSDCardPath());

        // Кнопка "Select file"
        view.findViewById(R.id.perform_file_search_btb).setOnClickListener(v -> performFileSearch());
    }

    private void createRootDirSaf() {

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, DIR_NAME);
        startActivityForResult(intent, CREATE_DIR_REQUEST_CODE);
    }

    private void createPublicDirSaf() {
        // TODO: 13.11.2020 попробовать создать папку в какой-нибудь публичном каталоге
        Log.d(TAG,"попробовать создать папку в какой-нибудь публичном каталоге");
    }

    private void createPublicDirClassic() {

        File docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        if (docDir.exists() && docDir.isDirectory()){
            Log.d(TAG, "Папка для документов в External Storage существует");

            File ag_dir = new File (docDir+"/"+DIR_NAME);

            if (!ag_dir.exists()){
                Log.d(TAG,"Создание папки AG: "+ag_dir.mkdir());
            }else {
                Log.d(TAG,"Папка AG уже существует");
            }
        }else {
            //Log.d(TAG, "Папка для документов в External Storage НЕ существует");
            Log.d(TAG, "Папка для документов в External Storage НЕ существует, создаём: "+docDir.toString());
            //File newDir = new File(docDir.toString());
            File newDir = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOCUMENTS);

            Log.d(TAG,"Результат создания: "+newDir.mkdir());
        }
    }

    private void createExternalDirClassic() {

        File docDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if (docDir.exists() && docDir.isDirectory()){
            Log.d(TAG, "Папка для документов в External Storage существует");

            File ag_dir = new File (docDir, DIR_NAME);

            if (!ag_dir.exists()){
                Log.d(TAG,"Создание папки AG: "+ag_dir.mkdir());
            }else {
                Log.d(TAG,"Папка AG уже существует");
            }
        }else {
            //Log.d(TAG, "Папка для документов в External Storage НЕ существует");
            Log.d(TAG, "Папка для документов в External Storage НЕ существует, создаём: "+docDir.toString());
            //File newDir = new File(docDir.toString());
            //File newDir = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOCUMENTS);

            Log.d(TAG,"Результат создания: "+docDir.mkdir());
        }
    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void getExtSDCardPath() {
        Log.d(TAG,"Путь к внешней монтируемой SD-карточке: "+getSDcardPath());
    }

    /** Функция определяет путь до внешней извлекаемой карты наподобие /storage/extSdCard */
    private String getSDcardPath() {

        String exts = Environment.getExternalStorageDirectory().getPath();
        String sdCardPath = null;

        try {

            FileReader fr = new FileReader(new File("/proc/mounts"));
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("secure") || line.contains("asec"))
                    continue;
                if (line.contains("fat")) {
                    String[] pars = line.split("\\s");
                    if (pars.length < 2)
                        continue;
                    if (pars[1].equals(exts))
                        continue;
                    sdCardPath = pars[1];
                    break;
                }
            }

            fr.close();
            br.close();
            return sdCardPath;

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Чтение системных файлов: " + e.getMessage());
        }

        return sdCardPath;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        Log.d(TAG,"requestCode "+requestCode+", resultCode "+resultCode);

        if (resultCode == Activity.RESULT_OK){

            Uri uri;

            switch (requestCode){

                case READ_REQUEST_CODE:

                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().

                if (data != null) {

                    uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());
                }
                break;

                case CREATE_DIR_REQUEST_CODE:

                    if (data != null) {
                        uri = data.getData();
                        Log.d(TAG, "Uri папка: " + uri.toString());
                    }
            }
        }

        //super.onActivityResult(requestCode, resultCode, data);
    }
}