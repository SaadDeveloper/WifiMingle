package com.wifimingle.Utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.wifimingle.activity.ActivityMain.FORMAT_DATE;

public class DialogDatePicker {

    private String title = null;
    private Context context = null;
    private EditText editText = null;
    private Date datePrevious = null;
    private DatePicker datePicker = null;
    private CalendarView calendarView = null;

    public DialogDatePicker(Context context, EditText editText, String title) {
        this.editText = editText;
        this.context = context;
        this.editText.setInputType(InputType.TYPE_NULL);
        this.editText.setClickable(false);
        this.editText.setFocusable(false);
        this.editText.setFocusableInTouchMode(false);
        this.title = title;
    }

    public DatePicker showDateDialog() {
        try {
            Calendar calendarInstance = Calendar.getInstance();
            String datePreviousText = editText.getText().toString();
            datePrevious = null;
            if (datePreviousText.length() > 0) {
                datePrevious = (new SimpleDateFormat(FORMAT_DATE, Locale.ENGLISH)).parse(datePreviousText);
            } else {
                datePrevious = new Date();
            }
            int year = datePrevious.getYear() + 1900;
            int month = datePrevious.getMonth();
            int dayOfMonth = datePrevious.getDate();
            DatePickerDialog dialogDatePicker = new DatePickerDialog(context, /*DatePickerDialog.THEME_HOLO_LIGHT, */null, year, month, dayOfMonth) {

                @Override
                public void onDateChanged(DatePicker view, int year, int month, int day) {
                    super.onDateChanged(view, year, month, day);
                    setTitle(title);
                }
            };
            datePicker = dialogDatePicker.getDatePicker();

            datePicker.init(year, month, dayOfMonth, new DatePicker.OnDateChangedListener() {

                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                }
            });
            //datePicker.setMinDate(calendarInstance.getTimeInMillis());
            datePicker.setMaxDate(calendarInstance.getTimeInMillis());
            datePicker.updateDate(year, month, dayOfMonth);

            dialogDatePicker.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE, Locale.ENGLISH);
                    String date = String.valueOf(datePicker.getDayOfMonth()) + "-" + String.valueOf((datePicker.getMonth() + 1)) +
                    "-" + String.valueOf(datePicker.getYear()) ;
                    //String date = dateFormat.format(new Date(datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear() - 1900));

//                    if (title.toLowerCase().contains("end")) {
//                        MainContainer.datePickerEndDate = datePicker;
//                    } else {
//                        MainContainer.datePicker = datePicker;
//                    }
                    editText.setText(date);
                }
            });
            dialogDatePicker.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialogDatePicker.setTitle(title);
            dialogDatePicker.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datePicker;
    }
}