/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Droid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora Droid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Droid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aurora.adroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.activity.IntroActivity;
import com.aurora.adroid.activity.SettingsActivity;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.manager.RepoManager;
import com.aurora.adroid.task.DatabaseTask;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoFragment extends Fragment {

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.repo_list)
    RelativeLayout repoLayout;
    @BindView(R.id.txtLog)
    TextView txtLog;
    @BindView(R.id.btn_sync)
    MaterialButton btnSync;
    @BindView(R.id.progress_layout)
    RelativeLayout progressLayout;
    @BindView(R.id.progress_sync)
    ProgressBar progressBar;
    @BindView(R.id.txt_progress)
    TextView txtProgress;
    @BindView(R.id.txt_status)
    TextView txtStatus;

    private Context context;
    private int count = 0;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        disposable.add(RxBus.get().toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof Event) {
                        Events eventEnum = ((Event) event).getEvent();
                        switch (eventEnum) {
                            case LOG:
                                break;
                            case SYNC_COMPLETED:
                                finalizeSync();
                                break;
                            case NET_DISCONNECTED:
                                break;
                            case SYNC_PROGRESS:
                                updateProgress();
                                break;
                        }
                    }

                    if (event instanceof LogEvent) {
                        String msg = ((LogEvent) event).getMessage();
                        txtLog.append("\n" + msg);
                    }
                }));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repo, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        repoLayout.setOnClickListener(v -> {
            RepoListFragment fragment = new RepoListFragment();
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null)
                fragmentManager
                        .beginTransaction()
                        .add(R.id.coordinator, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
        });
        btnSync.setOnClickListener(v -> syncDatabase());
        txtLog.setMovementMethod(new ScrollingMovementMethod());
    }

    private void syncDatabase() {
        RepoListManager.clearSynced(context);
        clearDatabase();
        RxBus.publish(new LogEvent(getString(R.string.database_cleared)));
        RepoManager repoManager = new RepoManager(context);
        if (repoManager.getRepoCount() == 0)
            notifyAction(getString(R.string.toast_no_repo_selected));
        else {
            repoManager.fetchRepo();
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setMax(repoManager.getRepoCount());
            btnSync.setEnabled(false);
            btnSync.setText(getString(R.string.sync_progress));
        }
    }

    private void clearDatabase() {
        disposable.add(Observable.fromCallable(() -> new DatabaseTask(context)
                .clearAllTables())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    private void finalizeSync() {
        RxBus.clearLogEvents();
        btnSync.setText(getString(R.string.action_finish));
        btnSync.setEnabled(true);
        txtStatus.setText(getString(R.string.sync_completed));
        if (getActivity() instanceof IntroActivity)
            btnSync.setOnClickListener(v -> {
                getActivity().startActivity(new Intent(context, AuroraActivity.class));
                getActivity().finish();
            });
        else if (getActivity() instanceof SettingsActivity)
            btnSync.setOnClickListener(v -> {
                getActivity().onBackPressed();
            });
    }

    private void updateProgress() {
        progressBar.setProgress(++count);
        txtStatus.setText(getString(R.string.sync_progress));
        txtProgress.setText(new StringBuilder()
                .append(getProgress(progressBar.getProgress(), progressBar.getMax()))
                .append("%"));
    }

    private float getProgress(int current, int max) {
        float percent = ((float) current / (float) max) * 100.0f;
        return ((int) percent);
    }

    private void notifyAction(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }
}
