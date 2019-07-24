package com.fundamentals.notificationjobservice;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 42;

    private JobScheduler mJobScheduler;

    RadioGroup mNetworkOptions;
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNetworkOptions = findViewById(R.id.networkOptions);

        mDeviceIdleSwitch = findViewById(R.id.idleSwitch);
        mDeviceChargingSwitch = findViewById(R.id.chargingSwitch);

        mSeekBar = findViewById(R.id.seekBar);
        final TextView deadlineSeconds = findViewById(R.id.deadlineSeconds);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0) {
                    deadlineSeconds.setText(getString(R.string.deadline_seconds, progress));
                } else {
                    deadlineSeconds.setText(R.string.not_set);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void scheduleJob(View view) {
        int selectedRadioButtonId = mNetworkOptions.getCheckedRadioButtonId();

        // SOS: This is the default and it means "no internet restriction applied".
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        switch (selectedRadioButtonId) {
            case R.id.noNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.wifiNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        // SOS: The jobInfo must be associated w the service and the job id.
        ComponentName serviceName = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());

        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked())
                .setRequiresCharging(mDeviceChargingSwitch.isChecked());

        // SOS: The deadline will make the job execute in that time even if constraints are not met!
        int deadlineSeconds = mSeekBar.getProgress();
        if (deadlineSeconds > 0) {
            builder.setOverrideDeadline(deadlineSeconds * 1000);
        }

        JobInfo jobInfo = builder.build();

        // SOS: If I try to schedule w/o any constraints, it will crash. A job must have at least 1
        // constraint, otherwise I'd use a Service!
        boolean constraintSet = selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE ||
                mDeviceIdleSwitch.isChecked() || mDeviceChargingSwitch.isChecked() || deadlineSeconds > 0;

        if (constraintSet) {
            mJobScheduler.schedule(jobInfo);

            Toast.makeText(this, "Job Scheduled, job will run when the constraints are met.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please set at least one constraint",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelJobs(View view) {
        if (mJobScheduler != null) {
            mJobScheduler.cancelAll();
            mJobScheduler = null;
            Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
