package com.example.nott;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;


public class NotTFragment extends Fragment {
    SQLiteDatabase sqLiteDatabase;
    Bitmap selectedImage;
    ImageView imageView;
    TextView dateTxt,timeTxt;
    EditText editText;
    Button date,time,back,saved;
    int day,monthh,years;
    int sayac = 0;

    public  Bitmap makeSmallerImage(Bitmap image,int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio =(float) width / (float)height;

        if(bitmapRatio>1)
        {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }
        else
        {
            height = maximumSize;
            width = (int)(height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        saved = view.findViewById(R.id.saved);
        saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Bitmap smallImage= makeSmallerImage(selectedImage,150);

                ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
                byte[] byteArray = outputStream.toByteArray();

                try {
                    sqLiteDatabase = getActivity().openOrCreateDatabase("Notlar", Context.MODE_PRIVATE,null);
                    sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notlar(id INTEGER PRIMARY KEY,notText VARCHAR)");
                }catch (Exception e){

                }


            }
        });



        back = view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDirections action = NotTFragmentDirections.actionNotTFragmentToFirstFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });


        date = view.findViewById(R.id.date);
        time = view.findViewById(R.id.time);
        timeTxt = view.findViewById(R.id.timeTxt);
        dateTxt= view.findViewById(R.id.dateTxt);


    time.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final Calendar takvim = Calendar.getInstance();
            int saat = takvim.get(Calendar.HOUR_OF_DAY);
            int dakika = takvim.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            timeTxt.setText(hourOfDay + ":" + minute);

                        }
                    }, saat, dakika, true);


            timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, "Seç", timePickerDialog);
            timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "İptal", timePickerDialog);
            timePickerDialog.show();
        }

    });


    date.setOnClickListener(new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            years = calendar.get(Calendar.YEAR);
            monthh = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    month += 1;
                    dateTxt.setText(day + "/" + month + "/" + year);


                }
            }, years, monthh, day);
            datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Seç", datePickerDialog);

            datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "İptal", datePickerDialog);
            datePickerDialog.show();

        }
    });


        imageView = view.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                else
                { Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,2);

                }
            }


        });
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,2);

            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==2 && resultCode == RESULT_OK && data != null){
            Uri imageData = data.getData();
            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoder.Source source= ImageDecoder.createSource(getActivity().getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                } else
                {       selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageData);
                    imageView.setImageBitmap(selectedImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    public NotTFragment() {
        // Required empty public constructor
    }


    public static NotTFragment newInstance(String param1, String param2) {
        NotTFragment fragment = new NotTFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_not_t, container, false);
    }
}